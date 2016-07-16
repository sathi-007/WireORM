package com.mast.orm.processor.Util;

import sun.rmi.runtime.Log;

/**
 * Created by sathish-n on 16/7/16.
 */

public class Utils {

    public static String getPackageName(String packageName){
        int index = packageName.lastIndexOf(".");
        System.out.println(TAG+packageName+" index  "+index);
        if(index!=-1) {
            String sub = packageName.substring(index);
            if(sub.contains("js2p")){
                String newPackageName = packageName.substring(0,index);
                System.out.println(TAG+newPackageName);
                return newPackageName;
            }
        }

        return packageName;
    }

    private static String TAG = Utils.class.getSimpleName();
}
