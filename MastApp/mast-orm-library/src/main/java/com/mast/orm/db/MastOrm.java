package com.mast.orm.db;

import android.content.Context;
import android.database.Cursor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sathish-n on 17/7/16.
 */

public class MastOrm {

    private static MastOrm mastOrm = null;

    private MastDbHelper mastDbHelper = null;

    public static MastOrm initialize(Context context) {
        if (mastOrm == null) {
            mastOrm = new MastOrm();
            mastOrm.initializeDb(context);
        }
        return mastOrm;
    }

    public static MastOrm getInstance() {
        if (mastOrm == null) {
            mastOrm = new MastOrm();
            if (mastOrm.getDb() == null) {
                new IllegalStateException("Database is Not Initialized Yet.. Please initialize by calling initialize(Context context) method");
            }
        }
        return mastOrm;
    }

    private void initializeDb(Context context) {
        mastDbHelper = new MastDbHelper(context);
    }

    private MastDbHelper getDb() {
        return mastDbHelper;
    }

    public void checkSchemaChange(String table_name, HashMap<String, String> columnsWithDataTypes) {
        Cursor dbCursor = mastDbHelper.getExecReadableDatabase().query(table_name, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        HashMap<String, Integer> temphashMap = new HashMap<>();
        int i = 0;
        for (String columnName : columnNames) {
            temphashMap.put(columnName, i);
            i++;
        }
        if (columnsWithDataTypes != null && columnNames.length > 0) {
            if (dbCursor.getColumnCount() != columnsWithDataTypes.size()) {
                if (columnsWithDataTypes.size() > dbCursor.getColumnCount()) {
                    StringBuilder alterTableString = new StringBuilder("ALTER TABLE " + table_name + " ADD COLUMN ");
                    Iterator it = columnsWithDataTypes.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String columnName = (String) pair.getKey();
                        if (!temphashMap.containsKey(columnName)) {
                            if (((String) pair.getValue()).contentEquals("Integer")) {
                                alterTableString.append(columnName + " INTEGER DEFAULT 0");
                            } else {
                                alterTableString.append(columnName);
                            }

                            mastDbHelper.getExecWritableDatabase().execSQL(alterTableString.toString());

                        }


                    }
                } else {

                }
            }
        }
        mastDbHelper.closeDB();
    }

    public void executeWrite(String rawQuery, String[] selectionArgs) {
        mastDbHelper.getExecWritableDatabase().execSQL(rawQuery);
        mastDbHelper.closeDB();
    }

    public Cursor executeRead(String rawQuery, String[] selectionArgs) {
        return mastDbHelper.getExecReadableDatabase().rawQuery(rawQuery, selectionArgs);
    }
}
