package ru.tachos.admitadstatistic;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ru.tachos.admitadstatisticsdk.AdmitadEvent;
import ru.tachos.admitadstatisticsdk.AdmitadOrder;
import ru.tachos.admitadstatisticsdk.AdmitadTracker;
import ru.tachos.admitadstatisticsdk.TrackerInitializationCallback;
import ru.tachos.admitadstatisticsdk.TrackerListener;

public class MainActivity extends AppCompatActivity implements TrackerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdmitadTracker.initialize(getApplicationContext(), "TestAndroidPostback", new TrackerInitializationCallback() {
            @Override
            public void onInitializationSuccess() {
                AdmitadTracker.getInstance().handleDeeplink(Uri.parse("schema://host?uid=TestUidAndroid"));
            }

            @Override
            public void onInitializationFailed(Exception exception) {

            }
        });
    }

    @Override
    public void onSuccess(AdmitadEvent result) {

    }

    @Override
    public void onFailure(int errorCode, @Nullable String errorText) {

    }

    public void registrationClick(View v) {
        AdmitadTracker.getInstance().logRegistration("TestRegistrationUid");
    }

    public void orderClick(View v) {
        final AdmitadOrder order = new AdmitadOrder.Builder("123", "100.00")
                .setCurrencyCode("RUB")
                .putItem(new AdmitadOrder.Item("Item1", "ItemName1", 3))
                .putItem(new AdmitadOrder.Item("Item2", "ItemName2", 5))
                .setUserInfo(new AdmitadOrder.UserInfo().putExtra("Surname", "Kek").putExtra("Age", "10"))
                .build();
        AdmitadTracker.getInstance().logOrder(order);
    }

    public void purchaseClick(View v) {
        final AdmitadOrder order = new AdmitadOrder.Builder("321", "1756.00")
                .setCurrencyCode("USD")
                .putItem(new AdmitadOrder.Item("Item1", "ItemName1", 7))
                .putItem(new AdmitadOrder.Item("Item2", "ItemName2", 8))
                .setUserInfo(new AdmitadOrder.UserInfo().putExtra("Name", "Keksel").putExtra("Age", "1430"))
                .build();
        AdmitadTracker.getInstance().logPurchase(order);
    }

    public void returnClick(View v) {
        AdmitadTracker.getInstance().logUserReturn("TestReturnUserUid", 5);
    }

    public void loyaltyClick(View v) {
        AdmitadTracker.getInstance().logUserLoyalty("TestUserLoyaltyUid", 10);
    }

    public void manyEventsQueue(View v) {
        for (int i = 0; i < 100; i++) {
            AdmitadTracker.getInstance().logRegistration("userRegistration" + i);
            AdmitadTracker.getInstance().logUserLoyalty("userLoyalty" + i, i);
        }
    }
}
