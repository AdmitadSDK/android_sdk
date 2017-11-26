package ru.tachos.admitadstatisticsdk.network_state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.HashSet;
import java.util.Set;

public class NetworkManager {
    private NetworkState networkState;
    private Set<Listener> listeners = new HashSet<>();

    public NetworkManager(Context context) {
        networkState = new NetworkState(NetworkState.getConnectivityStatus(context));
        BroadcastReceiver internetConnectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int newStatus = NetworkState.getConnectivityStatus(context);
                if (newStatus != networkState.status) {
                    networkState.status = newStatus;
                    notifyNetworkStateChanged(networkState);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(internetConnectivityReceiver, filter);
    }

    public NetworkState getCurrentState() {
        return networkState;
    }

    public boolean isOnline() {
        return networkState.isOnline();
    }

    public boolean isWifi() {
        return networkState.isWifi();
    }

    public boolean isData() {
        return networkState.isData();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyNetworkStateChanged(NetworkState networkState) {
        for (Listener listener : listeners) {
            listener.onNetworkStateChanged(networkState);
        }
    }

    public interface Listener {
        void onNetworkStateChanged(NetworkState networkState);
    }
}
