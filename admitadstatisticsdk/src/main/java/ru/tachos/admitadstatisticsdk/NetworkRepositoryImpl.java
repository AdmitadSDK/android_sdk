package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertPathValidatorException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okhttp3.Callback;

import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_FIRST_LAUNCH;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_LOYALTY;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_PAID_ORDER;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_REGISTRATION;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_RETURNED_USER;

public class NetworkRepositoryImpl implements NetworkRepository {
    private static final int TIME_OUT = 20; //seconds

    private static final String ENCODE = "UTF-8";

    private static final String TRACKING = "tracking";
    private static final String TAG = "AdmitadTracker";

    private static final String SCHEME = "https://";
    private static final String HOST = "ad.admitad.com";
    private static final String PATH = "r";

    private static final String SCHEME_INSTALL = "https://";
    private static final String HOST_INSTALL = "ad.admitad.com";
    private static final String PATH_INSTALL = "r";

    private OkHttpClient okHttpClient;
    private Handler uiHandler;

    public NetworkRepositoryImpl() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build();
        uiHandler = new Handler();
    }

    @Override
    public void log(final AdmitadEvent admitadEvent, final TrackerListener trackerListener) {
        StringBuilder urlBuilder = new StringBuilder();
        if (admitadEvent.type == TYPE_FIRST_LAUNCH) {
            urlBuilder.append(SCHEME_INSTALL)
                    .append(HOST_INSTALL).append("/").append(PATH_INSTALL);
        } else {
            urlBuilder.append(SCHEME)
                    .append(HOST).append("/").append(PATH);
        }

        urlBuilder.append("?")
                .append(getUrlQuery(admitadEvent));

        final String url = urlBuilder.toString();
        logConsole(url);

        final Request okHttpRequest = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
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
            try {
                final URL url = new URL(SCHEME + HOST);
                final HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(TIME_OUT * 1000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
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
        if (Utils.sLogEnabled) {
            Log.d(TAG, message);
        }
    }

    private String getUrlQuery(final AdmitadEvent admitadEvent) {
        final StringBuilder queryBuilder = new StringBuilder();
        for (final String key : admitadEvent.params.keySet()) {
            addParam(queryBuilder, key, admitadEvent.params.get(key), queryBuilder.length() == 0);
        }
        if (admitadEvent.type != AdmitadEvent.Type.TYPE_FIRST_LAUNCH) {
            addParam(queryBuilder, TRACKING, getEventConstant(admitadEvent.type), false);
        }
        return queryBuilder.toString();
    }

    private void addParam(final StringBuilder builder, final String key, final String value, final boolean firstParam) {
        try {
            if (!firstParam) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(key, ENCODE))
                    .append("=")
                    .append(URLEncoder.encode(value, ENCODE));
        } catch (UnsupportedEncodingException pE) {
            pE.printStackTrace();
        }
    }
}
