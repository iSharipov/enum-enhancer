# EnumEnhancer

![Java CI with Maven](https://github.com/iSharipov/enum-enhancer/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)
<a href="https://codecov.io/gh/iSharipov/enum-enhancer">
  <img src="https://codecov.io/gh/iSharipov/enum-enhancer/branch/master/graph/badge.svg" />
</a>
<a href="https://lgtm.com/projects/g/iSharipov/enum-enhancer/alerts/">
    <img alt="Total alerts" src="https://img.shields.io/lgtm/alerts/g/iSharipov/enum-enhancer.svg?logo=lgtm&logoWidth=18"/>
</a>
<a href="https://lgtm.com/projects/g/iSharipov/enum-enhancer/context:java">
    <img alt="Language grade: Java" src="https://img.shields.io/lgtm/grade/java/g/iSharipov/enum-enhancer.svg?logo=lgtm&logoWidth=18"/>
</a>

## What is EnumEnhancer

EnumEnhancer is a Java <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/apt/">annotation processor</a> 
for the generation of utility class for <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html">Java Enum</a> class.

To enhance enum annotate it like this:

```java
@EnumEnhancer
public enum Quarter {
    Q1(1),
    Q2(2),
    Q3(3),
    Q4(4);

    private final int quarter;

    Quarter(int quarter) {
        this.quarter = quarter;
    }

    public int getQuarter() {
        return quarter;
    }
}
```

### Result

```java
public class Quarter_ {
    public static final String Q1 = "Q1";
    public static final String Q2 = "Q2";
    public static final String Q3 = "Q3";
    public static final String Q4 = "Q4";

    public static Quarter fromQuarter(int quarter) {
        for (Quarter q : Quarter.values()) {
            if (q.getQuarter() == quarter) {
                return q;
            }
        }
        throw new IllegalArgumentException("No constant found");
    }

    public static <E> java.util.Map<Quarter, E> associate(java.util.function.Function<Quarter, E> transform) {
        return java.util.stream.Stream.of(Quarter.values()).collect(java.util.stream.Collectors.toMap(quarter -> quarter, transform));
    }

    public static java.util.List<Quarter> enumList() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(Quarter.values()));
    }

    public static java.util.Map<String, Quarter> enumMap() {
        return java.util.stream.Stream.of(Quarter.values()).collect(java.util.stream.Collectors.toMap(Enum::name, quarter -> quarter));
    }
}
```
At compile time EnumEnhancer will generate a utility class. 
The generated class uses plain Java fields and methods, no reflection is involved.

## EnumEnhancer can be applied

- [x] Polymorphic Deserialization using Jackson<br>
We can't use enum's name's because of - An annotation argument must be a compile-time constant

Instead of using this
```java
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "quarter",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FirstQuarterDto.class,  name = Quarter.QuarterValue.Q1),
    @JsonSubTypes.Type(value = SecondQuarterDto.class, name = Quarter.QuarterValue.Q2),
    @JsonSubTypes.Type(value = ThirdQuarterDto.class,  name = Quarter.QuarterValue.Q3),
    @JsonSubTypes.Type(value = FourthQuarterDto.class, name = Quarter.QuarterValue.Q4)
})
``` 

where Quarter.QuarterValue.Q1-4 are:

```java
public enum Quarter {
    Q1(1, QuarterValue.Q1),
    Q2(2, QuarterValue.Q2),
    Q3(3, QuarterValue.Q3),
    Q4(4, QuarterValue.Q4);

    public static class QuarterValue{
        public static String Q1 = "Q1";
        public static String Q2 = "Q2";
        public static String Q3 = "Q3";
        public static String Q4 = "Q4";
    }
}
```

use the class Quarter_:

```java
@JsonSubTypes({
    @JsonSubTypes.Type(value = FirstQuarterDto.class,  name = Quarter_.Q1),
    @JsonSubTypes.Type(value = SecondQuarterDto.class, name = Quarter_.Q2),
    @JsonSubTypes.Type(value = ThirdQuarterDto.class,  name = Quarter_.Q3),
    @JsonSubTypes.Type(value = FourthQuarterDto.class, name = Quarter_.Q4)
})   
```
- [x] Getting enum associated with<br>
Sometimes we need a Map with enum and associated values

```java
Map<Quarter, Integer> associated = Quarter_.associate(Quarter::getQuarter);
```
```java
Map<Quarter, String> associated = Quarter_.associate(quarter -> {
    String months;
    switch (quarter){
        case Q1: months = "January, February, and March";    break;
        case Q2: months = "April, May, and June ";           break;
        case Q3: months =  "July, August, and September";    break;
        case Q4: months = "October, November, and December"; break;
        default:
            throw new IllegalStateException("Unexpected value: " + quarter);
    }
    return months;
});    
```

- [x] Getting List or Map of enum

```java
List<Quarter> quarters = Quarter_.enumList();
```
```java
Map<String, Quarter> quarters = Quarter_.enumMap();
```

- [x] Getting enum by constructor parameter<br>
Processor generates **fromParameter** method for each constructor parameter
```java
Quarter quarter = Quarter_.fromQuarter(1);
```
## Using EnumEnhancer
EnumEnhancer works in command line builds and IDEs.
### Gradle
```groovy
buildscript {
    dependencies {
        classpath("net.ltgt.gradle:gradle-apt-plugin:0.21")
    }
}     
apply plugin: 'net.ltgt.apt'

dependencies {
    compileOnly("io.github.isharipov:enum-enhancer:0.1")
    annotationProcessor("io.github.isharipov:enum-enhancer:0.1")
}
```
### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.isharipov</groupId>
        <artifactId>enum-enhancer</artifactId>
        <version>0.1</version>
        <scope>compile</scope>
    </dependency>
<dependencies>
```

## Requirements
EnumEnhancer requires Java 1.8 or later.
