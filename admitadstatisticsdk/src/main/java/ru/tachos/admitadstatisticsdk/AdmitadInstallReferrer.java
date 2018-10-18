package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class to request install referrer from Play Install Referrer Library API
 * https://developer.android.com/google/play/installreferrer/library#java
 */
public class AdmitadInstallReferrer {

    private final static String TAG = "AdmitadTracker";

    // InstallReferrer client
    private InstallReferrerClient mReferrerClient;
    // application context
    private Context context;
    // lock object for synchronization
    private final Object lock;
    // if true install referrer is received
    private boolean referrerReceived;
    // reties count
    private int retries;
    // delay in seconds before next retry
    private long retryDelay;

    // maximal retries number
    private final static int MAX_RETRIES = 2;
    // delay increase step after each retry
    private final static long RETRY_DELAY_STEP = 3;

    // scheduling service
    private ScheduledExecutorService retryScheduler;

    public AdmitadInstallReferrer(final Context context) {
        this.context = context;
        this.lock = new Object();
        this.retries = 0;
        this.retryDelay = 0;
        this.referrerReceived = false;

        // new threads will be created only if queue is full
        // corePoolSize - number of core threads = 2
        this.retryScheduler = Executors.newScheduledThreadPool(2);
        logConsole("AdmitadInstallReferrer initialized");
    }

    /**
     * Request install referrer from Install Referrer service
     */
    public void requestInstallReferrer() {
        if (context == null) {
            logConsole("Null application context, unable to request install referrer");
            return;
        }

        // return if install referrer already received
        synchronized (lock) {
            if (referrerReceived) {
                return;
            }
        }

        if (mReferrerClient != null) {
            closeConnection();
        }

        if (retries < MAX_RETRIES) {
            // runnable task for receiving referrer
            Runnable request_referrer = new Runnable() {
                @Override
                public void run() {
                    logConsole(">>> Retry #" + Integer.toString(retries) + " <<<");
                    startConnection();
                    logConsole("Started connection with " + Long.toString(retryDelay) + " seconds delay");
                    retries += 1;
                }
            };

            // schedule runnable task with retryDelay delay
            retryScheduler.schedule(request_referrer, RETRY_DELAY_STEP, TimeUnit.SECONDS);
            logConsole("Scheduled install referrer request with " + Long.toString(retryDelay) + " seconds delay");
        } else {
            closeConnection();
            logConsole("Maximum of retries " + Integer.toString(MAX_RETRIES) + " exceeded");
            // manually shutdown ScheduledExecutorService,
            // but all previously scheduled threads will be executed
            retryScheduler.shutdown();
            logConsole("AdmitadInstallReferrer shutdown");
        }
    }

    /**
     * Set initial delay before requesting install
     * Should be optional set before calling requestInstallReferrer method
     *
     * @param delay initial delay in seconds
     */
    public void setInitialRetryDelay(long delay) {
        retryDelay = delay;
    }

    /**
     * Create InstallReferrer client in context of current app
     *
     * @param context app context
     * @return new instance of InstallReferrer
     */
    private InstallReferrerClient createReferrerClient(Context context) {
        logConsole("Created InstallReferrerClient instance");
        return InstallReferrerClient.newBuilder(context).build();
    }

    /**
     * Init connection to Install Referrer service
     */
    private void startConnection() {
        mReferrerClient = createReferrerClient(context);
        mReferrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerResponse.OK:
                        // Connection established
                        ReferrerDetails response;
                        try {
                            response = mReferrerClient.getInstallReferrer();
                            String referrerString = response.getInstallReferrer();

                            if (!TextUtils.isEmpty(referrerString)) {
                                try {
                                    String decodedReferrer = URLDecoder.decode(referrerString, "UTF-8");
                                    Utils.cacheReferrer(context, decodedReferrer);
                                    logConsole("Received referrer from Install Referrer service: " + decodedReferrer);
                                } catch (UnsupportedEncodingException e) {
                                    logConsole("Invalid install referrer: " + referrerString);
                                }
                            }

                            // install referrer received
                            synchronized (lock) {
                                referrerReceived = true;
                            }
                        } catch (RemoteException e) {
                            logConsole("Handled remote exception during connection to Install Referrer service");
                        }
                        break;
                    case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app
                        logConsole("API not available on the current version of Google Play Store app (< 8.3.73)");
                        break;
                    case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Try to restart the connection to the Google Install Referrer service.
                        logConsole("Connection with Install Referrer service not established");
                        requestInstallReferrer();
                        break;
                    default:
                        logConsole("Unexpected response code from Install Referrer service");
                        break;
                }

                closeConnection();

                retryScheduler.shutdown();
                logConsole("AdmitadInstallReferrer shutdown");
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection to Google Install Referrer service
                requestInstallReferrer();
                logConsole("Lost connection to Install Referrer service, retrying again");
            }
        });
    }

    /**
     * Close connection to Install Referrer service
     */
    private void closeConnection() {
        if (mReferrerClient == null) {
            return;
        }

        mReferrerClient.endConnection();
        logConsole("Closed connection to Install Referrer service");

        mReferrerClient = null;
    }

    /**
     * Logging method
     *
     * @param message message to log
     */
    private void logConsole(String message) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message);
        }
    }
}
