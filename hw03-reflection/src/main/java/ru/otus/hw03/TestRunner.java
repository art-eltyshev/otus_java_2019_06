package ru.otus.hw03;

import ru.otus.hw03.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void run(String classPath) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        System.out.println("Running test for " + classPath);
        System.out.println("-----------------------------");
        Class classToTest = Class.forName(classPath);

        List<Method> testMethods = new ArrayList<>();
        List<Method> beforeMethods = new ArrayList<>();
        List<Method> afterMethods = new ArrayList<>();
        List<Method> beforeAllMethods = new ArrayList<>();
        List<Method> afterAllMethods = new ArrayList<>();

        for (Method method : classToTest.getDeclaredMethods()) {
            if (method.getDeclaredAnnotation(Test.class) != null) {
                testMethods.add(method);
            } else if (method.getDeclaredAnnotation(Before.class) != null) {
                beforeMethods.add(method);
            } else if (method.getDeclaredAnnotation(After.class) != null) {
                afterMethods.add(method);
            } else if (method.getDeclaredAnnotation(BeforeAll.class) != null) {
                beforeAllMethods.add(method);
            } else if (method.getDeclaredAnnotation(AfterAll.class) != null) {
                afterAllMethods.add(method);
            }
        }

        int total = testMethods.size();
        int failed = 0;
        int success = 0;

        for (Method method : beforeAllMethods) {
            method.invoke(classToTest);
        }

        for (Method method : testMethods) {
            System.out.print(method.getName() + " – ");

            Object instance = classToTest.getDeclaredConstructor().newInstance();

            for (Method beforeMethod : beforeMethods) {
                beforeMethod.invoke(instance);
            }

            try {
                method.invoke(instance);
                success++;
                System.out.println("OK");
            } catch (Exception error) {
                failed++;
                System.out.println("FAILED");
            }

            for (Method afterMethod : afterMethods) {
                afterMethod.invoke(instance);
            }
        }

        for (Method method : afterAllMethods) {
            method.invoke(classToTest);
        }

        System.out.println("-----------------------------");
        System.out.println("Summary: total – " + total + ", success – " + success + ", failed – " + failed);
        System.out.println();
    }
}
