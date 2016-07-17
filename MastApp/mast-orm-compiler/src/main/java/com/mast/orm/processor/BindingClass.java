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

    public void addFunction(String columnName, String valueType) {
        columnsWithFunctionNames.put(columnName, valueType);
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
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classFqcn)
                .beginControlFlow("if($N == null)", clazz)
                .addStatement("$N = new $T()", clazz, classFqcn)
                .addStatement("$N.$N = $T.getInstance()", clazz, mastOrmField, mastOrm)
                .addStatement("$N.addValueTypes()", clazz)
                .addStatement("$N.addSqlDataTypes()", clazz)
                .addStatement("$N.createTable()", clazz)
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
                .addField(mastOrmField)
                .addField(columnUpdateWhereValueMap)
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

        MethodSpec saveMethodSpec = saveTableFunc();
        MethodSpec whereMethodSpec = whereMethodSpec();
        MethodSpec updateMethodSpec = updateMethodSpec();
        ;
        TypeSpec enumSpec = generateEnum();

//        MethodSpec updateMethodSpec = updateTableFunc();

        result.addType(enumSpec);
        result.addMethod(saveMethodSpec);
        result.addMethod(whereMethodSpec);
        result.addMethod(updateMethodSpec);
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
                .addStatement("$N.executeWrite(queryBuilder.toString(),null)", mastOrmField)
                .returns(void.class);
        return createTableFuncBuilder.build();
    }

    private MethodSpec saveTableFunc() {
        String insertIntoString = "INSERT INTO " + tableName + " ( ";
        String valuesString = "VALUES (";
        MethodSpec.Builder createTableFuncBuilder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T insertIntoBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertIntoBuilder.append($S)", insertIntoString)
                .addStatement("$T insertValueBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertValueBuilder.append($S)", valuesString)
                .addStatement("int size = $N.size()", columnValueMap)
                .addStatement("int index=0")
                .addStatement("$T it = $N.entrySet().iterator()", iteratorType, columnValueMap)
                .beginControlFlow(" while (it.hasNext())")
                .addStatement(" $T.Entry pair = ($T.Entry) it.next()", mapType, mapType)
                .addStatement("insertIntoBuilder.append(pair.getKey())")
                .beginControlFlow("if($N.get(pair.getKey()).contentEquals(\"String\"))", columnTypeMap)
                .addStatement("insertValueBuilder.append(\"'\"+pair.getValue()+\"'\")")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("insertValueBuilder.append(pair.getValue())")
                .endControlFlow()
                .beginControlFlow("if(index <size-1)")
                .addStatement("insertIntoBuilder.append(\",\")")
                .addStatement("insertValueBuilder.append(\",\")")
                .endControlFlow()
                .addStatement("index++")
                .endControlFlow()
                .addStatement("insertIntoBuilder.append(\")\")")
                .addStatement("insertValueBuilder.append(\")\")")
                .addStatement("insertIntoBuilder.append(\" \"+ insertValueBuilder)")
                .addStatement("$N.executeWrite(insertIntoBuilder.toString(),null)", mastOrmField)
                .addStatement("$N.clear()", columnValueMap)
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
//            String packageName = Utils.getPackageName(generatedClassName.packageName());
//
//
//            ClassName classFqcn = ClassName.get(packageName,
//                    generatedClassName.simpleName());
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                TypeMirror typeMirror = (TypeMirror) pair.getValue();
                String className = Utils.toString(typeMirror, false);
                System.out.println(TAG + " " + className);
                String decalredType = Utils.getSqlDataType(className);
                if (decalredType != null) {
                    String varName = (String) pair.getKey();
                    if (className.contentEquals("String")) {
                        createFieldAndMethod(String.class, result, varName);

                    } else if (className.contentEquals("Integer")) {
                        createFieldAndMethod(Integer.class, result, varName);

                    } else if (className.contentEquals("Boolean")) {
                        createFieldAndMethod(Boolean.class, result, varName);
                    }

                } else {

                }
            }
        }
    }

    private TypeSpec generateEnum() {
        TypeSpec.Builder tableEnum = TypeSpec.enumBuilder("COLUMNS")
                .addModifiers(Modifier.PUBLIC);
        Iterator it = columnsWithDataTypes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            TypeMirror typeMirror = (TypeMirror) pair.getValue();
            String className = Utils.toString(typeMirror, false);
            String decalredType = Utils.getSqlDataType(className);
            if (decalredType != null) {
                String varName = (String) pair.getKey();
                tableEnum.addEnumConstant(varName.toUpperCase(), TypeSpec.anonymousClassBuilder("$S", varName)
                        .build());
            }
        }
        tableEnum.addField(String.class, "columnName", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(String.class, "columnName")
                        .addStatement("this.$N = $N", "columnName", "columnName")
                        .build());
        return tableEnum.build();
    }

    private MethodSpec whereMethodSpec() {
        String packageName = Utils.getPackageName(generatedClassName.packageName());
        ClassName enumClass = ClassName.get(generatedClassName.simpleName(),
                "COLUMNS");

        ClassName classFqcn = ClassName.get(packageName,
                generatedClassName.simpleName());
        MethodSpec createTableFuncBuilder = MethodSpec.methodBuilder("where")
                .addModifiers(Modifier.PUBLIC)
                .returns(classFqcn)
                .addParameter(enumClass, "whereColumnName")
                .addParameter(Object.class, "whereConditionValue")
                .addStatement("$N.put($N, $N)", columnUpdateWhereValueMap, "whereColumnName.columnName", "whereConditionValue")
                .addStatement("return this")
                .build();

        return createTableFuncBuilder;
    }

    private MethodSpec updateMethodSpec() {
        String updateString = "UPDATE " + tableName + "  SET ";
        String whereString = " WHERE ";
        MethodSpec.Builder updateTableFuncBuilder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T insertIntoBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertIntoBuilder.append($S)", updateString)
                .addStatement("$T insertValueBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertValueBuilder.append($S)", whereString)


                .addStatement("int size = $N.size()", columnValueMap)
                .addStatement("int index=0")
                .addStatement("$T it = $N.entrySet().iterator()", iteratorType, columnValueMap)
                .beginControlFlow(" while (it.hasNext())")
                .addStatement(" $T.Entry pair = ($T.Entry) it.next()", mapType, mapType)
                .beginControlFlow("if($N.get(pair.getKey()).contentEquals(\"String\"))", columnTypeMap)
                .addStatement("insertIntoBuilder.append(pair.getKey() +\"=\"+ \"'\"+pair.getValue()+\"'\")")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("insertIntoBuilder.append(pair.getKey() +\"=\"+pair.getValue())")
                .endControlFlow()
                .beginControlFlow("if(index <size-1)")
                .addStatement("insertIntoBuilder.append(\",\")")
                .endControlFlow()
                .addStatement("index++")
                .endControlFlow()


                .addStatement("int new_size = $N.size()", columnUpdateWhereValueMap)
                .addStatement("int new_index=0")
                .addStatement("$T itr = $N.entrySet().iterator()", iteratorType, columnUpdateWhereValueMap)
                .beginControlFlow(" while (itr.hasNext())")
                .addStatement(" $T.Entry pair = ($T.Entry) itr.next()", mapType, mapType)
                .beginControlFlow("if($N.get(pair.getKey()).contentEquals(\"String\"))", columnTypeMap)
                .addStatement("insertValueBuilder.append(pair.getKey() +\"=\"+ \"'\"+pair.getValue()+\"'\")")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("insertValueBuilder.append(pair.getKey() +\"=\"+pair.getValue())")
                .endControlFlow()
                .beginControlFlow("if(new_index <new_size-1)")
                .addStatement("insertValueBuilder.append(\",\")")
                .endControlFlow()
                .addStatement("new_index++")
                .endControlFlow()
                .addStatement("insertIntoBuilder.append(\" \"+ insertValueBuilder)")
                .addStatement("$N.executeWrite(insertIntoBuilder.toString(),null)", mastOrmField)
                .addStatement("$N.clear()", columnValueMap)
                .addStatement("$N.clear()", columnUpdateWhereValueMap)
                .returns(void.class);
        return updateTableFuncBuilder.build();
    }

    private <T> void createFieldAndMethod(Class<T> type, TypeSpec.Builder result, String varName) {
        String packageName = Utils.getPackageName(generatedClassName.packageName());


        ClassName classFqcn = ClassName.get(packageName,
                generatedClassName.simpleName());
        FieldSpec android = null;
        MethodSpec loadFunc = null;
        android = FieldSpec.builder(type, varName)
                .addModifiers(Modifier.PRIVATE)
                .build();

        loadFunc = MethodSpec.methodBuilder(varName)
                .addModifiers(Modifier.PUBLIC)
                .returns(classFqcn)
                .addParameter(type, "val")
                .addStatement("$N.put($S, $N)", columnValueMap, varName, "val")
                .addStatement("return this")
                .build();

        if (android != null) {
            result.addField(android);
        }
        if (loadFunc != null) {
            result.addMethod(loadFunc);
        }
    }


    ClassName mastOrm = ClassName.get("com.mast.orm.db", "MastOrm");
    ClassName stringBuilder = ClassName.get("java.lang", "StringBuilder");
    ClassName iteratorType = ClassName.get("java.util", "Iterator");
    ClassName mapType = ClassName.get("java.util", "Map");
    ClassName string = ClassName.get("java.lang", "String");
    ClassName integer = ClassName.get("java.lang", "Integer");
    ClassName bool = ClassName.get("java.lang", "Boolean");
    ClassName hashSet = ClassName.get("java.util", "HashMap");
    TypeName declaredSet = ParameterizedTypeName.get(hashSet, string,
            ClassName.get("java.lang", "Object"));

    TypeName columnValueTypeName = ParameterizedTypeName.get(hashSet, string,
            string);
    FieldSpec columnValueMap = FieldSpec.builder(declaredSet, "columnValueMap")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();

    FieldSpec columnUpdateWhereValueMap = FieldSpec.builder(declaredSet, "columnUpdateWhereValueMap")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
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



    FieldSpec mastOrmField = FieldSpec.builder(mastOrm, "mastOrm")
            .addModifiers(Modifier.PRIVATE)
            .build();

    ClassName list = ClassName.get("java.util", "List");
    ClassName arrayList = ClassName.get("java.util", "ArrayList");
    TypeName listOfHoverboards = ParameterizedTypeName.get(list, string);
    private HashMap<String, TypeMirror> columnsWithDataTypes = new HashMap<>();
    private HashMap<String, String> columnsWithFunctionNames = new HashMap<>();
    private String tableName;
    private final String TAG = BindingClass.class.getSimpleName();
}
