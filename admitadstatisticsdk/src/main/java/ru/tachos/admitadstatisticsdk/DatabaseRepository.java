package ru.tachos.admitadstatisticsdk;

import java.util.List;

interface DatabaseRepository {
    void insertOrUpdate(AdmitadEvent event);

    void remove(long id);

    List<AdmitadEvent> findAll();

    void findAllAsync(Callback<List<AdmitadEvent>> trackerListener);
}
