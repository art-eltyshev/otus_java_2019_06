package ru.otus.hw04;

/*
    java -javaagent:target/hw04-bytecode-jar-with-dependencies.jar -jar target/hw04-bytecode-jar-with-dependencies.jar
 */
public class Main {
    public static void main(String[] args) {
        MyClass myClass = new MyClass();
        System.out.println(myClass.sum(1, 2));
        System.out.println(myClass.sum((float)3.1, (float)4.2));
        System.out.println(myClass.concat("Hello", "World"));
    }
}
