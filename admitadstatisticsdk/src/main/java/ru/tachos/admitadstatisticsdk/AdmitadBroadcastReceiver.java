package ru.tachos.admitadstatisticsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Class to request install referrer from Play Install Referrer Library API
 *
 */
public class AdmitadBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "AdmitadTracker";
    private final static String REFERRER = "referrer";

    /**
     * Method handling broadcast from Google Play Store
     * @param context app context
     * @param intent app intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String referrerString = extras.getString(REFERRER);

            if (!TextUtils.isEmpty(referrerString)) {
                try {
                    String decodedReferrer = URLDecoder.decode(referrerString, "UTF-8");
                    Utils.cacheReferrer(context, decodedReferrer);
                    logConsole("INSTALL_REFERRER handled: " + decodedReferrer);
                } catch (UnsupportedEncodingException e) {
                    logConsole("Invalid INSTALL_REFERRER: " + referrerString);
                }
            }
        }
    }

    /**
     * Logging method
     * @param message message to log
     */
    private void logConsole(String message) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message);
        }
    }
}