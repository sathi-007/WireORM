package com.mast.orm.db;

/**
 * Created by sathish-n on 29/7/16.
 */

public class BindClass {
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(Object objectValue) {
        this.objectValue = objectValue;
    }

    String className;

    Object objectValue;

}
