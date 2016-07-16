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

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by sathish-n on 16/7/16.
 */

public class BindingClass {

    private final ClassName generatedClassName;
    public BindingClass(String tableName, ClassName generatedClassName) {
        this.tableName = tableName;
        this.generatedClassName = generatedClassName;
    }

    public void addColumn(String columnName, TypeMirror valueType){
        columnsWithDataTypes.put(columnName, valueType);
    }

    public JavaFile brewJava() {
//        MethodSpec hexDigit = MethodSpec.methodBuilder("addActivityFilter")
//                .returns(void.class)
//                .addStatement("return (char) (i < 10 ? i + '0' : i - 10 + 'a')")
//                .build();

        ClassName hashSet = ClassName.get("java.util", "HashMap");

        TypeName declaredSet = ParameterizedTypeName.get(hashSet, ClassName.get("java.lang", "String"),
                ClassName.get("java.lang", "Object"));

        ClassName string = ClassName.get("java.lang", "String");
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        TypeName listOfHoverboards = ParameterizedTypeName.get(list, string);

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
                .addStatement("return $N",android)
                .build();

        MethodSpec getFilterList = MethodSpec.methodBuilder("getFilterList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfHoverboards)
                .addStatement("return $N", activityFilter)
                .build();

        MethodSpec isMethodExists = MethodSpec.methodBuilder("isMethodExists")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(String.class, "methodName")
                .addStatement("boolean flag = false")
                .beginControlFlow("for (int i = $L; i < $N.size(); i++)", 0, methodFilter)
                .addStatement("flag = $N.get(i).toLowerCase().contentEquals($N.toLowerCase())", methodFilter,"methodName")
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

        TypeSpec.Builder result = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(activityFilter)
                .addField(methodFilter)
                .addField(android)
//                .addMethod( beyondMethod)
                .addMethod(getFilterList)
                .addMethod(activityFilterMethod)
                .addMethod(isMethodExists)
                .addMethod(getScreenName)
                .addMethod(flux);

//        TypeName declaredSet = ParameterizedTypeName.get(BaseVerify, ClassName.get("java.lang", "String"));
//
//        result.addSuperinterface(declaredSet);

        return JavaFile.builder(Utils.getPackageName(generatedClassName.packageName()), result.build())
                .addFileComment("Generated code from Reactalytics. Do not modify!")
                .build();
    }

    private HashMap<String, TypeMirror> columnsWithDataTypes = new HashMap<>();
    private String tableName;
}
