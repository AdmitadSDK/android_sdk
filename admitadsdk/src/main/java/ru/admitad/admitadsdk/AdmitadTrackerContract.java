package ru.admitad.admitadsdk;

import android.provider.BaseColumns;

class AdmitadTrackerContract {

    AdmitadTrackerContract() {}

    public static abstract class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "AdmitadEvent";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_PARAMS = "param";
    }
}
