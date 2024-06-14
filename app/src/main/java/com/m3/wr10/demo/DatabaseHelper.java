package com.m3.wr10.demo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase";
    private static final int DATABASE_VERSION = 1;

    // 테이블 생성 쿼리
    private static final String CREATE_TABLE = "CREATE TABLE mytable (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE, " +
            "age INTEGER);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 업그레이드가 필요한 경우 처리
    }
}

