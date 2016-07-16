package com.mast.orm.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.mast.orm.processor.Util.Utils;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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

        Map<TypeElement, BindingClass> targetBindClassMap = new LinkedHashMap<>();
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

        messager.printMessage(NOTE, "MastCompiler annotation is being processed");
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

            VariableElement executableElement = (VariableElement) element;
            TypeMirror typeMirror = executableElement.asType();
            String name = executableElement.getSimpleName().toString();
//            TypeElement superClassTypeElement =
//                    (TypeElement)((DeclaredType)typeMirror).asElement();
            String typeName = Utils.toString(typeMirror,false);
            messager.printMessage(NOTE, "Activity Method Name " + typeName);
            BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, annotationElement);
            if (bindingClass != null) {
                messager.printMessage(NOTE, "Activity Method Name " + name);
                bindingClass.addColumn(name, typeMirror);
            }
        }
    }

    //
//
    private BindingClass getOrCreateTargetClass(Map<TypeElement, BindingClass> targetClassMap,
                                                TypeElement annotationElement) {
        BindingClass bindingClass = targetClassMap.get(annotationElement);
        if (bindingClass == null) {

            String packageName = getPackageName(annotationElement);
            ClassName classFqcn = ClassName.get(packageName,
                    getClassName(annotationElement, packageName));

            bindingClass = new BindingClass(getClassName(annotationElement, packageName), classFqcn);
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
        String packageName =  elementUtils.getPackageOf(type).getQualifiedName().toString();
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

}
