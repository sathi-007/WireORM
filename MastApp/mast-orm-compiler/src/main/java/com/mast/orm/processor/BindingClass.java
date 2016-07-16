package com.mast.orm.processor;

import com.mast.orm.processor.Util.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by sathish-n on 16/7/16.
 */

public class BindingClass {

    private final ClassName generatedClassName;

    private static final ClassName CrudOperations = ClassName.get("com.mast.orm.db", "CrudOperations");

    public BindingClass(String tableName, ClassName generatedClassName) {
        this.tableName = tableName.toLowerCase();
        this.generatedClassName = generatedClassName;
    }

    public void addColumn(String columnName, TypeMirror valueType) {
        columnsWithDataTypes.put(columnName, valueType);
    }

    public JavaFile brewJava() {
//        MethodSpec hexDigit = MethodSpec.methodBuilder("addActivityFilter")
//                .returns(void.class)
//                .addStatement("return (char) (i < 10 ? i + '0' : i - 10 + 'a')")
//                .build();



        String packageName = Utils.getPackageName(generatedClassName.packageName());


        ClassName classFqcn = ClassName.get(packageName,
                generatedClassName.simpleName());




        FieldSpec activityFilter = FieldSpec.builder(listOfHoverboards, "activityFilter")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", arrayList)
                .build();

        FieldSpec methodFilter = FieldSpec.builder(listOfHoverboards, "methodList")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", arrayList)
                .build();

        FieldSpec android = FieldSpec.builder(String.class, "tableName")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$S", tableName)
                .build();



        FieldSpec clazz = FieldSpec.builder(classFqcn, "instance")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
//
//        MethodSpec.Builder beyondBuilder = MethodSpec.methodBuilder("addMethodList")
//                .returns(void.class);
//
//        for (String methodName : methodNames) {
//            beyondBuilder.addStatement("$N.add($S)", methodFilter, methodName);
//        }

//        MethodSpec beyondMethod = beyondBuilder.build();

        MethodSpec.Builder addActivityFilterBuilder = MethodSpec.methodBuilder("addActivityFilter")
                .returns(void.class);

//        for (String methodName : trackerList) {
//            addActivityFilterBuilder.addStatement("$N.add($S)", activityFilter, methodName);
//        }

        MethodSpec activityFilterMethod = addActivityFilterBuilder.build();

        MethodSpec getScreenName = MethodSpec.methodBuilder("getScreenName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $N", android)
                .build();

//        MethodSpec getFilterList = MethodSpec.methodBuilder("getFilterList")
//                .addModifiers(Modifier.PUBLIC)
//                .returns(listOfHoverboards)
//                .addStatement("return $N", activityFilter)
//                .build();

        MethodSpec isMethodExists = MethodSpec.methodBuilder("isMethodExists")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(String.class, "methodName")
                .addStatement("boolean flag = false")
                .beginControlFlow("for (int i = $L; i < $N.size(); i++)", 0, methodFilter)
                .addStatement("flag = $N.get(i).toLowerCase().contentEquals($N.toLowerCase())", methodFilter, "methodName")
                .beginControlFlow("if(flag)")
                .addStatement("return true")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return flag")
                .build();

        MethodSpec flux = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("addActivityFilter()")
//                .addStatement("addMethodList()")
                .build();

        MethodSpec loadFunc = MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .returns(classFqcn)
                .beginControlFlow("if($N == null)", clazz)
                .addStatement("new $T()", classFqcn)
                .addStatement("addValueTypes()")
                .addStatement("addSqlDataTypes()")
                .addStatement("createTable()")
                .endControlFlow()
                .addStatement("return $N", clazz)
                .build();

        MethodSpec.Builder addValuesTypesFuncBuilder = MethodSpec.methodBuilder("addValueTypes")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        writeColumValueTypes(addValuesTypesFuncBuilder, columnTypeMap);

        MethodSpec addValuesTypesFunc = addValuesTypesFuncBuilder.build();

        MethodSpec.Builder addSqlTypesFuncBuilder = MethodSpec.methodBuilder("addSqlDataTypes")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        appendColumNameAndTypes(addSqlTypesFuncBuilder, columnSqlTypeMap);

        MethodSpec addSqlFunc = addSqlTypesFuncBuilder.build();

        MethodSpec createTabbleFunc = createTableFunc(columnTypeMap, columnSqlTypeMap);

        TypeSpec.Builder result = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(activityFilter)
                .addField(methodFilter)
                .addField(android)
                .addField(columnValueMap)
                .addField(columnTypeMap)
                .addField(columnSqlTypeMap)
                .addField(clazz)
                .addMethod(loadFunc)
                .addMethod(addValuesTypesFunc)
                .addMethod(addSqlFunc)
                .addMethod(createTabbleFunc)
//                .addMethod( beyondMethod)
//                .addMethod(getFilterList)
                .addMethod(activityFilterMethod)
                .addMethod(isMethodExists)
                .addMethod(getScreenName)
                .addMethod(flux);

        writeColumnIndividualSetters(result);
//        TypeName crudOperations = ParameterizedTypeName.get(CrudOperations);
//
//        result.addSuperinterface(crudOperations);

        return JavaFile.builder(Utils.getPackageName(generatedClassName.packageName()), result.build())
                .addFileComment("Generated code from Mast ORM. Do not modify!")
                .build();
    }

