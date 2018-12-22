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

    private static final String METHOD_PREFIX = "start";
    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    private static final ClassName classContext = ClassName.get("android.content", "Context");
    private static final ClassName classActivity = ClassName.get("android.app", "Activity");
    private static final ClassName classBundle = ClassName.get("android.os", "Bundle");
    private static final ClassName classParcelable = ClassName.get("android.os", "Parcelable");
    private static final ClassName classSerializable = ClassName.get("java.io", "Serializable");

    private ProcessingEnvironment processingEnvironment;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Map<String, String> activitiesWithPackage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnv;
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        activitiesWithPackage = new HashMap<>();


    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Routeable.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be only applied to class.");
            }

            TypeElement typeElement = (TypeElement) element;
            PackageElement pkg = elementUtils.getPackageOf(element);


            TypeSpec.Builder generatedClass = generateClass(element, typeElement.getSimpleName().toString(),
                    pkg.getQualifiedName().toString());


            try {
                JavaFile.builder(pkg.getQualifiedName().toString(), generatedClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        return true;
    }


    private TypeSpec.Builder generateClass(Element element, String activityName, String packageName) {


        TypeSpec.Builder generatedClass = TypeSpec
                .classBuilder(activityName + "Router")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        ClassName activityClass = ClassName.get(packageName, activityName);


        generatedClass.addMethod(getContextMethod(activityName, activityClass));
        generatedClass.addMethod(getContextFinishMethod(activityName, activityClass));
        generatedClass.addMethod(getContextBundleMethod(activityName, activityClass));
        generatedClass.addMethod(getContextBundleFinishMethod(activityName, activityClass));
        generatedClass.addMethod(getContextStringExtraMethod(activityName, activityClass));
        generatedClass.addMethod(getContextStringExtraFinishMethod(activityName, activityClass));
        generatedClass.addMethod(getContextSerializableExtraMethod(activityName, activityClass));
        generatedClass.addMethod(getContextSerializableExtraFinishMethod(activityName, activityClass));
        generatedClass.addMethod(getContextParcelableExtraMethod(activityName, activityClass));
        generatedClass.addMethod(getContextParcelableExtraFinishMethod(activityName, activityClass));


        return generatedClass;
    }

    private MethodSpec getContextMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addStatement("$L.startActivity( new $T($L, $L))", "context", classIntent, "context", activityClass + ".class")
                .build();
    }


    private MethodSpec getContextFinishMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$L.startActivity( new $T($L, $L))", "context", classIntent, "context", activityClass + ".class")
                .beginControlFlow("if(finishCurrent)")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    private MethodSpec getContextBundleMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }


    private MethodSpec getContextBundleFinishMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
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


    private MethodSpec getContextStringExtraMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }

    private MethodSpec getContextStringExtraFinishMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
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


    private MethodSpec getContextSerializableExtraMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }


    private MethodSpec getContextSerializableExtraFinishMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
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

    private MethodSpec getContextParcelableExtraMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("$L.startActivity( intent )", "context")
                .build();
    }


    private MethodSpec getContextParcelableExtraFinishMethod(String activityName, ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + activityName)
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
