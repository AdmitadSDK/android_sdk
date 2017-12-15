package ru.admitad.admitadsdk;

interface NetworkRepository {
    void log(AdmitadEvent admitadEvent, TrackerListener trackerListener);

    boolean isServerAvailable();
}
