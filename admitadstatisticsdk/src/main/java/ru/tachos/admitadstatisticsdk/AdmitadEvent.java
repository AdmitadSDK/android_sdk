package ru.tachos.admitadstatisticsdk;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;

import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_FINGERPRINT;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_INSTALL;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_LOYALTY;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_PAID_ORDER;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_REGISTRATION;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_RETURNED_USER;

public class AdmitadEvent {
    public long id;
    @Type
    public final int type;
    public final Map<String, String> params;

    public AdmitadEvent(@Type final int type, final Map<String, String> mainParams) {
        this.type = type;
        this.params = Collections.synchronizedMap(mainParams);
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AdmitadEvent{" +
                "type=" + typeToString(type) +
                ", params=" + params +
                '}';
    }

    private static String typeToString(@Type final int code) {
        switch (code) {
            case TYPE_INSTALL:
                return "Install";
            case TYPE_REGISTRATION:
                return "Registration";
            case TYPE_CONFIRMED_PURCHASE:
                return "Confirmed purchase";
            case TYPE_PAID_ORDER:
                return "Paid order";
            case TYPE_RETURNED_USER:
                return "Returned user";
            case TYPE_LOYALTY:
                return "Loyalty";
            case TYPE_FINGERPRINT:
                return "Fingerprint";
        }
        return "";
    }

    @IntDef({TYPE_INSTALL, TYPE_REGISTRATION, TYPE_CONFIRMED_PURCHASE, TYPE_PAID_ORDER, TYPE_RETURNED_USER, TYPE_LOYALTY, TYPE_FINGERPRINT})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface Type {
        int TYPE_INSTALL = 1;
        int TYPE_REGISTRATION = 2;
        int TYPE_CONFIRMED_PURCHASE = 3;
        int TYPE_PAID_ORDER = 4;
        int TYPE_RETURNED_USER = 5;
        int TYPE_LOYALTY = 6;
        int TYPE_FINGERPRINT = 7;
    }

}
