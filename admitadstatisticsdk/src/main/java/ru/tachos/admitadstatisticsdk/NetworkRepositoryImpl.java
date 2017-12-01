package ru.tachos.admitadstatisticsdk;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.*;
import okhttp3.Callback;

import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_FIRST_LAUNCH;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_LOYALTY;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_PAID_ORDER;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_REGISTRATION;
import static ru.tachos.admitadstatisticsdk.AdmitadEvent.Type.TYPE_RETURNED_USER;

public class NetworkRepositoryImpl implements NetworkRepository {

    private static final String URL_ENCODE_TYPE = "UTF-8";

    private static final String TRACKING = "tracking";
    private static final String TAG = "AdmitadTracker";

    private static final String SCHEME = "https";
    private static final String HOST = "ad.admitad.com";
    private static final String PATH = "r";

    private static final String SCHEME_INSTALL = "https";
    private static final String HOST_INSTALL = "ad.admitad.com";
    private static final String PATH_INSTALL = "r";

    private OkHttpClient okHttpClient;
    private Handler uiHandler;

    public NetworkRepositoryImpl() {
        okHttpClient = new OkHttpClient();
        uiHandler = new Handler();
    }

    @Override
    public void log(final AdmitadEvent admitadEvent, final TrackerListener trackerListener) {
        StringBuilder urlBuilder = new StringBuilder();
        if (admitadEvent.type == TYPE_FIRST_LAUNCH) {
            urlBuilder.append(SCHEME_INSTALL).append("://")
                    .append(HOST_INSTALL).append("/").append(PATH_INSTALL);
        } else {
            urlBuilder.append(SCHEME).append("://")
                    .append(HOST).append("/").append(PATH);
        }

        String paramsPath = getUrlQuery(admitadEvent.params);
        try {
            paramsPath = URLEncoder.encode(paramsPath, URL_ENCODE_TYPE);
        } catch (UnsupportedEncodingException pE) {
            pE.printStackTrace();
        }

        urlBuilder.append("?")
                .append(paramsPath);

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

    private String getUrlQuery(final Map<String, String> params) {
        final StringBuilder queryBuilder = new StringBuilder();
        for (final String key : params.keySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append("&");
            }
            queryBuilder.append(key)
                    .append("=")
                    .append(params.get(key));
        }
        return queryBuilder.toString();
    }
}
