package ru.tachos.admitadstatisticsdk;

import android.support.annotation.Nullable;

public interface TrackerListener {
    void onSuccess(AdmitadEvent result);

    void onFailure(@AdmitadTrackerCode int errorCode, @Nullable String errorText);
}