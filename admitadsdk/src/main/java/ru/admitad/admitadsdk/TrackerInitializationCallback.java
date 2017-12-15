package ru.admitad.admitadsdk;

public interface TrackerInitializationCallback {
    void onInitializationSuccess();

    void onInitializationFailed(Exception exception);
}
