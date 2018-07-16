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
    private static final String CHANNEL = "channel";

    static AdmitadEvent createRegistrationEvent(String registrationId, String channel) {
        Map<String, String> params = new HashMap<>();
        params.put(OID, registrationId);
        params.put(CHANNEL, channel);
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_REGISTRATION, params);
    }

    static AdmitadEvent createConfirmedPurchaseEvent(AdmitadOrder order, String channel) {
        AdmitadEvent order_event = order.toEvent(AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE);
        order_event.params.put(CHANNEL, channel);
        return order_event;
    }

    static AdmitadEvent createPaidOrderEvent(AdmitadOrder order, String channel) {
        AdmitadEvent order_event = order.toEvent(AdmitadEvent.Type.TYPE_PAID_ORDER);
        order_event.params.put(CHANNEL, channel);
        return order_event;
    }

    static AdmitadEvent createUserReturnEvent(String userId, String channel, int days) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(CHANNEL, channel);
        params.put(DAY, String.valueOf(days));
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params);
    }

    static AdmitadEvent createLoyaltyEvent(String userId, String channel, int loyal) {
        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(CHANNEL, channel);
        params.put(LOYAL, String.valueOf(loyal));
        return new AdmitadEvent(AdmitadEvent.Type.TYPE_LOYALTY, params);
    }
}
