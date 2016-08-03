package com.mast.orm.db;

import android.database.Cursor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sathish-n on 25/7/16.
 */

public class Utils {

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz)
    {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        return ret;
    }

    public static <T> T getColumnValue(Class<?> clazz, Cursor cursor, int index) {
        if(clazz.equals(String.class)){
            return (T)cursor.getString(index);
        }else if(clazz.equals(Boolean.class)){
            return (T)(cursor.getInt(index)==1?new Boolean(true):new Boolean(false));
        }else if(clazz.equals(Integer.class)){
            return (T)new Integer(cursor.getInt(index));
        }else if(clazz.equals(Long.class)){
            return (T)new Long(cursor.getLong(index));
        }else if(clazz.equals(Float.class)){
            return (T)new Float(cursor.getFloat(index));
        }else if(clazz.equals(Double.class)){
            return (T)new Double(cursor.getDouble(index));
        }
        return (T)cursor.getString(index);
    }
}
