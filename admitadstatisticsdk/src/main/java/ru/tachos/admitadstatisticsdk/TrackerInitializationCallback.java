package ru.tachos.admitadstatisticsdk;

public interface TrackerInitializationCallback {
    void onInitializationSuccess();

    void onInitializationFailed(Exception exception);
}
