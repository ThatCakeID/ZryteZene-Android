package com.thatcakeid.zrytezene;

import java.text.DecimalFormat;

public class HelperClass {
    public static String parseDuration(long duration) {
        return new DecimalFormat("0").format(duration / 60000)
                .concat(":".concat(new DecimalFormat("00").format((duration / 1000) % 60)));
    }
}
