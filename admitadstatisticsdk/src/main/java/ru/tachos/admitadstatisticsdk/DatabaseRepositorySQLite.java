package ru.tachos.admitadstatisticsdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

class DatabaseRepositorySQLite implements DatabaseRepository {

    private TrackerDatabaseHelper dbHelper;
    private final static String[] projection = {
            AdmitadTrackerContract.TrackEntry._ID,
            AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE,
            AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS
    };
    private final static String sortOrder = AdmitadTrackerContract.TrackEntry._ID + " ASC";
    private Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public DatabaseRepositorySQLite(Context context) {
        dbHelper = new TrackerDatabaseHelper(context);
    }

    @Override
    public void insertOrUpdate(final AdmitadEvent event) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                final SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.beginTransaction();
                event.id = database.insertWithOnConflict(AdmitadTrackerContract.TrackEntry.TABLE_NAME, null, parse(event), SQLiteDatabase.CONFLICT_REPLACE);
                database.setTransactionSuccessful();
                database.endTransaction();
            }
        });
    }

    @Override
    public void remove(final long id) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.beginTransaction();
                String selection = AdmitadTrackerContract.TrackEntry._ID + " LIKE ?";
                String[] selectionArgs = {String.valueOf(id)};
                database.delete(AdmitadTrackerContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
            }
        });
    }

    @Override
    public List<AdmitadEvent> findAll() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(AdmitadTrackerContract.TrackEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
        final List<AdmitadEvent> events = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                events.add(parse(cursor));
            } while (cursor.moveToNext());
        }
        return events;
    }

    @Override
    public void findAllAsync(final Callback<List<AdmitadEvent>> trackerListener) {
        final Handler uiHandler = new Handler();
        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<AdmitadEvent> events = findAll();
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        trackerListener.onSuccess(events);
                    }
                });
            }
        }).start();
    }

    public static ContentValues parse(AdmitadEvent event) {
        final ContentValues contentValues = new ContentValues();
        if (event.id > 0) {
            contentValues.put(AdmitadTrackerContract.TrackEntry._ID, event.id);
        }
        contentValues.put(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE, event.type);
        contentValues.put(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS, new JSONObject(event.params).toString());
        return contentValues;
    }

    public static AdmitadEvent parse(Cursor cursor) {
        String dbParams = cursor.getString(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS));
        long dbId = cursor.getLong(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry._ID));
        int dbType = cursor.getInt(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE));
        Map<String, String> params = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(dbParams);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                params.put(key, jsonObject.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        AdmitadEvent event = new AdmitadEvent(dbType, params);
        event.id = dbId;
        return event;
    }
}
