package com.mast.orm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sathish-n on 17/7/16.
 */

public class MastDbHelper extends SQLiteOpenHelper {

    public MastDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion){

        }
    }

    public SQLiteDatabase getExecReadableDatabase() {
        return this.getReadableDatabase();
    }


    public SQLiteDatabase getExecWritableDatabase() {
        return this.getWritableDatabase();
    }

    public void closeDB(){
        this.close();
    }
    // Logcat tag
    private static final String LOG = MastDbHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "mast-orm";

}
