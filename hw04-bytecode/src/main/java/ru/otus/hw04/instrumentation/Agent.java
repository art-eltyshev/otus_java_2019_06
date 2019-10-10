package ru.otus.hw04.instrumentation;

import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) {
                List<MethodInfo> methodsWithAnnotations = findMethodsWithAnnotations(classfileBuffer);

                if (methodsWithAnnotations.isEmpty()) {
                    return classfileBuffer;
                }

                return buildClassWithProxiedMethods(className, classfileBuffer, methodsWithAnnotations);
            }
        });
    }

    private static List<MethodInfo> findMethodsWithAnnotations(byte[] originalClass) {
        List<MethodInfo> methods = new ArrayList<>();

        ClassReader cr = new ClassReader(originalClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodInfo methodInfo = new MethodInfo(access, name, descriptor, signature, exceptions);

                return new MethodVisitor(api) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                        if (descriptor.equals("Lru/otus/hw04/annotations/Log;")) {
                            methods.add(methodInfo);
                        }

                        return super.visitAnnotation(descriptor, visible);
                    }
                };
            }
        }, Opcodes.ASM5);

        return methods;
    }

    private static byte[] buildClassWithProxiedMethods(String className, byte[] originalClass, List<MethodInfo> methods) {
        String classShortName = className.substring(className.lastIndexOf("/") + 1);
        ClassReader cr = new ClassReader(originalClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                Optional<MethodInfo> method = methods
                        .stream()
                        .filter((methodInfo -> methodInfo.equals(access, name, descriptor)))
                        .findFirst();

                if (method.isPresent()) {
                    return super.visitMethod(access, method.get().getNameForProxyMethod(), descriptor, signature, exceptions);
                } else {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }
        }, Opcodes.ASM5);

        Handle handle = new Handle(
                H_INVOKESTATIC,
                Type.getInternalName(java.lang.invoke.StringConcatFactory.class),
                "makeConcatWithConstants",
                MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, String.class, Object[].class).toMethodDescriptorString(),
                false
        );

        for (MethodInfo method : methods) {
            Type[] argumentTypes = Type.getArgumentTypes(method.descriptor);
            Type returnType = Type.getReturnType(method.descriptor);

            MethodVisitor mv = cw.visitMethod(method.access, method.name, method.descriptor, method.signature, method.exceptions);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

            loadVariables(argumentTypes, mv);

            mv.visitInvokeDynamicInsn(
                    "makeConcatWithConstants",
                    "(" + getArgumentsDescriptor(argumentTypes) + ")Ljava/lang/String;",
                    handle,
                    buildLogString(method, argumentTypes)
            );
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            loadVariables(argumentTypes, mv);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, method.getNameForProxyMethod(), method.descriptor, false);

            mv.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        byte[] finalClass = cw.toByteArray();

        try (OutputStream fos = new FileOutputStream(classShortName + ".class")) {
            fos.write(finalClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return finalClass;
    }

    private static void loadVariables(Type[] types, MethodVisitor methodVisitor) {
        for (int i = 0; i < types.length; i++) {
            methodVisitor.visitVarInsn(types[i].getOpcode(Opcodes.ILOAD), i + 1);
        }
    }

    private static String getArgumentsDescriptor(Type[] argumentTypes) {
        return Arrays.stream(argumentTypes).map((Type::toString)).collect(Collectors.joining());
    }

    private static String buildLogString(MethodInfo method, Type[] argumentTypes) {
        return "Executing method: " + method.name + ", params: " +
                Arrays.stream(argumentTypes)
                        .map((type) -> "\u0001")
                        .collect(Collectors.joining(", "));
    }

    private static class MethodInfo {
        public int access;
        public String name;
        public String descriptor;
        public String signature;
        public String[] exceptions;

        public MethodInfo(int access, String name, String descriptor, String signature, String[] exceptions) {
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        public String getNameForProxyMethod() {
            return "orig__" + this.name;
        }

        public boolean equals(int access, String name, String descriptor) {
            return this.access == access && this.name.equals(name) && this.descriptor.equals(descriptor);
        }
    }
}
