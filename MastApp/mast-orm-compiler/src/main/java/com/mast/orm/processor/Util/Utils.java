package com.mast.orm.processor.Util;

import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;


import static java.lang.Class.forName;
import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * Created by sathish-n on 16/7/16.
 */

public class Utils {

    public static String getPackageName(String packageName) {
        int index = packageName.lastIndexOf(".");
        System.out.println(TAG + packageName + " index  " + index);
        if (index != -1) {
            String sub = packageName.substring(index);
            if (sub.contains("js2p")) {
                String newPackageName = packageName.substring(0, index);
                System.out.println(TAG + newPackageName);
                return newPackageName;
            }
        }

        return packageName;
    }

    public static String getClassName(String packageName) {
        int index = packageName.lastIndexOf(".");
//        System.out.println(TAG + packageName + " index  " + index);
        if (index != -1) {
            String newPackageName = packageName.substring(index+1);
//            System.out.println(TAG + newPackageName);
            return newPackageName;
        }

        return packageName;
    }

    public static String getClassName(String packageName, TypeKind typeKind) {
        if (typeKind != DECLARED && typeKind != ARRAY) {
            int index = packageName.lastIndexOf(".");
//            System.out.println(TAG + packageName + " index  " + index);
            if (index != -1) {
                String newPackageName = packageName.substring(index+1);
//                System.out.println(TAG + newPackageName);
                return newPackageName;
            }
        }
        return packageName;
    }

    public static String toString(TypeMirror mirror, boolean usePrimitiveWrappers) {
        TypeKind kind = mirror.getKind();
        switch (kind) {
            case VOID:
                return "void";
            case DECLARED:
                Name paramType = ((TypeElement) ((DeclaredType) mirror).asElement()).getQualifiedName();

                List<? extends TypeMirror> typeArguments = ((DeclaredType) mirror).getTypeArguments();
                if (typeArguments.size() == 0)
                    return getClassName(paramType.toString());
                else {
                    StringBuilder buff = new StringBuilder(paramType).append('<');
                    for (TypeMirror typeArgument : typeArguments)
                        buff.append(toString(typeArgument, false));
                    return buff.append('>').toString();
                }
            case INT:
                return usePrimitiveWrappers ? Integer.class.getName() : getClassName(kind.toString().toLowerCase());
            case CHAR:
                return usePrimitiveWrappers ? Character.class.getName() : getClassName(kind.toString().toLowerCase());
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
            case LONG:
            case SHORT:
            case BYTE:
                String name = kind.toString().toLowerCase();
                if (usePrimitiveWrappers)
                    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
                else {

                    return getClassName(name);
                }
            case ARRAY:
                return toString(((ArrayType) mirror).getComponentType(), false) + "[]";
            default:
                throw new RuntimeException(kind + " is not implemented for " + mirror.getClass());
        }
    }

    public static String getSqlDataType(String dataTypeClass) {
        return classToSqlDataTypes.get(dataTypeClass);
    }

    private static HashMap<String, String> classToSqlDataTypes = new HashMap<>();

    static {
        classToSqlDataTypes.put("Integer", "INT");
        classToSqlDataTypes.put("String", "TEXT");
        classToSqlDataTypes.put("Boolean", "NUMERIC");

    }

    private static String TAG = Utils.class.getSimpleName();
}
