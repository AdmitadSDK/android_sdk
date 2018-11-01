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

    private static final String DEVICE_TYPE = "adm_device";
    private static final String OS = "adm_ostype";
    private static final String METHOD = "adm_method";


    static AdmitadEvent createRegistrationEvent(String registrationId, String channel) {
        Map<String, String> params = idParameters();
        params.put(OID, registrationId);
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_REGISTRATION, params);
    }

    static AdmitadEvent createConfirmedPurchaseEvent(AdmitadOrder order, String channel) {
        AdmitadEvent orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE);
        Map<String, String> idParams = idParameters();
        orderEvent.params.putAll(idParams);
        orderEvent.params.put(CHANNEL, channel);
        return orderEvent;
    }

    static AdmitadEvent createPaidOrderEvent(AdmitadOrder order, String channel) {
        AdmitadEvent orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_PAID_ORDER);
        Map<String, String> idParams = idParameters();
        orderEvent.params.putAll(idParams);
        orderEvent.params.put(CHANNEL, channel);
        return orderEvent;
    }

    static AdmitadEvent createUserReturnEvent(String userId, String channel, int days) {
        Map<String, String> params = idParameters();
        params.put(USER_ID, userId);
        params.put(DAY, String.valueOf(days));
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params);
    }

    static AdmitadEvent createLoyaltyEvent(String userId, String channel, int loyal) {
        Map<String, String> params = idParameters();
        params.put(USER_ID, userId);
        params.put(LOYAL, String.valueOf(loyal));
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_LOYALTY, params);
    }

    static AdmitadEvent createInstallEvent(String channel, Context context) {
        Map<String, String> params = idParameters();

        if (TextUtils.isEmpty(Utils.getAdmitadUid(context))) {
            String referrer = Utils.getReferrer(context);
            AdmitadTracker.getInstance().handleDeeplink(Uri.parse(referrer));
        }
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_INSTALL, params);
    }

    static AdmitadEvent createFingerprintEvent(String channel, Context context) {
        JSONObject fingerprint = Utils.collectDeviceInfo(context);
        String referrer = Utils.getReferrer(context);

        Map<String, String> params = idParameters();
        params.put(FINGERPRINT, fingerprint.toString());
        params.put(REFERRER, referrer);
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_FINGERPRINT, params);
    }

    private static Map<String, String> idParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(DEVICE_TYPE, AdmitadTracker.DEVICE_TYPE);
        params.put(OS, AdmitadTracker.OS_TYPE);
        params.put(METHOD, AdmitadTracker.METHOD_TYPE);
        params.put(SDK, AdmitadTracker.VERSION_NAME);
        return params;
    }
}
