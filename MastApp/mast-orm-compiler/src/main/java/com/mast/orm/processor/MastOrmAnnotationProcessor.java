package com.mast.orm.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.mast.orm.processor.Util.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.squareup.javapoet.TypeSpec.Kind.ANNOTATION;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * Created by sathish-n on 16/7/16.
 */
@AutoService(Processor.class)
public class MastOrmAnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private static final String BINDING_CLASS_SUFFIX = "_Schema";
    private boolean oneTimeFlag = true;
    private HashMap<String, String> classMaps = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        messager.printMessage(NOTE, "Reactalytics.class.getCanonicalName()");
        Set<String> types = new LinkedHashSet<>();

        types.add(JsonProperty.class.getCanonicalName());
        types.add(JsonPropertyOrder.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<TypeElement, BindingClass> targetBindClassMap = new HashMap<>();
//
        for (Element element : roundEnv.getElementsAnnotatedWith(JsonProperty.class)) {
            messager.printMessage(NOTE, "JsonPropertyOrder annotation is being processed");
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseTrackEvent(element, targetBindClassMap);
            } catch (Exception e) {
                logParsingError(element, JsonProperty.class, e);
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(JsonPropertyOrder.class)) {
            messager.printMessage(NOTE, "JsonProperty annotation is being processed");
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseReactalytics(element, targetBindClassMap);
            } catch (Exception e) {
                logParsingError(element, JsonPropertyOrder.class, e);
            }
        }


        for (Map.Entry<TypeElement, BindingClass> entry : targetBindClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingClass bindingClass = entry.getValue();

            try {
                if (oneTimeFlag) {
                    String pacakageName = getPackageName(typeElement);
                    ClassName baseSchemaClass = ClassName.get(pacakageName, "BaseSchema");
                    brewBaseSchema(baseSchemaClass).writeTo(filer);
                    oneTimeFlag = false;
                }

                bindingClass.brewJava().writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());
            }
        }

        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotationMirror, ExecutableElement executableElement, String s) {
        messager.printMessage(NOTE, " Completion annotation " + s);
        return super.getCompletions(element, annotationMirror, executableElement, s);
    }

    private void parseReactalytics(Element element, Map<TypeElement, BindingClass> targetClassMap) {
        TypeElement annotationElement = (TypeElement) element;
        if (!isValidClass(annotationElement)) {
            return;
        }
        BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, annotationElement);
    }

    private void parseTrackEvent(Element element, Map<TypeElement, BindingClass> targetClassMap) {
        TypeElement annotationElement = (TypeElement) element.getEnclosingElement();
        if (!isValidClass(annotationElement)) {
            return;
        }
        if (element instanceof VariableElement) {
            if (!(element instanceof VariableElement) && element.getKind() != ElementKind.FIELD) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a method.", JsonProperty.class.getSimpleName()));
            }

            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType();
            String name = variableElement.getSimpleName().toString();
//            TypeElement superClassTypeElement =
//                    (TypeElement)((DeclaredType)typeMirror).asElement();
            TypeName variableClass = getVariableClass(variableElement);
            String typeName = Utils.toString(typeMirror, true);
            messager.printMessage(NOTE, "Activity Variable Name " + typeName);
            BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, annotationElement);
            if (bindingClass != null) {
                bindingClass.addColumn(name, typeMirror);
            }
        } else if (element instanceof ExecutableElement) {
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeMirror typeMirror = executableElement.asType();
            String name = executableElement.getSimpleName().toString();
            JsonProperty annotatedElement = executableElement.getAnnotation(JsonProperty.class);

            messager.printMessage(NOTE, "Activity Method Name " + name + " annotation value " + annotatedElement.value());
            if (name.matches("set\\S+")) {
                messager.printMessage(NOTE, "Activity Set Method Name " + name + " annotation value " + annotatedElement.value());
                BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, annotationElement);
                if (bindingClass != null) {
                    bindingClass.addFunction(annotatedElement.value(), name);
                }
            }
        }
    }

    private TypeName getVariableClass(VariableElement variableElement) {
        TypeMirror typeMirror = variableElement.asType();
        TypeElement element1 = ((TypeElement) ((DeclaredType) typeMirror).asElement());

        Name paramType = ((TypeElement) ((DeclaredType) typeMirror).asElement()).getQualifiedName();

        List<? extends TypeMirror> typeArguments = ((DeclaredType) typeMirror).getTypeArguments();
        System.out.println("BindingClass TypeMirror");
        if (typeArguments.size() == 0) { //single object
            String packageName = getPackageName(element1);
            String name = variableElement.getSimpleName().toString();
            ClassName className = ClassName.get(packageName, name);
            return className;
        } else { //list of objects
            if (paramType.toString().contentEquals("java.util.List")) {
                ClassName list = ClassName.get("java.util", "List");
                TypeMirror mirror = typeArguments.get(0);
                TypeElement elementNew = ((TypeElement) ((DeclaredType) mirror).asElement());
                String packageName = getPackageName(elementNew);
                String name = variableElement.getSimpleName().toString();
                ClassName className = ClassName.get(packageName, name);
                TypeName listOfHoverboards = ParameterizedTypeName.get(list, className);
                return listOfHoverboards;
            }
        }
        return null;
    }

    //
