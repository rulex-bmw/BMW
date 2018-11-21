package com.rulex.bsb;

import java.util.Arrays;
import java.util.List;

public class Lambda {
    public static void main(String[] args) {
        Integer[] s = {1, 2, 3, 4, 5, 6, 7};
        List<Integer> integers = Arrays.asList(s);
        integers.forEach((p) -> System.out.println(p));
        integers.forEach(System.out::println);


    }


}
