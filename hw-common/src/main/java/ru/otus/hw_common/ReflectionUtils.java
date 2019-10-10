package ru.otus.hw_common;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static boolean invokeMethodsSafe(
            List<Method> methods,
            Object obj,
            boolean stopOnError,
            BiFunction<Method, Exception, Void> onInvokeError) {
        boolean isSuccess = true;

        for (Method method : methods) {
            try {
                method.invoke(obj);
            } catch (Exception error) {
                isSuccess = false;
                onInvokeError.apply(method, error);

                if (stopOnError) {
                    break;
                }
            }
        }

        return isSuccess;
    }

    public static List<Method> getMethodsWithAnnotation(Class clazz, Class annotation) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(annotation) != null)
                .collect(Collectors.toList());
    }
}
