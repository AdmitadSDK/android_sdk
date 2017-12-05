package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.net.Uri;

interface TrackerController {
    void addListener(TrackerListener listener);

    void removeListener(TrackerListener listener);

    void log(AdmitadEvent event, TrackerListener trackerListener);

    boolean handleDeeplink(Uri uri);

    String getAdmitadUid();

    Context getContext();
}