//
    private BindingClass getOrCreateTargetClass(Map<TypeElement, BindingClass> targetClassMap,
                                                TypeElement annotationElement) {
        BindingClass bindingClass = targetClassMap.get(annotationElement);
        if (bindingClass == null) {

            String packageName = getPackageName(annotationElement);
            String className = getClassName(annotationElement, packageName) + BINDING_CLASS_SUFFIX;
            ClassName classFqcn = ClassName.get(packageName,
                    className);
            classMaps.put(annotationElement.getQualifiedName().toString(), className);
            ClassName classPojo = ClassName.get(packageName,
                    getClassName(annotationElement, packageName));
            bindingClass = new BindingClass(getClassName(annotationElement, packageName), classFqcn, classPojo);
            targetClassMap.put(annotationElement, bindingClass);
        }
        return bindingClass;
    }

    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        String className = type.getQualifiedName().toString().substring(packageLen).replace(".", "");
        messager.printMessage(NOTE, "Activity Class type Name " + className);
        return className;
    }

    private String getPackageName(TypeElement type) {
        String packageName = elementUtils.getPackageOf(type).getQualifiedName().toString();
        return packageName;
    }

    //
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    //
    private boolean isValidClass(TypeElement annotatedClass) {

        if (!ClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (ClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        return true;
    }

    private JavaFile brewBaseSchema(ClassName baseSchemaClass) {
        TypeSpec.Builder result = TypeSpec.classBuilder(baseSchemaClass)
                .addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder subSchemaInsertBuilder = MethodSpec.methodBuilder("subSchemaInsert")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(String.class, "subClassName")
                .addParameter(Object.class, "subClassObj")
                .returns(void.class);

        for (Map.Entry<String, String> entry : classMaps.entrySet()) {
            ClassName subSchemaClassName = ClassName.get(baseSchemaClass.packageName(), entry.getValue());
            ClassName dtoClassName = ClassName.get(baseSchemaClass.packageName(), entry.getKey());
            subSchemaInsertBuilder.beginControlFlow("if(subClassName.contentEquals($S))", entry.getKey())
                    .addStatement("$T subClass = $T.load()", subSchemaClassName, subSchemaClassName)
                    .addStatement("subClass.insert(($T)subClassObj)",dtoClassName)
                    .endControlFlow();

        }
        result.addMethod(subSchemaInsertBuilder.build());
        return JavaFile.builder(baseSchemaClass.packageName(), result.build())
                .addFileComment("Generated code from Mast ORM. Do not modify!")
                .build();
    }

}
