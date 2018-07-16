package com.laguna.university.homeappliancecontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SQLiteDBcontroller extends SQLiteOpenHelper{

    ArrayList<Integer> light1 = new ArrayList<>();
    ArrayList<Integer> light2 = new ArrayList<>();
    ArrayList<Integer> light3 = new ArrayList<>();
    ArrayList<Integer> light4 = new ArrayList<>();
    ArrayList<Integer> light5 = new ArrayList<>();
    ArrayList<Integer> outlet1 = new ArrayList<>();
    ArrayList<Integer> outlet2 = new ArrayList<>();
    ArrayList<String> dn = new ArrayList<>();
    ArrayList<String> numberList = new ArrayList<>();
    ArrayList<String> username = new ArrayList<>();
    ArrayList<String> password = new ArrayList<>();

    public SQLiteDBcontroller(Context context) {
        super(context, "SPALU.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE setting( light1 INTEGER, light2 INTEGER, light3 INTEGER, light4 INTEGER, light5 INTEGER, outlet1 INTEGER, outlet2 INTEGER );");
        sqLiteDatabase.execSQL("CREATE TABLE deviceName( device TEXT, value TEXT );");
        sqLiteDatabase.execSQL("CREATE TABLE arduinoNumber( value TEXT );");
        sqLiteDatabase.execSQL("CREATE TABLE account( username TEXT, password TEXT );");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST setting;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST deviceName;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST arduinoNumber;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST account;");
        onCreate(sqLiteDatabase);
    }

    //setting TABLE
    public void insertSetting(int light1, int light2, int light3, int light4, int light5, int outlet1, int outlet2){
        ContentValues contentValues = new ContentValues();
        contentValues.put("light1",light1);
        contentValues.put("light2",light2);
        contentValues.put("light3",light3);
        contentValues.put("light4",light4);
        contentValues.put("light5",light5);
        contentValues.put("outlet1",outlet1);
        contentValues.put("outlet2",outlet2);
        this.getWritableDatabase().insertOrThrow("setting","",contentValues);
    }
    public void getSetting(){
        light1.clear(); light2.clear(); light3.clear(); light4.clear(); light5.clear(); outlet1.clear(); outlet2.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM setting",null);
        while (cursor.moveToNext()) {
            light1.add(cursor.getInt(0));
            light2.add(cursor.getInt(1));
            light3.add(cursor.getInt(2));
            light4.add(cursor.getInt(3));
            light5.add(cursor.getInt(4));
            outlet1.add(cursor.getInt(5));
            outlet2.add(cursor.getInt(6));
        }
    }
    public void updateSetting(String table, int value){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE setting set '"+table+"' = "+value,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //nameDevice TABLE
    public void saveName(String whatToRename, String value){
        ContentValues contentValues = new ContentValues();
        contentValues.put("device",whatToRename);
        contentValues.put("value",value);
        this.getWritableDatabase().insertOrThrow("deviceName","",contentValues);
    }
    public void updateDeviceName(String deviceName, String value){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE deviceName set value = '"+value+"' WHERE device = '"+deviceName+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getDeviceName(String name){
        dn.clear();
        name.toLowerCase();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT value FROM deviceName WHERE device = '"+name+"'",null);
        while (cursor.moveToNext()) {
            dn.add(cursor.getString(0));
        }
    }

    //number table
    public void saveNumber(String number){
        ContentValues contentValues = new ContentValues();
        contentValues.put("value",number);
        this.getWritableDatabase().insertOrThrow("arduinoNumber","",contentValues);
    }
    public void updateNumber(String number){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE arduinoNumber set value = '"+number+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getNumber(){
        numberList.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM arduinoNumber",null);
        while (cursor.moveToNext()) {
            numberList.add(cursor.getString(0));
        }
        cursor.close();
    }

    //account table
    public void saveAccount(String user, String pass){
        ContentValues contentValues = new ContentValues();
        contentValues.put("username",user);
        contentValues.put("password",pass);
        this.getWritableDatabase().insertOrThrow("account","",contentValues);
    }
    public void updateAccount(String user, String pass){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE account set username = '"+user+"', password = '"+pass+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getAccount(){
        username.clear();
        password.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM account",null);
        while (cursor.moveToNext()) {
            username.add(cursor.getString(0));
            password.add(cursor.getString(1));
        }
        cursor.close();
    }
}