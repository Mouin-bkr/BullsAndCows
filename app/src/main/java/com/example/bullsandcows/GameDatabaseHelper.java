package com.example.bullsandcows;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "game.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_SCORES = "scores";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_SCORE = "score";
    public static final String COL_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_SCORES =
            "CREATE TABLE " + TABLE_SCORES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT, " +
                    COL_SCORE + " INTEGER, " +
                    COL_TIMESTAMP + " TEXT);";

    public GameDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCORES);
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT, timestamp TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "username TEXT, " +
                "password TEXT, " + // optional if you store it
                "timestamp TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }
}
