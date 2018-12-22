package com.talhahasanzia.processor;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.talhahasanzia.annotation.Routeable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@SupportedAnnotationTypes("com.talhahasanzia.annotation.Routeable")
public class RouteProcessor extends AbstractProcessor {

    // method name that will be generated
    private static final String METHOD_PREFIX = "route";
    // Intent class specification
    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    // Context class specification
    private static final ClassName classContext = ClassName.get("android.content", "Context");
    // Activity class specification
    private static final ClassName classActivity = ClassName.get("android.app", "Activity");
    // Bundle class specification
    private static final ClassName classBundle = ClassName.get("android.os", "Bundle");
    // Parcelable class specification
    private static final ClassName classParcelable = ClassName.get("android.os", "Parcelable");
    // Serializable class specification
    private static final ClassName classSerializable = ClassName.get("java.io", "Serializable");


    // some objects that we get in "init" method, we use some and leave others for later implementations
    private ProcessingEnvironment processingEnvironment;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Map<String, String> activitiesWithPackage;

    // init implementation, runs 1st time the processor starts
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnv;
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        activitiesWithPackage = new HashMap<>();


    }

    // runs when processor is called by compiler
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // get each element that was annotated with "Routeable class, and do the following
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Routeable.class)) {

            // only support annotation on class types
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be only applied to class.");
            }

            // get element type
            TypeElement typeElement = (TypeElement) element;
            // get package
            PackageElement pkg = elementUtils.getPackageOf(element);

            // generate a class
            TypeSpec.Builder generatedClass = generateClass(element, typeElement.getSimpleName().toString(),
                    pkg.getQualifiedName().toString());

            // write a .java class using filer
            try {
                JavaFile.builder(pkg.getQualifiedName().toString(), generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        return true;
    }

    // Generates Router classes for annotated types, using JavaPoet
    private TypeSpec.Builder generateClass(Element element, String activityName, String packageName) {

        // Class specification
        TypeSpec.Builder generatedClass = TypeSpec
                .classBuilder(activityName + "Router")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // The class on which the annotation was used, here we expect it to be "Activity" class
        ClassName activityClass = ClassName.get(packageName, activityName);

        // add following method definitions to generated class
        generatedClass.addMethod(getContextMethod(activityClass));
        generatedClass.addMethod(getContextFinishMethod(activityClass));
        generatedClass.addMethod(getContextBundleMethod(activityClass));
        generatedClass.addMethod(getContextBundleFinishMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraFinishMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraFinishMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraFinishMethod(activityClass));

        // return generated class
        return generatedClass;
    }

    // simple route method that gets context and route to activity that annotation was used
    private MethodSpec getContextMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addStatement("$L.startActivity( new $T($L, $L))", "context", classIntent, "context", activityClass + ".class")
                .build();
    }

    // simple route method that gets context and route to activity that annotation was used.
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    private MethodSpec getContextFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$L.startActivity( new $T($L, $L))", "context", classIntent, "context", activityClass + ".class")
                .beginControlFlow("if(finishCurrent)")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a bundle that will be passed in the intent
    private MethodSpec getContextBundleMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }

    // route method with context and also a bundle that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    private MethodSpec getContextBundleFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("$L.startActivity( intent )", "context")
                .beginControlFlow("if(finishCurrent)")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a String extra that will be passed in the intent
    private MethodSpec getContextStringExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }

    // route method with context and also a String extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    private MethodSpec getContextStringExtraFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("$L.startActivity( intent )", "context")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a Serializable extra that will be passed in the intent
    private MethodSpec getContextSerializableExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }

    // route method with context and also a Serializable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    private MethodSpec getContextSerializableExtraFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a Parcelable extra that will be passed in the intent
    private MethodSpec getContextParcelableExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }


    // route method with context and also a Parcelable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    private MethodSpec getContextParcelableExtraFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Routeable.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

}
