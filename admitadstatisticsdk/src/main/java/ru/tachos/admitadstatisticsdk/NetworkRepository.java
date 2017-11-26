package ru.tachos.admitadstatisticsdk;

interface NetworkRepository {
    void log(AdmitadEvent admitadEvent, TrackerListener trackerListener);

    boolean isServerAvailable();
}
