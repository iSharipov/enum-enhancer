/*
 * Copyright (C) 2020 Sharipov Ilia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import static io.github.isharipov.enhancer.utils.StringUtils.firstLower;
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

        pw.println();

        associate(pw);

        pw.println();

        enumList(pw);

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
            pw.println("        for (" + enhanceableElement.getElementName() + " " + firstLower(enhanceableElement.getElementName()) + " : " + enhanceableElement.getElementName() + ".values()) {");
            pw.println("            if (" + firstLower(enhanceableElement.getElementName()) + ".get" + capitalize(constructorArgument.getName()) + "().equals(" + constructorArgument.getName() + ")) {");
            pw.println("                return " + firstLower(enhanceableElement.getElementName()) + ";");
            pw.println("            }");
            pw.println("        }");
            pw.println("        throw new IllegalArgumentException(\"No constant found\");");
            pw.println("    }");
        });
    }

    private void associate(PrintWriter pw) {
        pw.println("    public static <E> java.util.Map<" + enhanceableElement.getElementName() + ", E> associate(java.util.function.Function<" + enhanceableElement.getElementName() + ", E> transform) {");
        pw.println("        return java.util.stream.Stream.of(" + enhanceableElement.getElementName() + ".values()).collect(java.util.stream.Collectors.toMap(" + uncapitalizeFirst(enhanceableElement.getElementName()) + " -> " + uncapitalizeFirst(enhanceableElement.getElementName()) + ", transform));");
        pw.println("    }");
    }

    private void enumList(PrintWriter pw) {
        pw.println("    public static java.util.List<" + enhanceableElement.getElementName() + "> enumList() {");
        pw.println("        return new java.util.ArrayList<>(java.util.Arrays.asList(" + enhanceableElement.getElementName() + ".values()));");
        pw.println("    }");
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