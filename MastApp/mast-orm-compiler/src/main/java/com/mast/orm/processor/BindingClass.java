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
    private final ClassName pojoClassName;
    private static final ClassName CrudOperations = ClassName.get("com.mast.orm.db", "CrudOperations");

    public BindingClass(String tableName, ClassName generatedClassName, ClassName pojoClassName) {
        this.tableName = tableName.toLowerCase();
        this.generatedClassName = generatedClassName;
        this.pojoClassName = pojoClassName;
    }

    public void addColumn(String columnName, TypeMirror valueType) {
        columnsWithDataTypes.put(columnName, valueType);
    }

    public void addFunction(String columnName, String valueType) {
        columnsWithFunctionNames.put(columnName, valueType);
    }

    public void addSubClassFKMapValue(String className, String fkName) {
        subClassFKMap.put(className, fkName);
    }

    public JavaFile brewJava() {

        String packageName = generatedClassName.packageName();
        ClassName classFqcn = ClassName.get(packageName,
                generatedClassName.simpleName());

        FieldSpec baseColumnName = FieldSpec.builder(string, "_id")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$S", "_id")
                .build();

        FieldSpec android = FieldSpec.builder(String.class, "tableName")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$S", tableName)
                .build();

        FieldSpec clazz = FieldSpec.builder(classFqcn, "instance")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();

        MethodSpec checkSchemaFuncSpec = MethodSpec.methodBuilder("checkSchemaChange")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("$N.checkSchemaChange($N,$N)", mastOrmField, android, columnTypeMap)
                .build();

        MethodSpec loadFunc = MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classFqcn)
                .beginControlFlow("if($N == null)", clazz)
                .addStatement("$N = new $T()", clazz, classFqcn)
                .addStatement("$N.$N = $T.getInstance()", clazz, mastOrmField, mastOrm)

                .addStatement("$N.addValueTypes()", clazz)
                .addStatement("$N.addSqlDataTypes()", clazz)
                .addStatement("$N.addPojoFunctionName()", clazz)
                .addStatement("$N.createTable()", clazz)
                .addStatement("$N.getTableInfo()", clazz)
                .addStatement("$N.checkSchemaChange()", clazz)
                .addStatement("$N.addSubClassFKValue()", clazz)
                .endControlFlow()
                .addStatement("return $N", clazz)
                .build();

        MethodSpec.Builder addValuesTypesFuncBuilder = MethodSpec.methodBuilder("addValueTypes")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);
        writeColumValueTypes(addValuesTypesFuncBuilder, columnTypeMap);

        MethodSpec addValuesTypesFunc = addValuesTypesFuncBuilder.build();

        MethodSpec.Builder addSqlTypesFuncBuilder = MethodSpec.methodBuilder("addSqlDataTypes")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);

        appendColumNameAndTypes(addSqlTypesFuncBuilder, columnSqlTypeMap);

        MethodSpec addSqlFunc = addSqlTypesFuncBuilder.build();

        MethodSpec.Builder addPojoFunctionName = MethodSpec.methodBuilder("addPojoFunctionName")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);
        writeStringMapToGenClass(addPojoFunctionName, pojoFunctionNameMap, columnsWithFunctionNames);

        MethodSpec pojo2Func = addPojoFunctionName.build();

        MethodSpec.Builder addSubClassFKValue = MethodSpec.methodBuilder("addSubClassFKValue")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);
        writeStringMapToGenClass(addSubClassFKValue, subClassFKHashMap, subClassFKMap);

        MethodSpec subClassFKMapFunc = addSubClassFKValue.build();

        MethodSpec createTabbleFunc = createTableFunc(columnTypeMap, columnSqlTypeMap);

        TypeSpec.Builder result = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(android)
                .addField(columnValueMap)
                .addField(columnTypeMap)
                .addField(columnSqlTypeMap)
                .addField(mastOrmField)
                .addField(columnUpdateWhereValueMap)
                .addField(pojoFunctionNameMap)
                .addField(subClassFKHashMap)
                .addField(baseColumnName)
                .addField(clazz)
                .addMethod(loadFunc)
                .addMethod(addValuesTypesFunc)
                .addMethod(addSqlFunc)
                .addMethod(createTabbleFunc)
                .addMethod(checkSchemaFuncSpec)
                .addMethod(subClassFKMapFunc)
                .addMethod(pojo2Func);

        writeColumnIndividualSetters(result);

        MethodSpec saveMethodSpec = saveTableFunc();
        MethodSpec whereMethodSpec = whereMethodSpec();
        MethodSpec updateMethodSpec = updateMethodSpec();
        MethodSpec findMethodSpec = findMethodSpec();
        MethodSpec deleteMethodSpec = deleteMethodSpec();
        MethodSpec getTableInfoMethodSpec = getTableInfoMethodSpec();
        MethodSpec getTableLatestRowIdMethodSpec = getTableLatestRowIdMethodSpec();
        MethodSpec newSaveMethodSpec = saveMethodSpec();
        MethodSpec processSubClassTypeListMethodSpec = processSubClassTypeListMethodSpec();
        MethodSpec subClassInsertMethodSpec = subClassInsertMethodSpec();
        MethodSpec findDataMethodSpec = findDataMethodSpec();
        MethodSpec subClassFindMethodSpec = subClassFindMethodSpec();
        TypeSpec enumSpec = generateEnum();

        result.addType(enumSpec);
        result.addMethod(saveMethodSpec);
        result.addMethod(whereMethodSpec);
        result.addMethod(updateMethodSpec);
        result.addMethod(findMethodSpec);
        result.addMethod(deleteMethodSpec);
        result.addMethod(getTableInfoMethodSpec);
        result.addMethod(getTableLatestRowIdMethodSpec);
        result.addMethod(processSubClassTypeListMethodSpec);
        result.addMethod(subClassInsertMethodSpec);
        result.addMethod(newSaveMethodSpec);
        result.addMethod(findDataMethodSpec);
        result.addMethod(subClassFindMethodSpec);
        ClassName superClass = ClassName.get(generatedClassName.packageName(), "BaseSchema");
        result.superclass(superClass);
        return JavaFile.builder(generatedClassName.packageName(), result.build())
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
//                String decalredType = Utils.getSqlDataType(className);
//                if (decalredType != null)
                builder.addStatement("$N.put($S,$S)", columnTypeMap, pair.getKey(), className);
            }
        }
    }

    private void writeStringMapToGenClass(MethodSpec.Builder builder, FieldSpec columnTypeMap, HashMap<String, String> columnsWithFunctionNames) {
        if (builder != null && columnsWithFunctionNames != null) {
            Iterator it = columnsWithFunctionNames.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
//                String typeMirror = (String) pair.getValue();
//                String className = Utils.toString(typeMirror, false);
//                if(typeMirror.getKind()!= TypeKind.DECLARED)
//                String decalredType = Utils.getSqlDataType(className);
//                if (decalredType != null)
                builder.addStatement("$N.put($S,$S)", columnTypeMap, pair.getKey(), pair.getValue());
//                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }


    private MethodSpec createTableFunc(FieldSpec columnTypeMap, FieldSpec sqlTypeMap) {

        String createTableString = "CREATE TABLE IF NOT EXISTS "
                + tableName
                + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, ";
        MethodSpec.Builder createTableFuncBuilder = MethodSpec.methodBuilder("createTable")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T queryBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("queryBuilder.append($S)", createTableString)
                .addStatement("int size = $N.size()", columnTypeMap)
                .addStatement("int index=0")
                .addStatement("$T it = $N.entrySet().iterator()", iteratorType, columnTypeMap)
                .beginControlFlow(" while (it.hasNext())")
                .addStatement(" $T.Entry pair = ($T.Entry) it.next()", mapType, mapType)
                .beginControlFlow("if($N.get(pair.getValue())!=null)", sqlTypeMap)
                .addStatement("queryBuilder.append(pair.getKey()+\" \"+ $N.get(pair.getValue()))", sqlTypeMap)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("queryBuilder.append(pair.getKey()+\" \"+ \"TEXT\")")
                .endControlFlow()
                .beginControlFlow("if(index <size-1)")
                .addStatement("queryBuilder.append(\",\")")
                .endControlFlow()
                .addStatement("index++")
                .endControlFlow()
                .addStatement("queryBuilder.append(\")\")")
                .addStatement("$T.e($S, queryBuilder.toString())", log, tableName)
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
                .addStatement("$T.e($S, $S+pair.getKey())", log, tableName, "field value ")
                .beginControlFlow("if($N.containsKey(pair.getKey())&&$N.get(pair.getKey()).contentEquals(\"String\"))", columnTypeMap, columnTypeMap)
                .addStatement("insertValueBuilder.append(\"'\"+pair.getValue()+\"'\")")
                .endControlFlow()
                .beginControlFlow("else if($N.containsKey(pair.getKey())&&$N.get(pair.getKey()).contentEquals(\"Boolean\"))", columnTypeMap, columnTypeMap)
                .addStatement("Boolean value = (Boolean)pair.getValue()")
                .beginControlFlow("if(value!=null)")
                .addStatement("insertValueBuilder.append(value?1:0)")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("insertValueBuilder.append(0)")
                .endControlFlow()
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
                        createFieldAndMethod(string, result, varName);
                    } else if (className.contentEquals("Integer")) {
                        createFieldAndMethod(integer, result, varName);
                    } else if (className.contentEquals("Boolean")) {
                        createFieldAndMethod(bool, result, varName);
                    }

                } else {
//                    TypeMirror mirror = (TypeMirror) pair.getValue();
//                    Name paramType = ((TypeElement) ((DeclaredType) mirror).asElement()).getQualifiedName();
//
//                    List<? extends TypeMirror> typeArguments = ((DeclaredType) mirror).getTypeArguments();
//                    System.out.println("BindingClass TypeMirror");
//                    TypeName elementTypeName= TypeName.get(typeMirror);
//                    ClassName mastOrm = ClassName.get(((TypeElement) ((DeclaredType) mirror).asElement()).getQualifiedName().toString(), "MastOrm");
//                    if (typeArguments.size() == 0){ //single object
//
//                    }else { //list of objects
//
//                    }
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

//        tableEnum.addEnumConstant("_ID", TypeSpec.anonymousClassBuilder("$S", "_id")
//                .build());

        tableEnum.addField(String.class, "columnName", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(String.class, "columnName")
                        .addStatement("this.$N = $N", "columnName", "columnName")
                        .build());
        return tableEnum.build();
    }

    private MethodSpec whereMethodSpec() {
//        String packageName = Utils.getPackageName(generatedClassName.packageName());
        String packageName = generatedClassName.packageName();
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
                .addStatement("insertValueBuilder.append(\" AND \")")
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

    private MethodSpec subClassFindMethodSpec() {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("subFindData")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Integer.class, "insertRowId")
                .addParameter(String.class, "subClassFK_Key")
                .returns(listOfPojos)
                .addStatement("$N.put(subClassFK_Key,insertRowId)", columnUpdateWhereValueMap)
                .addStatement("return findData()");
        return findFuncBuilder.build();
    }

    private MethodSpec findMethodSpec() {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        String updateString = "SELECT * FROM " + tableName;
        String whereString = " WHERE ";
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("find")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T insertValueBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertValueBuilder.append($S)", updateString)
                .addStatement("int new_size = $N.size()", columnUpdateWhereValueMap)
                .beginControlFlow("if(new_size>0)")
                .addStatement("insertValueBuilder.append($S)", whereString)
                .endControlFlow()
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
                .addStatement("insertValueBuilder.append(\" AND \")")
                .endControlFlow()
                .addStatement("new_index++")
                .endControlFlow()
                .returns(listOfPojos);
        enableCursorProcessing(findFuncBuilder);
        return findFuncBuilder.build();
    }

    private MethodSpec findDataMethodSpec() {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        String updateString = "SELECT * FROM " + tableName;
        String whereString = " WHERE ";
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("findData")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T insertValueBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertValueBuilder.append($S)", updateString)
                .addStatement("int new_size = $N.size()", columnUpdateWhereValueMap)
                .beginControlFlow("if(new_size>0)")
                .addStatement("insertValueBuilder.append($S)", whereString)
                .endControlFlow()
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
                .addStatement("insertValueBuilder.append(\" AND \")")
                .endControlFlow()
                .addStatement("new_index++")
                .endControlFlow()
                .returns(listOfPojos);
        enableCursorDataProcessing(findFuncBuilder);
        return findFuncBuilder.build();
    }


    private MethodSpec deleteMethodSpec() {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        String updateString = "DELETE FROM " + tableName;
        String whereString = " WHERE ";
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T insertValueBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("insertValueBuilder.append($S)", updateString)
                .addStatement("int new_size = $N.size()", columnUpdateWhereValueMap)
                .beginControlFlow("if(new_size>0)")
                .addStatement("insertValueBuilder.append($S)", whereString)
                .endControlFlow()
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
                .addStatement("insertValueBuilder.append(\" AND \")")
                .endControlFlow()
                .addStatement("new_index++")
                .endControlFlow()
                .returns(listOfPojos);
        enableCursorProcessing(findFuncBuilder);
        return findFuncBuilder.build();
    }


    private void enableCursorProcessing(MethodSpec.Builder findFuncBuilder) {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        TypeName arrayListOfPojos = ParameterizedTypeName.get(arrayList, pojoClassName);
        ClassName cursor = ClassName.get("android.database", "Cursor");
        findFuncBuilder
                .addStatement("$T cursor = $N.executeRead(insertValueBuilder.toString(),null)", cursor, mastOrmField)
                .addStatement("$T pojoList = new $T()", listOfPojos, arrayListOfPojos)
                .beginControlFlow("if (cursor.moveToFirst())")
                .beginControlFlow("do")
                .addStatement("$T pojo = new $T()", pojoClassName, pojoClassName);

        iteratePojoFunnctionMap(findFuncBuilder);
        findFuncBuilder
                .addStatement("pojoList.add(pojo)")
                .endControlFlow("while(cursor.moveToNext())")
                .endControlFlow()
                .addStatement("$N.clear()", columnUpdateWhereValueMap)
                .addStatement("return pojoList");
    }

    private void iteratePojoFunnctionMap(MethodSpec.Builder builder) {
        if (builder != null && columnsWithFunctionNames != null) {
            Iterator it = columnsWithFunctionNames.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String funcName = (String) pair.getValue();
                TypeMirror typeMirror = columnsWithDataTypes.get(pair.getKey());
                String className = Utils.toString(typeMirror, false);
                String decalredType = Utils.getSqlDataType(className);
                if (decalredType != null) {

                    builder.beginControlFlow("if($N.get($S)!=null)", columnTypeMap, pair.getKey());
                    builder.addStatement("int index = cursor.getColumnIndex($S)", pair.getKey());

                    if (className.contentEquals("String")) {
                        builder.addStatement("String value = cursor.getString(index)");
                    } else if (className.contentEquals("Integer")) {
                        builder.addStatement("Integer value = cursor.getInt(index)");
                    } else if (className.contentEquals("Boolean")) {
                        builder.addStatement("Integer intVal = cursor.getInt(index)");
                        builder.addStatement("Boolean value = false");
                        builder.beginControlFlow("if(intVal>0)");
                        builder.addStatement("value = true");
                        builder.endControlFlow();
                    }
                    builder.addStatement("$N.$N($N)", "pojo", funcName, "value");
                    builder.endControlFlow();
                }
//                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    private <T> void createFieldAndMethod(TypeName type, TypeSpec.Builder result, String varName) {
//        String packageName = Utils.getPackageName(generatedClassName.packageName());
        String packageName = generatedClassName.packageName();


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

    //    SELECT sql FROM sqlite_master
//    WHERE tbl_name = 'table_name' AND type = 'table'
    private MethodSpec getTableInfoMethodSpec() {
        String updateString = "PRAGMA table_info(" + tableName + ")";
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("getTableInfo")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T tableInfoQueryBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("tableInfoQueryBuilder.append($S)", updateString);

        ClassName cursor = ClassName.get("android.database", "Cursor");
        findFuncBuilder
                .addStatement("$T cursor = $N.executeRead(tableInfoQueryBuilder.toString(),null)", cursor, mastOrmField)
                .addStatement("$T pojoList = new $T()", listOfHoverboards, arrayList)
                .beginControlFlow("if (cursor.moveToFirst())")
                .beginControlFlow("do")
                .addStatement("String value = cursor.getString(1)")
                .addStatement("pojoList.add(value)")
                .endControlFlow("while(cursor.moveToNext())")
                .endControlFlow()
                .addStatement("return pojoList")
                .returns(listOfHoverboards);
        return findFuncBuilder.build();
    }

    private MethodSpec processSubClassTypeListMethodSpec() {
        ClassName aClass = ClassName.get("java.lang", "Class");
        MethodSpec.Builder processSubClassTypeListMethodSpec = MethodSpec.methodBuilder("processSubClassTypeList")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listOfBindClasses, "subClassValueList")
                .addParameter(Integer.class, "lastInsertedId")
                .beginControlFlow(" for($T bindClass : subClassValueList)", bindClass)
                .addStatement("String classTypeStr = bindClass.getClassName()")
                .addStatement("String fkKey = $N.get(classTypeStr)", subClassFKHashMap)
                .addStatement("subSchemaInsert(classTypeStr,bindClass.getObjectValue(),lastInsertedId,fkKey)")
                .endControlFlow();

        return processSubClassTypeListMethodSpec.build();
    }

    private MethodSpec getTableLatestRowIdMethodSpec() {
        String updateString = "SELECT _id from " + tableName + " order by _id DESC limit 1";
        MethodSpec.Builder findFuncBuilder = MethodSpec.methodBuilder("getTableLatestRowId")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T tableInfoQueryBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("tableInfoQueryBuilder.append($S)", updateString);

        ClassName cursor = ClassName.get("android.database", "Cursor");
        findFuncBuilder
                .addStatement("$T cursor = $N.executeRead(tableInfoQueryBuilder.toString(),null)", cursor, mastOrmField)
                .beginControlFlow("if (cursor.moveToFirst())")
                .addStatement("Integer value = cursor.getInt(0)")
                .addStatement("return value")
                .endControlFlow()
                .addStatement("return -1")
                .returns(Integer.class);
        return findFuncBuilder.build();
    }

    private void enableCursorDataProcessing(MethodSpec.Builder findFuncBuilder) {
        TypeName listOfPojos = ParameterizedTypeName.get(list, pojoClassName);
        TypeName arrayListOfPojos = ParameterizedTypeName.get(arrayList, pojoClassName);
        ClassName cursor = ClassName.get("android.database", "Cursor");
        ClassName aClass = ClassName.get("java.lang", "Class");
        ClassName utils = ClassName.get("com.mast.orm.db", "Utils");
        ClassName field = ClassName.get("java.lang.reflect", "Field");
        ClassName method = ClassName.get("java.lang.reflect", "Method");
        ClassName objectClass = ClassName.get("java.lang", "Object");
        TypeName listOfObjectClass = ParameterizedTypeName.get(list, objectClass);
        ClassName parameterizedType = ClassName.get("java.lang.reflect", "ParameterizedType");
        ClassName log = ClassName.get("android.util", "Log");
        findFuncBuilder
                .addStatement("$T cursor = $N.executeRead(insertValueBuilder.toString(),null)", cursor, mastOrmField)
                .beginControlFlow("try")
                .addStatement("$T columnNameList = getTableInfo()", listOfHoverboards)
                .beginControlFlow("if(columnNameList!=null&&columnNameList.size()>0)")
                .addStatement("$T pojoList = new $T()", listOfPojos, arrayListOfPojos)
                .beginControlFlow("if (cursor.moveToFirst())")
                .beginControlFlow("do")
                .addStatement("$T pojo = new $T()", pojoClassName, pojoClassName)
                .addStatement("$T classObj = pojo.getClass()", aClass)
                .beginControlFlow("for($T columnName:columnNameList)", string)
                .addStatement("$T field = classObj.getDeclaredField(columnName)", field)
                .addStatement("$T fieldType = field.getType()", aClass)
                .beginControlFlow("if($T.isWrapperType(fieldType))", utils)
                .addStatement("int index = cursor.getColumnIndex(columnName)")
                .addStatement("String functionName = $N.get(columnName)", pojoFunctionNameMap)
                .addStatement("$T method = classObj.getMethod(functionName, new $T[]{fieldType})", method, aClass)
                .addStatement("method.invoke(pojo,$T.getColumnValue(fieldType,cursor,index))", utils)
                .endControlFlow()
                .beginControlFlow("else if(fieldType.equals(List.class))")
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "field is List Type ")
                .addStatement("$T listType = ($T) field.getGenericType()", parameterizedType, parameterizedType)
                .addStatement("$T<?> argumentType = ($T<?>)listType.getActualTypeArguments()[0]", aClass, aClass)
                .beginControlFlow("if($T.isWrapperType(argumentType))", utils)
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "list fieldType is primitive ")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "list fieldType is not primitive ")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "field is not primitive ")
//                .addStatement("$T listOf")
                .endControlFlow()
                .addStatement("pojoList.add(pojo)")
                .endControlFlow()
                .endControlFlow("while(cursor.moveToNext())")
                .endControlFlow()
                .addStatement("$N.clear()", columnUpdateWhereValueMap)
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("catch(Exception e)")
                .addStatement("e.printStackTrace()")
                .endControlFlow()
                .addStatement("return null");

    }

    private MethodSpec saveMethodSpec() {
        ClassName aClass = ClassName.get("java.lang", "Class");
        ClassName utils = ClassName.get("com.mast.orm.db", "Utils");
        ClassName field = ClassName.get("java.lang.reflect", "Field");
        ClassName objectClass = ClassName.get("java.lang", "Object");
        TypeName listOfObjectClass = ParameterizedTypeName.get(list, objectClass);
        ClassName parameterizedType = ClassName.get("java.lang.reflect", "ParameterizedType");
        ClassName log = ClassName.get("android.util", "Log");
        MethodSpec.Builder saveMethodSpec = MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(pojoClassName, "objValue")
                .beginControlFlow("try")
                .addStatement("$T subClassValueList = new $T()", listOfBindClasses, arrayListOfBindClasses)
                .addStatement("$T classObj = objValue.getClass()", aClass)
                .addStatement("int size = $N.size()", columnTypeMap)
                .addStatement("int index=0")
                .addStatement("$T it = $N.entrySet().iterator()", iteratorType, columnTypeMap)
                .beginControlFlow(" while (it.hasNext())")
                .addStatement("$T.Entry pair = ($T.Entry) it.next()", mapType, mapType)
                .addStatement("$T field = classObj.getDeclaredField((String)pair.getKey())", field)
                .addStatement("$T fieldType = field.getType()", aClass)
                .addStatement("field.setAccessible(true)")
//                .beginControlFlow("if(field.get(objValue)!=null)")
                .beginControlFlow("if($T.isWrapperType(fieldType))", utils)
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "field is primitive ")
                .addStatement("$N.put(field.getName(),field.get(objValue))", columnValueMap)
                .endControlFlow()
                .beginControlFlow("else if(fieldType.equals(List.class))")
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "field is List Type ")
                .addStatement("$T listType = ($T) field.getGenericType()", parameterizedType, parameterizedType)
                .addStatement("$T<?> argumentType = ($T<?>)listType.getActualTypeArguments()[0]", aClass, aClass)
                .beginControlFlow("if($T.isWrapperType(argumentType))", utils)
                .addStatement("$T primitiveStringBuilder = new $T()", stringBuilder, stringBuilder)
                .beginControlFlow("for($T object:($T)field.get(objValue))", objectClass, listOfObjectClass)
                .addStatement("primitiveStringBuilder.append(String.valueOf(object)+\",\")")
                .endControlFlow()
                .addStatement("$N.put(field.getName(),primitiveStringBuilder.toString())", columnValueMap)
                .endControlFlow()
                .beginControlFlow("else")
                .beginControlFlow("for($T object:($T)field.get(objValue))", objectClass, listOfObjectClass)
                .addStatement("$T bindClass = new $T()", bindClass, bindClass)
                .addStatement("bindClass.setClassName(argumentType.getCanonicalName())")
                .addStatement("bindClass.setObjectValue(object)")
                .addStatement("subClassValueList.add(bindClass)")
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$T.e($S, $S+field.getName())", log, tableName, "field is not primitive ")
                .addStatement("$T bindClass = new $T()", bindClass, bindClass)
                .addStatement("bindClass.setClassName(fieldType.getCanonicalName())")
                .addStatement("bindClass.setObjectValue(field.get(objValue))")
                .addStatement("subClassValueList.add(bindClass)")
                .endControlFlow()
