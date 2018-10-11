package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class EventFactory {
    private static final String OID = "oid";
    private static final String USER_ID = "userid";
    private static final String DAY = "day";
    private static final String LOYAL = "loyal";
    private static final String CHANNEL = "channel";
    private static final String FINGERPRINT = "fingerprint";
    private static final String SDK = "sdk";
    private static final String REFERRER = "referer";

    static AdmitadEvent createRegistrationEvent(String registrationId, String channel) {
        Map<String, String> params = new HashMap<>();
        params.put(OID, registrationId);
        params.put(CHANNEL, channel);
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_REGISTRATION, params);
    }

    static AdmitadEvent createConfirmedPurchaseEvent(AdmitadOrder order, String channel) {
        AdmitadEvent orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE);
        orderEvent.params.put(CHANNEL, channel);
        orderEvent.params.put(SDK, AdmitadTracker.VERSION_NAME);
        return orderEvent;
    }

    static AdmitadEvent createPaidOrderEvent(AdmitadOrder order, String channel) {
        AdmitadEvent orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_PAID_ORDER);
        orderEvent.params.put(CHANNEL, channel);
        orderEvent.params.put(SDK, AdmitadTracker.VERSION_NAME);
        return orderEvent;
    }

    static AdmitadEvent createUserReturnEvent(String userId, String channel, int days) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(CHANNEL, channel);
        params.put(DAY, String.valueOf(days));
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params);
    }

    static AdmitadEvent createLoyaltyEvent(String userId, String channel, int loyal) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(CHANNEL, channel);
        params.put(LOYAL, String.valueOf(loyal));
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_LOYALTY, params);
    }

    static AdmitadEvent createInstallEvent(String channel, Context context) {
        Map<String, String> params = new HashMap<>();

        if (TextUtils.isEmpty(Utils.getAdmitadUid(context))) {
            String referrer = Utils.getReferrer(context);
            AdmitadTracker.getInstance().handleDeeplink(Uri.parse(referrer));
        }

        params.put(CHANNEL, channel);
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_INSTALL, params);
    }

    static AdmitadEvent createFingerprintEvent(String channel, Context context) {
        JSONObject fingerprint = Utils.collectDeviceInfo(context);
        String referrer = Utils.getReferrer(context);

        Map<String, String> params = new HashMap<>();
        params.put(FINGERPRINT, fingerprint.toString());
        params.put(CHANNEL, channel);
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        params.put(REFERRER, referrer);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_FINGERPRINT, params);
    }
}
