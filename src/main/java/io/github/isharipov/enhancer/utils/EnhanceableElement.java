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
