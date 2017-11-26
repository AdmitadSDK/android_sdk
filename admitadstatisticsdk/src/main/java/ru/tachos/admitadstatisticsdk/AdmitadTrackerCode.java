package ru.tachos.admitadstatisticsdk;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_GENERIC;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_NO_INTERNET;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_SDK_ADMITAD_UID_MISSED;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_SDK_GAID_MISSED;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.ERROR_SDK_POSTBACK_CODE_MISSED;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.NONE;
import static ru.tachos.admitadstatisticsdk.AdmitadTrackerCode.SUCCESS;

@IntDef({NONE, SUCCESS, ERROR_GENERIC, ERROR_NO_INTERNET, ERROR_SERVER_UNAVAILABLE, ERROR_SDK_NOT_INITIALIZED, ERROR_SDK_POSTBACK_CODE_MISSED, ERROR_SDK_GAID_MISSED, ERROR_SDK_ADMITAD_UID_MISSED})
@Retention(RetentionPolicy.SOURCE)
public @interface AdmitadTrackerCode {
    int NONE = 0;
    int SUCCESS = 200;

    int ERROR_GENERIC = -1;
    int ERROR_NO_INTERNET = -2;
    int ERROR_SERVER_UNAVAILABLE = -3;
    int ERROR_SDK_NOT_INITIALIZED = -10;
    int ERROR_SDK_POSTBACK_CODE_MISSED = -11;
    int ERROR_SDK_GAID_MISSED = -12;
    int ERROR_SDK_ADMITAD_UID_MISSED = -13;
}
