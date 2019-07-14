package ru.otus.hw02;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        List<Integer> srcList = new DIYArrayList<>();
        List<Integer> dstList = new DIYArrayList<>();

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());

        for (int i = 0; i < 10; i++) {
            Collections.addAll(srcList, rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
            Collections.addAll(dstList, 0, 0, 0);
        }

        Collections.copy(dstList, srcList);

        System.out.println("srcList");
        System.out.println(Arrays.toString(srcList.toArray()));
        System.out.println("dstList");
        System.out.println(Arrays.toString(dstList.toArray()));

        Collections.sort(dstList);

        System.out.println("sorted dstList");
        System.out.println(Arrays.toString(dstList.toArray()));
    }
}
