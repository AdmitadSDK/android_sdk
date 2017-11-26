package ru.tachos.admitadstatisticsdk;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdmitadOrder {
    private final Map<String, String> params;

    private AdmitadOrder(Map<String, String> mainParams) {
        this.params = mainParams;
    }

    public AdmitadEvent toEvent(@AdmitadEvent.Type int type) {
        return new AdmitadEvent(type, params);
    }

    public static class Item {
        private final static String FIELD_ITEM_ID = "id";
        private final static String FIELD_ITEM_NAME = "name";
        private final static String FIELD_ITEM_QUANTITY = "quantity";

        private final JSONObject jsonObject = new JSONObject();

        public Item(@Nullable String id, @NonNull String name, @IntRange(from = 1) int quantity) {
            try {
                if (!TextUtils.isEmpty(id)) {
                    jsonObject.put(FIELD_ITEM_ID, id);
                }
                if (!TextUtils.isEmpty(name)) {
                    jsonObject.put(FIELD_ITEM_NAME, name);
                }
                try {
                    jsonObject.put(FIELD_ITEM_QUANTITY, quantity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
            }
        }
    }

    public static class UserInfo {
        private JSONObject jsonObject;

        public UserInfo() {
            jsonObject = new JSONObject();
        }

        public UserInfo(@NonNull Map<String, String> params) {
            jsonObject = new JSONObject(params);
        }

        public UserInfo putExtra(@NonNull String key, String value) {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    public static class Builder {
        private final static String FIELD_ID = "oid";
        private final static String FIELD_TOTAL_PRICE = "price";
        private final static String FIELD_CURRENCY_CODE = "currency_code";
        private final static String FIELD_JSON = "json";
        private final static String FIELD_USER_INFO = "user_info";
        private final static String FIELD_ITEMS = "items";

        private Map<String, String> mainParams = new HashMap<>();
        private List<Item> items = new ArrayList<>();
        private UserInfo userInfo;

        public Builder(@NonNull String id, @NonNull String totalPrice) {
            mainParams.put(FIELD_ID, id);
            mainParams.put(FIELD_TOTAL_PRICE, totalPrice);
        }

        public Builder setCurrencyCode(@NonNull String currencyCode) {
            mainParams.put(FIELD_CURRENCY_CODE, currencyCode);
            return this;
        }

        public Builder setUserInfo(@NonNull UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        public Builder putItem(@NonNull Item item) {
            items.add(item);
            return this;
        }

        public AdmitadOrder build() {
            JSONObject json = new JSONObject();
            if (userInfo != null) {
                try {
                    json.put(FIELD_USER_INFO, userInfo.jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (items != null && items.size() > 0) {
                JSONArray itemsArray = new JSONArray();
                for (Item item : items) {
                    itemsArray.put(item.jsonObject);
                }
                try {
                    json.put(FIELD_ITEMS, itemsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mainParams.put(FIELD_JSON, json.toString());
            return new AdmitadOrder(mainParams);
        }
    }
}
