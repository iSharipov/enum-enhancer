package io.github.isharipov.enhancer;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;

@RunWith(JUnit4.class)
public class EnumEnhancerTest {

    @Test
    public void testEnumEnhancerAnnotation() {
        Compilation compilation = javac()
                .withProcessors(new EnumEnhancerAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("Quarter", "package io.github.isharipov.enhancer;\n" +
                        "\n" +
                        "import io.github.isharipov.enhancer.EnumEnhancer;\n" +
                        "\n" +
                        "@EnumEnhancer\n" +
                        "public enum Quarter {\n" +
                        "    Q1(1),\n" +
                        "    Q2(2),\n" +
                        "    Q3(3),\n" +
                        "    Q4(4);\n" +
                        "\n" +
                        "    private final int quarter;\n" +
                        "\n" +
                        "    Quarter(int quarter) {\n" +
                        "        this.quarter = quarter;\n" +
                        "    }\n" +
                        "\n" +
                        "    public int getQuarter() {\n" +
                        "        return quarter;\n" +
                        "    }\n" +
                        "}"));
        Assert.assertSame(compilation.status(), Compilation.Status.SUCCESS);
        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        Assert.assertFalse(generatedFiles.isEmpty());
        Assert.assertEquals(3, generatedFiles.size());
        Assert.assertTrue(generatedFiles.stream().anyMatch(file -> file.getName().contains("Quarter_.class")));
    }
}
