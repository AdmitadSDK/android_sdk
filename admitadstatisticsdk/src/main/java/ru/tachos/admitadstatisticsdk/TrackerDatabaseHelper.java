package ru.tachos.admitadstatisticsdk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class TrackerDatabaseHelper  extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "admitad_tacker.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AdmitadTrackerContract.TrackEntry.TABLE_NAME + " (" +
                    AdmitadTrackerContract.TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AdmitadTrackerContract.TrackEntry.TABLE_NAME;

    public TrackerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
