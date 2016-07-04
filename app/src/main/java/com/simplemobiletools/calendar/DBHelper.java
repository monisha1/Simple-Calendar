package com.simplemobiletools.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.simplemobiletools.calendar.models.Event;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static SQLiteDatabase mDb;
    private static DBOperationsListener mCallback;

    private static final String DB_NAME = "events.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "events";
    private static final String COL_ID = "id";
    private static final String COL_START_TS = "start_ts";
    private static final String COL_END_TS = "end_ts";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";

    public static DBHelper newInstance(Context context, DBOperationsListener callback) {
        mCallback = callback;
        return new DBHelper(context);
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mDb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_START_TS + " INTEGER," +
                COL_END_TS + " INTEGER," +
                COL_TITLE + " TEXT," +
                COL_DESCRIPTION + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(Event event) {
        final ContentValues values = fillContentValues(event);
        mDb.insert(TABLE_NAME, null, values);
        mCallback.eventInserted();
    }

    public void update(Event event) {
        final ContentValues values = fillContentValues(event);
        final String selection = COL_ID + " = ?";
        final String[] selectionArgs = {String.valueOf(event.getId())};
        mDb.update(TABLE_NAME, values, selection, selectionArgs);
        mCallback.eventUpdated();
    }

    private ContentValues fillContentValues(Event event) {
        final ContentValues values = new ContentValues();
        values.put(COL_START_TS, event.getStartTS());
        values.put(COL_END_TS, event.getEndTS());
        values.put(COL_TITLE, event.getTitle());
        values.put(COL_DESCRIPTION, event.getDescription());
        return values;
    }

    public void deleteEvent(int id) {
        final String selection = COL_ID + " = ?";
        final String[] selectionArgs = {String.valueOf(id)};
        mDb.delete(TABLE_NAME, selection, selectionArgs);
        mCallback.eventsDeleted();
    }

    public void deleteEvents(String[] ids) {
        final String selection = COL_ID + " IN (?)";
        mDb.delete(TABLE_NAME, selection, ids);
        mCallback.eventsDeleted();
    }

    public void getEvents(int fromTS, int toTS) {
        final String[] projection = {COL_ID, COL_START_TS, COL_END_TS, COL_TITLE, COL_DESCRIPTION};
        List<Event> events = new ArrayList<>();
        final String selection = COL_START_TS + " <= ? AND " + COL_END_TS + " >= ?";
        final String[] selectionArgs = {String.valueOf(toTS), String.valueOf(fromTS)};
        final Cursor cursor = mDb.query(TABLE_NAME, projection, selection, selectionArgs, null, null, COL_START_TS);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                    final int startTS = cursor.getInt(cursor.getColumnIndex(COL_START_TS));
                    final int endTS = cursor.getInt(cursor.getColumnIndex(COL_END_TS));
                    final String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                    final String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
                    events.add(new Event(id, startTS, endTS, title, description));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        mCallback.gotEvents(events);
    }

    public interface DBOperationsListener {
        void eventInserted();

        void eventUpdated();

        void eventsDeleted();

        void gotEvents(List<Event> events);
    }
}