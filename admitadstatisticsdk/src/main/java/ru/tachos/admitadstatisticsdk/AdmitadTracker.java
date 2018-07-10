package ru.tachos.admitadstatisticsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public final class AdmitadTracker {
    public final static String ADMITAD_MOBILE_CHANNEL = "adm_mobile";
    public final static String UNKNOWN_CHANNEL = "na";

    private TrackerController controller;

    public void addListener(@NonNull TrackerListener listener) {
        controller.addListener(listener);
    }

    public void removeListener(@NonNull TrackerListener listener) {
        controller.removeListener(listener);
    }

    //Returns true if uid handled successfully
    public boolean handleDeeplink(@Nullable Uri uri) {
        return controller.handleDeeplink(uri);
    }

    // registration with default ADMITAD_MOBILE_CHANNEL
    public void logRegistration(String registrationId) {
        logRegistration(registrationId, ADMITAD_MOBILE_CHANNEL, null);
    }

    // registration with default ADMITAD_MOBILE_CHANNEL and listener
    public void logRegistration(String registrationId, @Nullable TrackerListener trackerListener) {
        logRegistration(registrationId, ADMITAD_MOBILE_CHANNEL, trackerListener);
    }

    // registration with preset channel
    public void logRegistration(String registrationId, String channel) {
        logRegistration(registrationId, channel, null);
    }

    // registration with preset channel and listener
    public void logRegistration(String registrationId, String channel, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createRegistrationEvent(registrationId, channel), trackerListener);
    }

    // purchase with default ADMITAD_MOBILE_CHANNEL
    public void logPurchase(AdmitadOrder order) {
        logPurchase(order, ADMITAD_MOBILE_CHANNEL, null);
    }

    // purchase with default ADMITAD_MOBILE_CHANNEL and listener
    public void logPurchase(AdmitadOrder order, @Nullable TrackerListener trackerListener) {
        logPurchase(order, ADMITAD_MOBILE_CHANNEL, trackerListener);
    }

    // purchase with preset channel
    public void logPurchase(AdmitadOrder order, String channel) {
        logPurchase(order, channel, null);
    }

    // purchase with preset channel and listener
    public void logPurchase(AdmitadOrder order, String channel, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createConfirmedPurchaseEvent(order, channel), trackerListener);
    }

    // order with default ADMITAD_MOBILE_CHANNEL
    public void logOrder(AdmitadOrder order) {
        logOrder(order, ADMITAD_MOBILE_CHANNEL, null);
    }

    // order with default ADMITAD_MOBILE_CHANNEL and lsitener
    public void logOrder(AdmitadOrder order, @Nullable TrackerListener trackerListener) {
        logOrder(order, ADMITAD_MOBILE_CHANNEL, trackerListener);
    }

    // order with preset channel
    public void logOrder(AdmitadOrder order, String channel) {
        logOrder(order, channel, null);
    }

    // order with preset channel and listener
    public void logOrder(AdmitadOrder order, String channel, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createPaidOrderEvent(order, channel), trackerListener);
    }

    // user return with default ADMITAD_MOBILE_CHANNEL
    public void logUserReturn(@Nullable String userId, int dayCount) {
        logUserReturn(userId, ADMITAD_MOBILE_CHANNEL, dayCount, null);
    }

    // user return with default ADMITAD_MOBILE_CHANNEL and listener
    public void logUserReturn(@Nullable String userId, int dayCount, @Nullable TrackerListener trackerListener) {
        logUserReturn(userId, ADMITAD_MOBILE_CHANNEL, dayCount, trackerListener);
    }

    // user return with preset channel
    public void logUserReturn(@Nullable String userId, String channel, int dayCount) {
        logUserReturn(userId, channel, dayCount, null);
    }

    // user return with preset channel and listener
    public void logUserReturn(@Nullable String userId, String channel, int dayCount, @Nullable TrackerListener trackerListener) {
        String user_id = TextUtils.isEmpty(userId) ? controller.getAdmitadUid() : userId;
        controller.log(EventFactory.createUserReturnEvent(user_id, channel, dayCount), trackerListener);
    }

    // loyalty with default ADMITAD_MOBILE_CHANNEL
    public void logUserLoyalty(@Nullable String userId, int loyalty) {
        logUserLoyalty(userId, ADMITAD_MOBILE_CHANNEL, loyalty, null);
    }

    // loyalty with default ADMITAD_MOBILE_CHANNEL and listener
    public void logUserLoyalty(@Nullable String userId, int loyalty, @Nullable TrackerListener trackerListener) {
        logUserLoyalty(userId, ADMITAD_MOBILE_CHANNEL, loyalty, trackerListener);
    }

    // loyalty with preset channel
    public void logUserLoyalty(@Nullable String userId, String channel, int loyalty) {
        logUserLoyalty(userId, channel, loyalty, null);
    }

    public void logUserLoyalty(@Nullable String userId, String channel, int loyalty, @Nullable TrackerListener trackerListener) {
        String user_id = TextUtils.isEmpty(userId) ? controller.getAdmitadUid() : userId;
        controller.log(EventFactory.createLoyaltyEvent(user_id, channel, loyalty), trackerListener);
    }

    private AdmitadTracker(@NonNull Context context,
                           @NonNull String postbackKey,
                           @Nullable TrackerInitializationCallback callback) {
        initTracker(context, postbackKey, callback);
    }

    private void initTracker(@NonNull final Context context,
                             @NonNull String postbackKey,
                             @Nullable final TrackerInitializationCallback callback) {
        this.controller = new TrackerControllerImpl(
                context,
                postbackKey,
                new Handler(),
                new DatabaseRepositorySQLite(context),
                new NetworkRepositoryImpl(),
                new TrackerInitializationCallback() {

                    @Override
                    public void onInitializationSuccess() {
                        if (Utils.isFirstLaunch(controller.getContext())) {
                            trackFirstLaunch(controller.getContext());
                        }

                        if (callback != null) {
                            callback.onInitializationSuccess();
                        }
                    }

                    @Override
                    public void onInitializationFailed(Exception exception) {
                        if (callback != null) {
                            callback.onInitializationFailed(exception);
                        }
                    }
                }
        );
    }

    private void trackFirstLaunch(Context context) {
        controller.log(Utils.getDeviceInfo(context), null);
    }

    public static void setLogEnabled(boolean isEnabled) {
        Utils.sLogEnabled = isEnabled;
    }

    @SuppressLint("StaticFieldLeak")
    private static AdmitadTracker instance;

    public static AdmitadTracker getInstance() {
        if (instance == null) {
            throw new NullPointerException("You must call AdmitadTracker#initialize() before using getInstance() method");
        }

        return instance;
    }

    public static void initialize(@NonNull Context context,
                                  @NonNull String postbackKey,
                                  @Nullable TrackerInitializationCallback callback) {
        instance = new AdmitadTracker(context, postbackKey, callback);
    }
}
