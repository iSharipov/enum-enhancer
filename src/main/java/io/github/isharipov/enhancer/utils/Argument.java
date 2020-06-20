package io.github.isharipov.enhancer.utils;

public class Argument {
    private final String name;
    private final String type;

    public Argument(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}