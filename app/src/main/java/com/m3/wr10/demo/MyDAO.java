package com.m3.wr10.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class MyDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public MyDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

//    public void open() throws SQLException {
//        database = dbHelper.getWritableDatabase();
//    }

/*    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        if (database == null) {
            throw new SQLException("Failed to open database");
        }
    }*/


    public void close() {
        dbHelper.close();
    }

    // 데이터 추가


    // 모든 데이터 조회
    public Cursor getAllData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cTemp = db.query("mytable", null, null, null, null, null, null);
        db.close();
        return cTemp;
    }

    // 특정 데이터 조회
    public Cursor getDataById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cTemp = db.query("mytable", null, "_id=" + id, null, null, null, null);
        db.close();
        return cTemp;
    }

    // 데이터 수정
    public int renameName( String oldName, String newName ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName );

        int nTemp = db.update( "mytable", values, "name" + "=?", new String[]{oldName});

        db.close();
        return nTemp;
    }

    // 데이터 수정
    public int updateData(long id, String name, int age) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);

        int nTemp = db.update("mytable", values, "_id=" + id, null);

        db.close();
        return nTemp;
    }

    public void init() {
        insertData("DEFAULT", 1000);
    }

    public long insertData(String name, int age) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);

        long nTemp = db.insert("mytable", null, values);

        db.close();
        return nTemp;
    }

    // 데이터 삭제
    public int deleteData(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int nTemp = db.delete("mytable", "_id=" + id, null);
        db.close();;
        return nTemp;
    }

    public int getCount() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + "mytable", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        db.close();
        return count;
    }

    //////////////////////////////////////////////////////////////////

    public void cloneFruit(String fruitName) {
        String copiedFruitName = fruitName + "_copy";
        addFruit( copiedFruitName );
    }

    // 새로운 과일 추가
    public void addFruit(String fruitName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", fruitName);

        db.insert("mytable", null, values);
        db.close();
    }


    public void deleteFruit(String fruitName) {
        Log.d("M3Mobile", "==deleteFruit::" + fruitName + "==" );

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("mytable", "name" + "=?", new String[]{fruitName});
        db.close();
    }

    public ArrayList<String> getAllFruits() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> fruits = new ArrayList<>();

        Cursor cursor = db.query("mytable", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String fruitName = cursor.getString(cursor.getColumnIndex("name"));
            fruits.add(fruitName);
        }

        cursor.close();
        db.close();
        return fruits;
    }
}

