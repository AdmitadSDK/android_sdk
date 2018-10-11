package ru.tachos.admitadstatisticsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ru.tachos.admitadstatisticsdk.network_state.NetworkManager;
import ru.tachos.admitadstatisticsdk.network_state.NetworkState;

final class TrackerControllerImpl implements TrackerController, NetworkManager.Listener {

    private final static long TIME_TO_CHECK_SERVER = TimeUnit.MINUTES.toMillis(5);
    private final static long TIME_TO_TRY_AGAIN = TimeUnit.MINUTES.toMillis(2);

    private final static String TAG = "AdmitadTracker";
    private final static String URI_KEY_ADMITAD_UID = "uid";
    private final DatabaseRepository databaseRepository;
    private final NetworkRepository networkRepository;
    private final Handler uiHandler;
    private final Set<TrackerListener> listeners = new LinkedHashSet<>();
    private final List<Pair<AdmitadEvent, WeakReference<TrackerListener>>> eventQueue = new LinkedList<>();
    private final Context context;
    private NetworkState networkState;
    private String gaid;
    private String postbackKey;
    private String admitadUid;
    private boolean isInitialized = false;
    private boolean isBusy = false;
    private boolean isServerUnavailable = false;

    TrackerControllerImpl(Context context,
                          String postbackKey,
                          Handler uiHandler,
                          DatabaseRepository databaseRepository,
                          NetworkRepository networkRepository,
                          @Nullable TrackerInitializationCallback callback) {
        if (TextUtils.isEmpty(postbackKey)) {
            throw new NullPointerException("Postback key must be non-null");
        }
        this.context = context;
        this.postbackKey = postbackKey;
        this.databaseRepository = databaseRepository;
        this.networkRepository = networkRepository;
        this.uiHandler = uiHandler;
        Collections.synchronizedList(eventQueue);
        initialize(callback);
    }