    private void writeColumValueTypes(MethodSpec.Builder builder, FieldSpec columnTypeMap) {
        if (builder != null && columnsWithDataTypes != null) {
            Iterator it = columnsWithDataTypes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                TypeMirror typeMirror = (TypeMirror) pair.getValue();
                String className = Utils.toString(typeMirror, false);
//                if(typeMirror.getKind()!= TypeKind.DECLARED)
                String decalredType = Utils.getSqlDataType(className);
                if (decalredType != null)
                    builder.addStatement("$N.put($S,$S)", columnTypeMap, pair.getKey(), className);
//                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    private MethodSpec createTableFunc(FieldSpec columnTypeMap, FieldSpec sqlTypeMap) {
        ClassName stringBuilder = ClassName.get("java.lang", "StringBuilder");
        ClassName iteratorType = ClassName.get("java.util", "Iterator");
        ClassName mapType = ClassName.get("java.util", "Map");
        String createTableString = "CREATE TABLE IF NOT EXISTS " + tableName + " ( ";
        MethodSpec.Builder createTableFuncBuilder = MethodSpec.methodBuilder("createTable")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T queryBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("queryBuilder.append($S)", createTableString)
                .addStatement("int size = $N.size()", columnTypeMap)
                .addStatement("int index=0")
                .addStatement("$T it = $N.entrySet().iterator()", iteratorType, columnTypeMap)
                .beginControlFlow(" while (it.hasNext())")
                .addStatement(" $T.Entry pair = ($T.Entry) it.next()", mapType, mapType)
                .addStatement("queryBuilder.append(pair.getKey()+\" \"+ $N.get(pair.getValue()))", sqlTypeMap)
                .beginControlFlow("if(index <size-1)")
                .addStatement("queryBuilder.append(\",\")")
                .endControlFlow()
                .addStatement("index++")
                .endControlFlow()
                .addStatement("queryBuilder.append(\")\")")
                .returns(void.class);
        return createTableFuncBuilder.build();
    }

    private void appendColumNameAndTypes(MethodSpec.Builder builder, FieldSpec columnTypeMap) {
        if (builder != null && columnsWithDataTypes != null) {
//            System.out.println(TAG + " "+tableName +" "+ columnsWithDataTypes.size());
            Iterator it = columnsWithDataTypes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
//                System.out.println(pair.getKey() + " = " + pair.getValue());
                TypeMirror typeMirror = (TypeMirror) pair.getValue();
                String className = Utils.toString(typeMirror, false);
//                System.out.println(TAG + " " + className);
                String decalredType = Utils.getSqlDataType(className);
                if (decalredType != null)
                    builder.addStatement("$N.put($S,$S)", columnTypeMap, className, decalredType);
//                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    private void writeColumnIndividualSetters(TypeSpec.Builder result) {
        if (result != null && columnsWithDataTypes != null) {
            Iterator it = columnsWithDataTypes.entrySet().iterator();
            String packageName = Utils.getPackageName(generatedClassName.packageName());


            ClassName classFqcn = ClassName.get(packageName,
                    generatedClassName.simpleName());
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                TypeMirror typeMirror = (TypeMirror) pair.getValue();
                String className = Utils.toString(typeMirror, false);
                System.out.println(TAG + " " + className);
                String decalredType = Utils.getSqlDataType(className);
                if (decalredType != null) {
                    FieldSpec android = null;
                    MethodSpec loadFunc = null;
                    String varName = (String) pair.getKey();
                    if (className.contentEquals("String")) {
                        android = FieldSpec.builder(String.class, varName)
                                .addModifiers(Modifier.PRIVATE)
                                .build();

                        loadFunc = MethodSpec.methodBuilder(varName)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(classFqcn)
                                .addParameter(String.class, "val")
                                .addStatement("$N.put($S, $N)", columnValueMap,varName ,"val")
                                .addStatement("return this")
                                .build();

                    } else if (className.contentEquals("Integer")) {
                        android = FieldSpec.builder(Integer.class, (String) pair.getKey())
                                .addModifiers(Modifier.PRIVATE)
                                .build();

                        loadFunc = MethodSpec.methodBuilder(varName)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(classFqcn)
                                .addParameter(Integer.class, "val")
//                                .addStatement("$N = $N", android, "val")
                                .addStatement("$N.put($S, $N)", columnValueMap,varName ,"val")
                                .addStatement("return this")
                                .build();

                    } else if (className.contentEquals("Boolean")) {
                        android = FieldSpec.builder(Boolean.class, (String) pair.getKey())
                                .addModifiers(Modifier.PRIVATE)
                                .build();

                        loadFunc = MethodSpec.methodBuilder(varName)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(classFqcn)
                                .addParameter(Boolean.class, "val")
//                                .addStatement("$N = $N", android, "val")
                                .addStatement("$N.put($S, $N)", columnValueMap,varName ,"val")
                                .addStatement("return this")
                                .build();
                    }

                    if (android != null) {
                        result.addField(android);
                    }
                    if (loadFunc != null) {
                        result.addMethod(loadFunc);
                    }

                } else {

                }
            }
        }
    }




    ClassName string = ClassName.get("java.lang", "String");
    ClassName integer = ClassName.get("java.lang", "Integer");
    ClassName bool = ClassName.get("java.lang", "Boolean");
    ClassName hashSet = ClassName.get("java.util", "HashMap");
    TypeName declaredSet = ParameterizedTypeName.get(hashSet, string,
            ClassName.get("java.lang", "Object"));

    TypeName columnValueTypeName = ParameterizedTypeName.get(hashSet, string,
            string);
    FieldSpec columnValueMap = FieldSpec.builder(declaredSet, "columnValueMap")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();

    FieldSpec columnTypeMap = FieldSpec.builder(columnValueTypeName, "columnTypeMap")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();

    FieldSpec columnSqlTypeMap = FieldSpec.builder(columnValueTypeName, "columnSqlTypeMap")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();
    ClassName list = ClassName.get("java.util", "List");
    ClassName arrayList = ClassName.get("java.util", "ArrayList");
    TypeName listOfHoverboards = ParameterizedTypeName.get(list, string);
    private HashMap<String, TypeMirror> columnsWithDataTypes = new HashMap<>();
    private String tableName;
    private final String TAG = BindingClass.class.getSimpleName();
}
