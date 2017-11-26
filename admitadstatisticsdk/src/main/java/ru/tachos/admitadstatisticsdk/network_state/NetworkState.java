package ru.tachos.admitadstatisticsdk.network_state;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NetworkState {
    @IntDef({NOT_CONNECTED, WIFI, MOBILE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Status {
    }
    public static final int NOT_CONNECTED = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;

    @Status
    public int status =  NOT_CONNECTED;

    public NetworkState(@Status int status) {
        this.status = status;
    }

    public boolean isOnline() {
        return status != NetworkState.NOT_CONNECTED;
    }

    public boolean isWifi() {
        return status == NetworkState.WIFI;
    }

    public boolean isData() {
        return status == NetworkState.MOBILE;
    }

    @Status
    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return MOBILE;
        }
        return  NOT_CONNECTED;
    }
}
