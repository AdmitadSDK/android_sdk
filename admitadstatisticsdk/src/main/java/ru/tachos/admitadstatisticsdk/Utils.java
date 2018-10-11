package ru.tachos.admitadstatisticsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ru.tachos.admitadstatisticsdk.network_state.NetworkState;

class Utils {
    private static final String KEY_CACHED_GAID = "KEY_CACHED_GAID";
    private static final String KEY_FIRST_START = "ADMITAD_TRACKER_KEY_FIRST_START";
    private static final String KEY_ADMITAD_ID = "ADMITAD_ID";
    private static final String KEY_REFERRER = "INSTALL_REFERRER";
    private final static String TAG = "AdmitadTracker";

    public static boolean sLogEnabled;

    static JSONObject collectDeviceInfo(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            @SuppressLint("HardwareIds")
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(androidId)) {
                jsonObject.put("hardware_id", androidId);
                jsonObject.put("is_hardware_id_real", false);
            }
            jsonObject.put("brand", Build.MANUFACTURER);
            jsonObject.put("model", Build.MODEL);
            jsonObject.put("product", Build.PRODUCT);
            jsonObject.put("device", Build.DEVICE);
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            jsonObject.put("screen_dpi", metrics.density * 160f);
            jsonObject.put("screen_height", metrics.heightPixels);
            jsonObject.put("screen_width", metrics.widthPixels);
            jsonObject.put("wifi",
                    NetworkState.getConnectivityStatus(context) == NetworkState.WIFI);
            jsonObject.put("os", "Android");
            jsonObject.put("os_version", Build.VERSION.RELEASE);
            jsonObject.put("sdk", AdmitadTracker.VERSION_NAME);
            jsonObject.put("installDate",
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ",
                            Locale.getDefault())
                            .format(Calendar.getInstance().getTime()));
            String gaid = getCachedGAID(context);
            if (!TextUtils.isEmpty(gaid)) {
                jsonObject.put("google_advertising_id", getCachedGAID(context));
            }
            TelephonyManager telephonyManager =
                    ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            if (telephonyManager != null) {
                String operatorName = telephonyManager.getNetworkOperatorName();
                String countryCode = telephonyManager.getSimCountryIso();
                if (!TextUtils.isEmpty(operatorName)) {
                    jsonObject.put("carrier", operatorName);
                    jsonObject.put("operator", operatorName);
                }
                if (!TextUtils.isEmpty(countryCode)) {
                    jsonObject.put("country", countryCode);
                }
            }
            jsonObject.put("lang_code", Locale.getDefault().getLanguage());
            jsonObject.put("lang", Locale.getDefault().getDisplayLanguage());

            try {
                jsonObject.put("currency", Currency.getInstance(Locale.getDefault()).getCurrencyCode());
            } catch (IllegalArgumentException | NullPointerException e) {
                // https://developer.android.com/reference/java/util/Currency.html#getInstance(java.util.Locale)
                // Locale.getDefault() can return:
                // 1) truncated locales (for example "en" instead of "en_US", because country is optional)
                // 2) deprecated locales (for example "en_UK" instead of "en_GB")
                // 3) locales without currency (for example Antarctica)
                // All of them throw on getCurrencyCode or returned currency can be null.
                jsonObject.put("currency", "");
            }

            JSONObject jsonDeviceData = new JSONObject();
            jsonDeviceData.put("build_display_id", Build.DISPLAY);
            jsonDeviceData.put("arch", System.getProperty("os.arch"));
            String cpu_abi = "";
            String cpu_abi2 = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cpu_abi = Arrays.toString(Build.SUPPORTED_32_BIT_ABIS);
                cpu_abi2 = Arrays.toString(Build.SUPPORTED_64_BIT_ABIS);
            }
            jsonDeviceData.put("cpu_abi", cpu_abi);
            jsonDeviceData.put("cpu_abi2", cpu_abi2);

            // https://developer.android.com/training/monitoring-device-state/battery-monitoring
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            float batteryPct = level / (float)scale;

            String state;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    state = "unknown"; break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    state = "charging"; break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    state = "discharging"; break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    state = "not charging"; break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    state = "full"; break;
                default:
                    state = "unknown";
            }

            jsonDeviceData.put("battery_level", batteryPct);
            jsonDeviceData.put("battery_state", state);
            jsonDeviceData.put("localip", getLocalIpAddress());

            jsonObject.put("deviceData", jsonDeviceData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    static String getCachedGAID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CACHED_GAID, "");
    }

    static void cacheGAID(Context context, String gaid) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_CACHED_GAID, gaid).apply();
    }

    static String getAdmitadUid(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ADMITAD_ID, "");
    }

    static void cacheUid(Context context, String gaid) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ADMITAD_ID, gaid).apply();
    }

    static String getReferrer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_REFERRER, "");
    }

    static void cacheReferrer(Context context, String referrer) {
        String cachedReferrer = Utils.getReferrer(context);
        if (!TextUtils.isEmpty(referrer) && !TextUtils.equals(cachedReferrer, referrer)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_REFERRER, referrer).apply();
        }
    }

    static boolean isFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isFirstStart = sharedPreferences.getBoolean(KEY_FIRST_START, true);
        if (isFirstStart) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_START, false).apply();
        }
        return isFirstStart;
    }

    public static String getLocalIpAddress() {
        String address = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        if (intf.getName().contains("wlan")) {
                            // WiFi interfaces are prioritized
                            return addr.getHostAddress();
                        } else if (TextUtils.isEmpty(address)) {
                            // keep first non-WiFi address if we won't find WiFi
                            address = addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (sLogEnabled) {
                Log.e(TAG, e.getMessage());
            }
        }

        return address;
    }
}
