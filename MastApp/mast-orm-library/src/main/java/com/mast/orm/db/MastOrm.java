package com.mast.orm.db;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by sathish-n on 17/7/16.
 */

public class MastOrm {

    private static MastOrm mastOrm = null;

    private MastDbHelper mastDbHelper =null;
    public static MastOrm initialize(Context context){
        if(mastOrm==null){
            mastOrm = new MastOrm();
           mastOrm.initializeDb(context);
        }
        return mastOrm;
    }

    public static MastOrm getInstance(){
        if(mastOrm==null){
            mastOrm = new MastOrm();
            if(mastOrm.getDb()==null){
                new IllegalStateException("Database is Not Initialized Yet.. Please initialize by calling initialize(Context context) method");
            }
        }
        return mastOrm;
    }

    private void initializeDb(Context context){
        mastDbHelper = new MastDbHelper(context);
    }

    private MastDbHelper getDb(){
        return mastDbHelper;
    }

    public void executeWrite(String rawQuery, String[] selectionArgs){
        mastDbHelper.getExecWritableDatabase().execSQL(rawQuery);
        mastDbHelper.closeDB();
    }

    public Cursor executeRead(String rawQuery, String[] selectionArgs){
        return mastDbHelper.getExecReadableDatabase().rawQuery(rawQuery,selectionArgs);
    }
}
