package ru.otus.hw04;

import ru.otus.hw04.annotations.Log;

public class MyClass {
    @Log
    public int sum(int x, int y) {
        return x + y;
    }

    @Log
    public float sum(float x, float y) {
        return x + y;
    }

    @Log
    public String concat(String s1, String s2) {
        return s1 + s2;
    }
}
