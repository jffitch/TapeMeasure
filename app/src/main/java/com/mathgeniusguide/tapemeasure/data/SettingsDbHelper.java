package com.mathgeniusguide.tapemeasure.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by grenade on 10/22/2017.
 */

public class SettingsDbHelper extends SQLiteOpenHelper {
    private static final String START_TABLE = "CREATE TABLE settings (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, target TEXT NOT NULL, value REAL DEFAULT 0)";

    public SettingsDbHelper(Context context) {
        super(context, "settings.db", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(START_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}