package io.github.isharipov.enhancer.utils;

public class EnhanceableElement {

    private static final String SUFFIX = "_";

    private final String elementPackage;
    private final String elementName;

    public EnhanceableElement(String elementPackage, String elementName) {
        this.elementPackage = elementPackage;
        this.elementName = elementName;
    }

    public String getElementPackage() {
        return elementPackage;
    }

    public String getElementName() {
        return elementName;
    }

    public String getSourceFileName() {
        return elementPackage + "." + elementName + SUFFIX;
    }

    public String getPackageSignature() {
        return "package " + elementPackage + ";";
    }

    public String getClassSignature() {
        return "public class " + elementName + SUFFIX;
    }

    @Override
    public String toString() {
        return "EnhanceableElement{" +
                "elementPackage='" + elementPackage + '\'' +
                ", elementName='" + elementName + '\'' +
                '}';
    }
}
