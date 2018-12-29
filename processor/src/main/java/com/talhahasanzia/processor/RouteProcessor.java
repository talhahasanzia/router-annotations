package com.talhahasanzia.processor;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.talhahasanzia.annotation.Routeable;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
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

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;
    private ClassName classModifyIntent;


    // init implementation, runs 1st time the processor starts
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {

        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();


    }

    // runs when processor is called by compiler
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        boolean isIntentModifierGenerated = false;
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

            TypeSpec.Builder generatedClass;

            // if IntentModifier is generated proceed to generate router
            // this is important since generated router class HAS to use IntentModifier and if not present will result in error
            if (isIntentModifierGenerated) {
                // generate a class
                generatedClass = generateClass(typeElement.getSimpleName().toString(),
                        pkg.getQualifiedName().toString());
            } else {
                // get current package of the caller
                classModifyIntent = ClassName.get(pkg.getQualifiedName().toString(), "IntentModifier");
                // generate interface in that package (in build directory)
                generatedClass = generateInterface();
                // mark as generated
                isIntentModifierGenerated = true;
            }
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
    @Nonnull
    private TypeSpec.Builder generateClass(String activityName, String packageName) {

        // Class specification
        TypeSpec.Builder generatedClass = TypeSpec
                .classBuilder(activityName + "Router")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // The class on which the annotation was used, here we expect it to be "Activity" class
        ClassName activityClass = ClassName.get(packageName, activityName);

        // add following method definitions to generated class
        generatedClass.addMethod(getContextMethod(activityClass));
        generatedClass.addMethod(getContextIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextFinishMethod(activityClass));
        generatedClass.addMethod(getContextFinishIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextBundleMethod(activityClass));
        generatedClass.addMethod(getContextBundleIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextBundleFinishMethod(activityClass));
        generatedClass.addMethod(getContextBundleFinishIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraFinishMethod(activityClass));
        generatedClass.addMethod(getContextStringExtraFinishIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraFinishMethod(activityClass));
        generatedClass.addMethod(getContextSerializableExtraFinishIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraIntentModifierMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraFinishMethod(activityClass));
        generatedClass.addMethod(getContextParcelableExtraFinishIntentModifierMethod(activityClass));
        generatedClass.addMethod(getActivityForResultMethod(activityClass));
        generatedClass.addMethod(getActivityForResultIntentModifierMethod(activityClass));

        // return generated class
        return generatedClass;
    }

    // simple route method that gets context and route to activity that annotation was used
    @Nonnull
    private MethodSpec getContextMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addStatement("context.getApplicationContext().startActivity( new $T($L, $L))", classIntent, "context", activityClass + ".class")
                .build();
    }

    // simple route method that gets context and route to activity that annotation was used.
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextFinishIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(boolean.class, "finishCurrent")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T($L, $L)", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if(finishCurrent)")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // simple route method that gets context and route to activity that annotation was used.
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    @Nonnull
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

    // simple route method that gets context and route to activity that annotation was used
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T($L, $L)", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }

    // route method with context and also a bundle that will be passed in the intent
    @Nonnull
    private MethodSpec getContextBundleMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }

    // route method with context and also a bundle that will be passed in the intent
    @Nonnull
    private MethodSpec getContextBundleIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }

    // route method with context and also a bundle that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    @Nonnull
    private MethodSpec getContextBundleFinishMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addParameter(boolean.class, "finishCurrent")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("context.getApplicationContext().startActivity( intent )", "context")
                .beginControlFlow("if(finishCurrent)")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }


    // route method with context and also a bundle that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextBundleFinishIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(classBundle, "bundle")
                .addParameter(boolean.class, "finishCurrent")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtras( bundle )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if (finishCurrent) ")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a String extra that will be passed in the intent
    @Nonnull
    private MethodSpec getContextStringExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("context.getApplicationContext().startActivity( intent )", "context")
                .build();
    }

    // route method with context and also a String extra that will be passed in the intent
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextStringExtraIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }

    // route method with context and also a String extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    @Nonnull
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
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }


    // route method with context and also a String extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextStringExtraFinishIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(String.class, "stringExtraData")
                .addParameter(boolean.class, "finishCurrent")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, stringExtraData )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }


    // route method with context and also a Serializable extra that will be passed in the intent
    @Nonnull
    private MethodSpec getContextSerializableExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }

    // route method with context and also a Serializable extra that will be passed in the intent
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextSerializableExtraIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }


    // route method with context and also a Serializable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    @Nonnull
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
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a Serializable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextSerializableExtraFinishIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classSerializable, "serializableExtra")
                .addParameter(boolean.class, "finishCurrent")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, serializableExtra )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a Parcelable extra that will be passed in the intent
    @Nonnull
    private MethodSpec getContextParcelableExtraMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }


    // route method with context and also a Parcelable extra that will be passed in the intent
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextParcelableExtraIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .build();
    }


    // route method with context and also a Parcelable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    @Nonnull
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
                .addStatement("context.getApplicationContext().startActivity( intent )", "context")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // route method with context and also a Parcelable extra that will be passed in the intent
    // also provides "finishCurrent" flag if true will finish current (caller) activity
    // this also accepts an Intent modifier which gives caller a way of customizing intent
    @Nonnull
    private MethodSpec getContextParcelableExtraFinishIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(String.class, "key")
                .addParameter(classParcelable, "parcelableExtra")
                .addParameter(boolean.class, "finishCurrent")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T( $L, $L )", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent.putExtra( key, parcelableExtra )")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("context.getApplicationContext().startActivity( intent )")
                .beginControlFlow("if( finishCurrent )")
                .addStatement("(($T)$L).finish()", classActivity, "context")
                .endControlFlow()
                .build();
    }

    // simple route for result method that gets context and route to activity using startActivityForResult that annotation was used.
    private MethodSpec getActivityForResultMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + "ForResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(int.class, "requestCode")
                .addStatement("(($T)$L).startActivityForResult( new $T($L, $L), requestCode)", classActivity, "context", classIntent, "context", activityClass + ".class")
                .build();
    }

    // simple route for result method that gets context and route to activity using startActivityForResult that annotation was used.
    private MethodSpec getActivityForResultIntentModifierMethod(ClassName activityClass) {
        return MethodSpec
                .methodBuilder(METHOD_PREFIX + "ForResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(classContext, "context")
                .addParameter(int.class, "requestCode")
                .addParameter(classModifyIntent, "intentModifier")
                .addStatement("$T intent = new $T($L, $L)", classIntent, classIntent, "context", activityClass + ".class")
                .addStatement("intent = intentModifier.modifyIntent( intent )")
                .addStatement("(($T)$L).startActivityForResult( intent, requestCode)", classActivity, "context")
                .build();
    }

    // generate an interface that can be used to modify intent
    @Nonnull
    private TypeSpec.Builder generateInterface() {
        // Class specification
        TypeSpec.Builder generatedClass = TypeSpec
                .interfaceBuilder("IntentModifier")
                .addModifiers(Modifier.PUBLIC);

        generatedClass.addMethod(MethodSpec
                .methodBuilder("modifyIntent")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(classIntent, "intent")
                .returns(classIntent)
                .build());


        return generatedClass;
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