//                .addStatement("it.remove()")
//                .endControlFlow()
                .endControlFlow()
                .addStatement("save()")
                .addStatement("int latestRowId =getTableLatestRowId()")
                .addStatement("processSubClassTypeList(subClassValueList,latestRowId)")
                .endControlFlow()

                .beginControlFlow("catch(Exception e)")
                .addStatement("e.printStackTrace()")
                .endControlFlow();

        return saveMethodSpec.build();
    }

    private MethodSpec subClassInsertMethodSpec() {
        String alterTableString = "ALTER TABLE " + tableName + " ADD COLUMN ";
        ClassName aClass = ClassName.get("java.lang", "Class");
        ClassName utils = ClassName.get("com.mast.orm.db", "Utils");
        ClassName field = ClassName.get("java.lang.reflect", "Field");
        ClassName objectClass = ClassName.get("java.lang", "Object");
        TypeName listOfObjectClass = ParameterizedTypeName.get(list, objectClass);
        ClassName parameterizedType = ClassName.get("java.lang.reflect", "ParameterizedType");
        ClassName log = ClassName.get("android.util", "Log");
        MethodSpec.Builder saveMethodSpec = MethodSpec.methodBuilder("subInsert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(pojoClassName, "objValue")
                .addParameter(string, "fKey")
                .addParameter(integer, "insertedId")
                .addStatement("$T columnExists = false;", bool)
                .addStatement("$T existingColumns = getTableInfo()", listOfHoverboards)
                .beginControlFlow("for($T str :existingColumns)", string)
                .beginControlFlow("if(str.contentEquals(fKey))")
                .addStatement("columnExists = true")
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("if(!columnExists)")
                .addStatement("$T tableInfoQueryBuilder = new $T()", stringBuilder, stringBuilder)
                .addStatement("tableInfoQueryBuilder.append($S)", alterTableString)
                .addStatement("tableInfoQueryBuilder.append(fKey+\" INTEGER\")", alterTableString)
                .addStatement("$N.executeWrite(tableInfoQueryBuilder.toString(),null)", mastOrmField)
                .endControlFlow()
                .addStatement("$N.put(fKey,insertedId)", columnValueMap)
                .addStatement("insert(objValue)");
        return saveMethodSpec.build();
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

    FieldSpec pojoFunctionNameMap = FieldSpec.builder(columnValueTypeName, "pojoFunctionNameMap")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();

    FieldSpec subClassFKHashMap = FieldSpec.builder(columnValueTypeName, "subClassFKMap")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T<>()", hashSet)
            .build();

    FieldSpec mastOrmField = FieldSpec.builder(mastOrm, "mastOrm")
            .addModifiers(Modifier.PRIVATE)
            .build();

    ClassName bindClass = ClassName.get("com.mast.orm.db", "BindClass");
    ClassName log = ClassName.get("android.util", "Log");
    ClassName list = ClassName.get("java.util", "List");
    ClassName arrayList = ClassName.get("java.util", "ArrayList");
    TypeName listOfHoverboards = ParameterizedTypeName.get(list, string);
    TypeName listOfBindClasses = ParameterizedTypeName.get(list, bindClass);
    TypeName arrayListOfBindClasses = ParameterizedTypeName.get(arrayList, bindClass);

    private HashMap<String, TypeMirror> columnsWithDataTypes = new HashMap<>();
    private HashMap<String, String> columnsWithFunctionNames = new HashMap<>();
    private HashMap<String, String> subClassFKMap = new HashMap<>();
    private String tableName;
    private final String TAG = BindingClass.class.getSimpleName();
}
