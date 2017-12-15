package ru.admitad.admitadsdk;

public interface Callback<T> {
    void onSuccess(T result);

    void onFailure(int errorCode, String errorText);
}
