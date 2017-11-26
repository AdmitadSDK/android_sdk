package ru.tachos.admitadstatisticsdk;

public interface Callback<T> {
    void onSuccess(T result);

    void onFailure(int errorCode, String errorText);
}
