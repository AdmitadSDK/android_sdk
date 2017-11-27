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
    private TrackerController controller;

    public void addListener(@NonNull TrackerListener listener) {
        controller.addListener(listener);
    }

    public void removeListener(@NonNull TrackerListener listener) {
        controller.removeListener(listener);
    }

    public void handleDeeplink(@Nullable Uri uri) {
        controller.handleDeeplink(uri);
    }

    public void logRegistration(String registrationId) {
        logRegistration(registrationId, null);
    }

    public void logRegistration(String registrationId, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createRegistrationEvent(registrationId), trackerListener);
    }

    public void logPurchase(AdmitadOrder order) {
        logPurchase(order, null);
    }

    public void logPurchase(AdmitadOrder order, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createConfirmedPurchaseEvent(order), trackerListener);
    }

    public void logOrder(AdmitadOrder order) {
        logOrder(order, null);
    }

    public void logOrder(AdmitadOrder order, @Nullable TrackerListener trackerListener) {
        controller.log(EventFactory.createPaidOrderEvent(order), trackerListener);
    }

    public void logUserReturn(@Nullable String userId, int dayCount) {
        logUserReturn(userId, dayCount, null);
    }

    public void logUserReturn(@Nullable String userId, int dayCount, @Nullable TrackerListener trackerListener) {
        controller.log(
                EventFactory.createUserReturnEvent(
                        TextUtils.isEmpty(userId) ? controller.getAdmitadUid() : userId,
                        dayCount),
                trackerListener);
    }

    public void logUserLoyalty(@Nullable String userId, int loyalty) {
        logUserLoyalty(userId, loyalty, null);
    }

    public void logUserLoyalty(@Nullable String userId, int loyalty, @Nullable TrackerListener trackerListener) {
        controller.log(
                EventFactory.createLoyaltyEvent(
                        TextUtils.isEmpty(userId) ? controller.getAdmitadUid() : userId,
                        loyalty),
                trackerListener);
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
                        if (Utils.isFirstLaunch(context)) {
                            trackFirstLaunch(context);
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
