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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ru.tachos.admitadstatisticsdk.network_state.NetworkManager;
import ru.tachos.admitadstatisticsdk.network_state.NetworkState;

final class TrackerControllerImpl implements TrackerController, NetworkManager.Listener {
    private final static long TIME_TO_CHECK_SERVER = TimeUnit.SECONDS.toMillis(10);
    private final static long TIME_TO_TRY_AGAIN = TimeUnit.SECONDS.toMillis(5);

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
        this.context = context;
        this.postbackKey = postbackKey;
        this.databaseRepository = databaseRepository;
        this.networkRepository = networkRepository;
        this.uiHandler = uiHandler;
        initilize(callback);
    }

    public void addListener(@NonNull TrackerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull TrackerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void log(AdmitadEvent event, @Nullable TrackerListener trackerListener) {
        logConsole("New event: " + event.toString());
        databaseRepository.insertOrUpdate(event);
        eventQueue.add(0, new Pair<>(event, new WeakReference<>(trackerListener)));
        tryLog();
    }

    @Override
    public void handleDeeplink(Uri uri) {
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
                    }
                }
            }
        }
    }

    @Override
    public String getAdmitadUid() {
        return admitadUid;
    }

    @Override
    public void onNetworkStateChanged(NetworkState networkState) {
        this.networkState = networkState;
        tryLog();
    }

    private void initilize(@Nullable TrackerInitializationCallback callback) {
        NetworkManager networkManager = new NetworkManager(context);
        networkManager.addListener(this);
        this.networkState = networkManager.getCurrentState();
        new InitializationAsynctask(callback).execute();
    }

    private void tryLog() {
        if (eventQueue.size() > 0 && !isBusy) {
            int errorCode = AdmitadTrackerCode.NONE;
            if (!isInitialized) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED;
            }
            if (TextUtils.isEmpty(postbackKey)) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_POSTBACK_CODE_MISSED;
            }
            if (TextUtils.isEmpty(admitadUid)) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_ADMITAD_UID_MISSED;
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
                logConsole("Trying to send " + admitadEvent.toString());
                isBusy = true;
                networkRepository.log(fillRequiredParams(admitadEvent), new NetworkLogCallback(admitadPair));
            } else {
                onLogFailed(admitadPair, errorCode, "");
            }
        }
    }

    private AdmitadEvent fillRequiredParams(AdmitadEvent admitadEvent) {
        admitadEvent.params.put("pk", postbackKey);
        admitadEvent.params.put("uid", admitadUid);
        admitadEvent.params.put("device", gaid);
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
        if (eventQueue.size() == 0) {
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
        logConsole("log success " + admitadPair.first.toString());
        isBusy = false;
        if (admitadPair.second != null) {
            TrackerListener trackerListener = admitadPair.second.get();
            if (trackerListener != null) {
                trackerListener.onSuccess(admitadPair.first);
            }
        }
        notifyLogSuccess(admitadPair.first);
        databaseRepository.remove(admitadPair.first.id);
        eventQueue.remove(admitadPair);
        tryLog();
    }

    private void onLogFailed(Pair<AdmitadEvent,  WeakReference<TrackerListener>> admitadPair, int errorCode, @Nullable String errorText) {
        logConsole("Log failed, errorCode = " + errorCode + ", text = " + errorText);
        isBusy = false;
        if (!networkState.isOnline() && errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            errorCode = AdmitadTrackerCode.ERROR_NO_INTERNET;
        }
        if (errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            isServerUnavailable = true;
            onServerUnavailable();
        }
        if (admitadPair.second != null) {
            TrackerListener trackerListener = admitadPair.second.get();
            if (trackerListener != null) {
                trackerListener.onFailure(errorCode, errorText);
            }
        }
        notifyLogFailed(errorCode, errorText);
        if (errorCode != AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE
                && errorCode != AdmitadTrackerCode.ERROR_NO_INTERNET
                && errorCode != AdmitadTrackerCode.ERROR_SDK_GAID_MISSED
                && errorCode != AdmitadTrackerCode.ERROR_SDK_ADMITAD_UID_MISSED
                && errorCode != AdmitadTrackerCode.ERROR_SDK_POSTBACK_CODE_MISSED
                && errorCode != AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    tryLog();
                }
            }, TIME_TO_TRY_AGAIN);
        }
    }

    private void notifyLogSuccess(AdmitadEvent event) {
        for (TrackerListener listener : listeners) {
            listener.onSuccess(event);
        }
    }

    private void notifyLogFailed(int errorCode, @Nullable String errorText) {
        for (TrackerListener listener : listeners) {
            listener.onFailure(errorCode, errorText);
        }
    }

    private void logConsole(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    private class NetworkLogCallback implements TrackerListener {
        private final Pair<AdmitadEvent, WeakReference<TrackerListener>> admitadPair;

        private NetworkLogCallback(@NonNull Pair<AdmitadEvent,  WeakReference<TrackerListener>> admitadPair) {
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
            for (AdmitadEvent admitadEvent : databaseRepository.findAll()) {
                eventQueue.add(new Pair<AdmitadEvent, WeakReference<TrackerListener>>(admitadEvent, null));
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

            if (isServerUnavailable) {
                onServerUnavailable();
            }
        }
    }

    private static class GaidAsyncTaskResult {
        private String gaid;
        private Exception exception;
    }
}
