package ru.otus.hw03;

import ru.otus.hw03.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.otus.hw_common.ReflectionUtils.getMethodsWithAnnotation;
import static ru.otus.hw_common.ReflectionUtils.invokeMethodsSafe;

public class TestRunner {
    public static void run(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        printTestsRunHeader(className);

        Class classToTest = Class.forName(className);

        boolean successfulPreparation = invokeMethodsSafe(
                getMethodsWithAnnotation(classToTest, BeforeAll.class),
                classToTest,
                true,
                TestRunner::printInvokeMethodError
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
                        true,
                        TestRunner::printInvokeMethodError
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
                        false,
                        TestRunner::printInvokeMethodError
                );

                if (!successfulPreparation) {
                    break;
                }
            }
        }

        invokeMethodsSafe(
                getMethodsWithAnnotation(classToTest, AfterAll.class),
                classToTest,
                false,
                TestRunner::printInvokeMethodError
        );

        printTestsRunResults(testsResults);
        printTestsRunSummary(testsResults);
    }

    private static void printTestsRunHeader(String className) {
        System.out.println("Running tests from " + className + "\n");
    }

    private static Void printInvokeMethodError(Method method, Exception error) {
        System.err.println("Invoke method \"" + method.getName() + "\" failed:");
        error.printStackTrace();
        return null;
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

    enum TestStatus {
        SUCCESS,
        FAILED,
        SKIPPED
    }
}
