package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

class EventFactory {
    private static final String OID = "oid";
    private static final String USER_ID = "userid";
    private static final String DAY = "day";
    private static final String LOYAL = "loyal";

    static AdmitadEvent createRegistrationEvent(String registrationId) {
        Map<String, String> params = new HashMap<>();
        params.put(OID, registrationId);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_REGISTRATION, params);
    }

    static AdmitadEvent createConfirmedPurchaseEvent(AdmitadOrder order) {
        return order.toEvent(AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE);
    }

    static AdmitadEvent createPaidOrderEvent(AdmitadOrder order) {
        return order.toEvent(AdmitadEvent.Type.TYPE_PAID_ORDER);
    }

    static AdmitadEvent createUserReturnEvent(String userId, int days) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(DAY, String.valueOf(days));
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params);
    }

    static AdmitadEvent createLoyaltyEvent(String userId, int loyal) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(LOYAL, String.valueOf(loyal));
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params);
    }
}
