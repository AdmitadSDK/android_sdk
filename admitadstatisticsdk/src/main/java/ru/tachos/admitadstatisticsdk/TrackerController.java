package ru.tachos.admitadstatisticsdk;

import android.net.Uri;

interface TrackerController {
    void addListener(TrackerListener listener);

    void removeListener(TrackerListener listener);

    void log(AdmitadEvent event, TrackerListener trackerListener);

    void handleDeeplink(Uri uri);

    String getAdmitadUid();
}
