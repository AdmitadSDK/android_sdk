package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.net.Uri;

interface TrackerController {
    void addListener(TrackerListener listener);

    void removeListener(TrackerListener listener);

    void track(AdmitadEvent event, TrackerListener trackerListener);

    //Returns true if uid handled
    boolean handleDeeplink(Uri uri);

    String getAdmitadUid();

    Context getContext();
}
