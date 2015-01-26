package io.getsurge.android.utils;

/**
 * Created by Gil on 17/01/15.
 */
public class Utils {

    public static int random(int min, int max) {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }
}
