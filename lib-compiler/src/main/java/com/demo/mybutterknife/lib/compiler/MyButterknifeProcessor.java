package com.demo.mybutterknife.lib.compiler;

import com.demo.mybutterknife.lib.annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.demo.mybutterknife.lib.annotation.BindView"})
public class MyButterknifeProcessor extends AbstractProcessor {

    javax.lang.model.util.Elements elementsUtils;
    Filer filer;

    static final ClassName I_UNBINDER = ClassName.get("com.demo.mybutterknife.lib.annotation", "Unbinder");
    static final ClassName VIEW = ClassName.get("android.view", "View");

    Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        messager = processingEnvironment.getMessager();
        elementsUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (set != null && set.size() > 0){

            Set<? extends Element> bindViews = roundEnvironment.getElementsAnnotatedWith(BindView.class);
            processBindViews(bindViews);
            return true;
        }

        return false;
    }

    private void processBindViews(Set<? extends Element> bindViews) {
        HashMap<String, List<Element>> bindViewsMap = new HashMap<>();
        for(Element bindViewElement : bindViews){
            TypeElement element = (TypeElement)bindViewElement.getEnclosingElement();
            String qName = element.getQualifiedName().toString();
            List<Element> elements = bindViewsMap.get(qName);
            if(elements == null){
                elements = new ArrayList<>();
                bindViewsMap.put(qName, elements);
            }
            elements.add(bindViewElement);
        }
        for(Map.Entry<String, List<Element>> entry : bindViewsMap.entrySet()){
            String k = entry.getKey();
            List<Element> v = entry.getValue();

            TypeElement classElement = this.elementsUtils.getTypeElement(k);
            ClassName className = ClassName.get(classElement);

            //为了兼容内部类，所以要用以下写法，不能直接simpleName()
            String classNameStr = k.replace(className.packageName()+".", "");
            classNameStr = classNameStr.replace(".", "$");
            classNameStr = classNameStr + "_ViewBinding";

            TypeSpec.Builder builder = TypeSpec.classBuilder(classNameStr);
            builder.addModifiers(Modifier.PUBLIC);
            builder.addSuperinterface(I_UNBINDER);
            builder.addField(FieldSpec.builder(className, "target", Modifier.PUBLIC).build());

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
            constructorBuilder.addModifiers(Modifier.PUBLIC);
            constructorBuilder.addParameter(className, "target");
            constructorBuilder.addParameter(VIEW, "source");
            constructorBuilder.addStatement("this.target = target");

            MethodSpec.Builder unbinderMethodBuilder = MethodSpec.methodBuilder("unbind");
            unbinderMethodBuilder.addModifiers(Modifier.PUBLIC);
            unbinderMethodBuilder.addAnnotation(Override.class);
            unbinderMethodBuilder.addStatement("$T target = this.target", className);
            unbinderMethodBuilder.addStatement("if(target == null) throw new IllegalStateException($S)","Bindings already cleared.");
            unbinderMethodBuilder.addStatement("this.target = null");

            for (Element element : v) {
                constructorBuilder.addStatement("target.$N = ($T)source.findViewById($L)",
                        element.getSimpleName(),
                        TypeName.get(element.asType()),
                        element.getAnnotation(BindView.class).value());

                unbinderMethodBuilder.addStatement("target.$N = null", element.getSimpleName());
            }

            builder.addMethod(constructorBuilder.build());
            builder.addMethod(unbinderMethodBuilder.build());

            try {
                JavaFile.builder(className.packageName(), builder.build()).build().writeTo(this.filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