    public void addListener(@NonNull TrackerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull TrackerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void track(AdmitadEvent event, @Nullable TrackerListener trackerListener) {
        if (Utils.sLogEnabled) {
            logConsole("New event: " + event);
        }

        fillRequiredParams(event);
        databaseRepository.insertOrUpdate(event);
        synchronized (eventQueue) {
            eventQueue.add(0, new Pair<>(event, new WeakReference<>(trackerListener)));
        }
        tryLog();
    }

    @Override
    public boolean handleDeeplink(Uri uri) {
        if (uri != null) {
            logConsole("Deeplink handled, uri = " + uri);
            for (String key : uri.getQueryParameterNames()) {
                if (TextUtils.equals(key, URI_KEY_ADMITAD_UID)) {
                    String newUid = uri.getQueryParameter(key);
                    if (!TextUtils.isEmpty(newUid)) {
                        logConsole("Admitad UID handled, new UID = " + newUid + ", last UID = " + admitadUid);
                        admitadUid = newUid;
                        Utils.cacheUid(context, newUid);
                        tryLog();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getAdmitadUid() {
        return admitadUid;
    }

    @Override
    public void onNetworkStateChanged(NetworkState networkState) {
        logConsole("Network state changed, new status = " + networkState.status);
        this.networkState = networkState;
        isServerUnavailable = false;
        if (networkState.isOnline()) {
            tryLog();
        }
    }

    public Context getContext() {
        return context;
    }

    private void initialize(@Nullable TrackerInitializationCallback callback) {
        NetworkManager networkManager = new NetworkManager(context);
        networkManager.addListener(this);
        this.networkState = networkManager.getCurrentState();
        new InitializationAsynctask(callback).execute();
    }

    private void tryLog() {
        if (!eventQueue.isEmpty() && !isBusy) {
            int errorCode = AdmitadTrackerCode.NONE;
            if (!isInitialized) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED;
            }
            if (TextUtils.isEmpty(gaid)) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_GAID_MISSED;
            }
            if (!networkState.isOnline()) {
                errorCode = AdmitadTrackerCode.ERROR_NO_INTERNET;
            } else {
                if (isServerUnavailable) {
                    errorCode = AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE;
                }
            }

            Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair = eventQueue.get(eventQueue.size() - 1);
            AdmitadEvent admitadEvent = admitadPair.first;
            if (errorCode == AdmitadTrackerCode.NONE) {
                if (Utils.sLogEnabled) {
                    logConsole("Trying to send " + admitadEvent.toString());
                }
                isBusy = true;
                admitadEvent.params.put("device", gaid);
                networkRepository.log(admitadEvent, new NetworkLogCallback(admitadPair));
            } else {
                onLogFailed(admitadPair, errorCode, "");
            }
        }
    }

    private AdmitadEvent fillRequiredParams(AdmitadEvent admitadEvent) {
        synchronized (admitadEvent.params) {
            admitadEvent.params.put("pk", postbackKey);
            if (admitadEvent.type != AdmitadEvent.Type.TYPE_FINGERPRINT) {
                admitadEvent.params.put("uid", admitadUid);
            }
        }
        return admitadEvent;
    }

    private void notifyInitializationSuccess(@Nullable TrackerInitializationCallback callback) {
        if (callback != null) {
            callback.onInitializationSuccess();
        }
    }

    private void notifyInitializationFailed(Exception e, @Nullable TrackerInitializationCallback callback) {
        if (callback != null) {
            callback.onInitializationFailed(e);
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initialization failed with exception " + e);
        }
    }

    private void onServerUnavailable() {
        logConsole("Server unavailable");
        if (eventQueue.isEmpty()) {
            return;
        }
        uiHandler.removeCallbacksAndMessages(null);

        uiHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        logConsole("Try to check if server available");
                        isServerUnavailable = !networkRepository.isServerAvailable();
                        logConsole("Checked server, server available = " + !isServerUnavailable);
                        if (isServerUnavailable) {
                            onServerUnavailable();
                        } else {
                            tryLog();
                        }
                    }
                }).start();
            }
        }, TIME_TO_CHECK_SERVER);
    }

    private void onLogSuccess(Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair) {
        if (Utils.sLogEnabled) {
            logConsole("log success " + admitadPair.first.toString());
        }
        isBusy = false;
        TrackerListener trackerListener = null;
        if (admitadPair.second != null) {
            trackerListener = admitadPair.second.get();
        }
        notifyLogSuccess(admitadPair.first, trackerListener);
        databaseRepository.remove(admitadPair.first.id);
        synchronized (eventQueue) {
            if (!eventQueue.isEmpty() && eventQueue.get(eventQueue.size() - 1) == admitadPair) {
                eventQueue.remove(eventQueue.size() - 1);
            } else {
                eventQueue.remove(admitadPair);
            }
        }
        tryLog();
    }

    private void onLogFailed(Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair, int errorCode, @Nullable String errorText) {
        logConsole("Log failed, errorCode = " + errorCode + ", text = " + errorText);
        isBusy = false;
        if (!networkState.isOnline() && errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            errorCode = AdmitadTrackerCode.ERROR_NO_INTERNET;
        }
        if (errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            isServerUnavailable = true;
            onServerUnavailable();
        }
        TrackerListener trackerListener = null;
        if (admitadPair.second != null) {
            trackerListener = admitadPair.second.get();
        }
        notifyLogFailed(errorCode, errorText, trackerListener);
        if (errorCode != AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE
                && errorCode != AdmitadTrackerCode.ERROR_NO_INTERNET
                && errorCode != AdmitadTrackerCode.ERROR_SDK_GAID_MISSED
                && errorCode != AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED) {
            this.uiHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    tryLog();
                }
            }, TIME_TO_TRY_AGAIN);
        }
    }

    private void notifyLogSuccess(AdmitadEvent event, @Nullable TrackerListener trackerListener) {
        if (trackerListener != null) {
            trackerListener.onSuccess(event);
        }

        for (TrackerListener listener : listeners) {
            listener.onSuccess(event);
        }
    }

    private void notifyLogFailed(int errorCode, @Nullable String errorText, @Nullable TrackerListener trackerListener) {
        if (trackerListener != null) {
            trackerListener.onFailure(errorCode, errorText);
        }

        for (TrackerListener listener : listeners) {
            listener.onFailure(errorCode, errorText);
        }
    }

    private void logConsole(String message) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message);
        }
    }

    private class NetworkLogCallback implements TrackerListener {

        private final Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair;

        private NetworkLogCallback(@NonNull Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair) {
            this.admitadPair = admitadPair;
        }

        @Override
        public void onSuccess(AdmitadEvent result) {
            onLogSuccess(admitadPair);
        }

        @Override
        public void onFailure(int errorCode, @Nullable String errorText) {
            onLogFailed(admitadPair, errorCode, errorText);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InitializationAsynctask extends AsyncTask<Void, Void, GaidAsyncTaskResult> {

        @Nullable
        TrackerInitializationCallback callback;

        public InitializationAsynctask(@Nullable TrackerInitializationCallback callback) {
            this.callback = callback;
        }

        @Override
        protected GaidAsyncTaskResult doInBackground(Void... voids) {
            GaidAsyncTaskResult result = new GaidAsyncTaskResult();
            isServerUnavailable = !networkRepository.isServerAvailable();
            for (final AdmitadEvent admitadEvent : databaseRepository.findAll()) {
                result.events.add(0, new Pair<AdmitadEvent, WeakReference<TrackerListener>>(admitadEvent, null));
            }
            result.gaid = Utils.getCachedGAID(context);
            admitadUid = Utils.getAdmitadUid(context);
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                if (adInfo != null && !TextUtils.isEmpty(adInfo.getId())) {
                    result.gaid = adInfo.getId();
                }
            } catch (Exception e) {
                result.exception = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(GaidAsyncTaskResult result) {
            if (TextUtils.isEmpty(result.gaid)) {
                notifyInitializationFailed(result.exception, callback);
                logConsole("Initialize failed, e = " + result.exception);
            } else {
                isInitialized = true;
                gaid = result.gaid;
                notifyInitializationSuccess(callback);
                Utils.cacheGAID(context, result.gaid);
                logConsole("Initialize success, gaid = " + gaid + ", uid = " + admitadUid + ", key = " + postbackKey + ", server availability " + !isServerUnavailable);
            }

            synchronized (eventQueue) {
                eventQueue.addAll(0, result.events);
            }

            if (isServerUnavailable) {
                onServerUnavailable();
            } else {
                tryLog();
            }
        }
    }

    private static class GaidAsyncTaskResult {

        private String gaid;
        private List<Pair<AdmitadEvent, WeakReference<TrackerListener>>> events = new LinkedList<>();
        private Exception exception;
    }
}
