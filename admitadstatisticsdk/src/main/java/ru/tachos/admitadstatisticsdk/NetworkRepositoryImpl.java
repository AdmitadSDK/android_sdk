package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import okhttp3.*;
import okhttp3.Callback;

import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_FIRST_LAUNCH;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_LOYALTY;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_PAID_ORDER;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_REGISTRATION;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_RETURNED_USER;

public class NetworkRepositoryImpl implements NetworkRepository {
    private static final String TRACKING = "tracking";
    private static final String TAG = "AdmitadTracker";
    private static final String HOST = "ad.admitad.com";

    private OkHttpClient okHttpClient;
    private Handler uiHandler;

    public NetworkRepositoryImpl() {
        okHttpClient = new OkHttpClient();
        uiHandler = new Handler();
    }

    @Override
    public void log(final AdmitadEvent admitadEvent, final TrackerListener trackerListener) {
        HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment("r");
        for (String key : admitadEvent.params.keySet()) {
            httpUrlBuilder.addQueryParameter(key, admitadEvent.params.get(key));
        }
        if (admitadEvent.type != AdmitadEvent.Type.TYPE_FIRST_LAUNCH) {
            httpUrlBuilder.addQueryParameter(TRACKING, getEventConstant(admitadEvent.type));
        }
        HttpUrl httpUrl = httpUrlBuilder.build();
        logConsole(httpUrl.toString());
        Request okHttpRequest = new Request.Builder()
                .url(httpUrl)
                .build();
        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                logConsole("Exception: " + e.toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int code = AdmitadTrackerCode.ERROR_GENERIC;
                        if (!isServerAvailable()) {
                            code = AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE;
                        }
                        final int finalCode = code;
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                trackerListener.onFailure(finalCode, e.getMessage());
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    trackerListener.onSuccess(admitadEvent);
                    logConsole("Success: response code = " + response.code() + " response = " + response.toString());
                } else {
                    trackerListener.onFailure(response.code(), response.message());
                }
            }
        });
    }

    @Override
    public boolean isServerAvailable() {
        final Socket socket = new Socket();
        try {
            final InetAddress inetAddress = InetAddress.getByName(HOST);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 80);

            socket.connect(inetSocketAddress, 5000);
            return true;
        } catch (java.io.IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String getEventConstant(@AdmitadEvent.Type int code) {
        switch (code) {
            case TYPE_REGISTRATION:
                return "registration";
            case TYPE_CONFIRMED_PURCHASE:
                return "confirmed_purchase";
            case TYPE_PAID_ORDER:
                return "paid_order";
            case TYPE_RETURNED_USER:
                return "returned";
            case TYPE_LOYALTY:
                return "loyalty";
        }
        return "";
    }

    private void logConsole(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }
}
