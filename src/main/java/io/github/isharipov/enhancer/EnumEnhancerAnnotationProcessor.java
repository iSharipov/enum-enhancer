package io.github.isharipov.enhancer;


import io.github.isharipov.enhancer.utils.Argument;
import io.github.isharipov.enhancer.utils.EnhanceableElement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.isharipov.enhancer.utils.StringUtils.capitalize;
import static io.github.isharipov.enhancer.utils.StringUtils.uncapitalizeFirst;
import static javax.lang.model.SourceVersion.RELEASE_8;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationTypes({"io.github.isharipov.enhancer.EnumEnhancer"})
public class EnumEnhancerAnnotationProcessor extends AbstractProcessor {
    private ProcessingEnvironment pe;
    private EnhanceableElement enhanceableElement;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.pe = processingEnvironment;
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(EnumEnhancer.class);
        elements.forEach(this::enhanceEnum);
        return true;
    }

    private void enhanceEnum(Element element) {
        Objects.requireNonNull(element);
        enhanceableElement = new EnhanceableElement(
                pe.getElementUtils().getName(pe.getElementUtils().getPackageOf(element).getQualifiedName()).toString(),
                element.getSimpleName().toString()
        );
        if (isEnum(element)) {
            String sourceFileName = enhanceableElement.getSourceFileName();
            try {
                FileObject fo = pe.getFiler().createSourceFile(sourceFileName, element);
                OutputStream os = fo.openOutputStream();
                PrintWriter pw = new PrintWriter(os);
                pw.println(enhanceableElement.getPackageSignature());
                pw.println();
                pw.println(body(element));
                pw.flush();
                pw.close();
            } catch (IOException ex) {
                pe.getMessager().printMessage(Diagnostic.Kind.ERROR, "An I/O error occurred");
            }
        }
    }

    private StringBuilder body(Element element) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(enhanceableElement.getClassSignature());
        pw.println(" {");

        printEnumConstants(enumConstants(element), pw);

        pw.println();

        constructorArgumentsLookup(constructorArguments(element), pw);

        pw.println("}");
        return new StringBuilder(sw.getBuffer());
    }

    private List<String> enumConstants(Element element) {
        return element.getEnclosedElements().stream()
                .filter(this::isEnumConstant)
                .map(Element::getSimpleName)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private void printEnumConstants(List<String> enumConstants, PrintWriter pw) {
        enumConstants.forEach(enumConstant -> {
            String attribute = "public static final " +
                    String.class.getSimpleName() +
                    " " +
                    enumConstant +
                    " = " +
                    "\"" +
                    enumConstant +
                    "\"" +
                    ";";
            pw.println("    " + attribute);
        });
    }

    private List<Argument> constructorArguments(Element element) {
        return element.getEnclosedElements().stream()
                .filter(this::isConstructor)
                .map(executableElement -> (ExecutableElement) executableElement)
                .flatMap(variableElement -> variableElement.getParameters().stream())
                .map(argument -> new Argument(argument.getSimpleName().toString(), argument.asType().toString()))
                .collect(Collectors.toList());
    }

    private void constructorArgumentsLookup(List<Argument> constructorArguments, PrintWriter pw) {
        constructorArguments.forEach(constructorArgument -> {
            pw.print("    public static " + enhanceableElement.getElementName() + " from" + capitalize(constructorArgument.getName()) + "(" + constructorArgument.getType() + " " + constructorArgument.getName() + ")");
            pw.println(" {");
            pw.println("        for (" + enhanceableElement.getElementName() + " " + uncapitalizeFirst(enhanceableElement.getElementName()) + " : " + enhanceableElement.getElementName() + ".values()) {");
            pw.println("            if (" + uncapitalizeFirst(enhanceableElement.getElementName()) + ".get" + capitalize(constructorArgument.getName()) + "().equals(" + constructorArgument.getName() + ")) {");
            pw.println("                return " + uncapitalizeFirst(enhanceableElement.getElementName()) + ";");
            pw.println("            }");
            pw.println("        }");
            pw.println("        throw new IllegalArgumentException(\"No constant found\");");
            pw.println("    }");
        });
    }

    private boolean isEnum(Element element) {
        return element.getKind() == ElementKind.ENUM;
    }

    private boolean isEnumConstant(Element element) {
        return element.getKind() == ElementKind.ENUM_CONSTANT;
    }

    private boolean isConstructor(Element element) {
        return element.getKind() == ElementKind.CONSTRUCTOR;
    }
}