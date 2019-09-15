package ru.otus.hw03;

import ru.otus.hw03.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestRunner {
    public static void run(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        printTestsRunHeader(className);

        Class classToTest = Class.forName(className);

        boolean successfulPreparation = invokeMethodsSafe(
                getMethodsWithAnnotation(classToTest, BeforeAll.class),
                classToTest,
                true
        );

        List<Method> testMethods = getMethodsWithAnnotation(classToTest, Test.class);
        Map<Method, TestStatus> testsResults = testMethods.stream()
                .collect(Collectors.toMap(x -> x, x -> TestStatus.SKIPPED));

        if (successfulPreparation) {
            for (Method method : testMethods) {
                Object instance = classToTest.getDeclaredConstructor().newInstance();

                successfulPreparation = invokeMethodsSafe(
                        getMethodsWithAnnotation(classToTest, Before.class),
                        instance,
                        true
                );

                if (successfulPreparation) {
                    try {
                        method.invoke(instance);
                        testsResults.put(method, TestStatus.SUCCESS);
                    } catch (Exception error) {
                        testsResults.put(method, TestStatus.FAILED);
                    }
                }

                invokeMethodsSafe(
                        getMethodsWithAnnotation(classToTest, After.class),
                        instance,
                        false
                );

                if (!successfulPreparation) {
                    break;
                }
            }
        }

        invokeMethodsSafe(
                getMethodsWithAnnotation(classToTest, AfterAll.class),
                classToTest,
                false
        );

        printTestsRunResults(testsResults);
        printTestsRunSummary(testsResults);
    }

    private static boolean invokeMethodsSafe(List<Method> methods, Object obj, boolean stopOnError) {
        boolean isSuccess = true;

        for (Method method : methods) {
            try {
                method.invoke(obj);
            } catch (Exception error) {
                isSuccess = false;
                printInvokeMethodError(method, error);

                if (stopOnError) {
                    break;
                }
            }
        }

        return isSuccess;
    }

    private static List<Method> getMethodsWithAnnotation(Class clazz, Class annotation) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(annotation) != null)
                .collect(Collectors.toList());
    }

    private static void printTestsRunHeader(String className) {
        System.out.println("Running tests from " + className + "\n");
    }

    private static void printInvokeMethodError(Method method, Exception error) {
        System.err.println("Invoke method \"" + method.getName() + "\" failed:");
        error.printStackTrace();
    }

    private static void printTestsRunResults(Map<Method, TestStatus> testsResults) {
        testsResults.forEach((method, testStatus) -> System.out.println(method.getName() + " – " + testStatus.name()));
    }

    private static void printTestsRunSummary(Map<Method, TestStatus> testsResults) {
        int total = testsResults.size();
        long success = testsResults
                .entrySet()
                .stream()
                .filter(x -> x.getValue() == TestStatus.SUCCESS)
                .count();

        System.out.println(
                "\nSummary: total – " + total + ", success – " + success + ", failed – " + (total - success) + "\n"
        );
    }
}

enum TestStatus {
    SUCCESS,
    FAILED,
    SKIPPED
}
