package com.example.metalslug.util;

import java.util.Random;

/**
 * Created by Administrator on 2018/2/27.
 */

public class Util {
    public static Random random = new Random();
    //返回一个[0，range-1]的随机数
    public static int rand(int range) {
        //如果range为0，直接返回0
        if (range == 0) {
            return 0;
        }
        return Math.abs(random.nextInt() % range);
    }
}
