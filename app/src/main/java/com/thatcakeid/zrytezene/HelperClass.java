package com.thatcakeid.zrytezene;

import android.icu.text.CompactDecimalFormat;
import android.icu.util.ULocale;
import android.os.Build;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;

public class HelperClass {
    public static String parseDuration(long duration) {
        return new DecimalFormat("0").format(duration / 60000)
                .concat(":".concat(new DecimalFormat("00").format((duration / 1000) % 60)));
    }

    // TODO: Stub
    public static String getPrettyDateFormat(Timestamp timestamp) {
        return "0 seconds ago";
    }

    public static String getPrettyPlaysCount(Number plays) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return CompactDecimalFormat.getInstance(ULocale.getDefault(),
                    CompactDecimalFormat.CompactStyle.SHORT).format(plays) + " x played";
        }
        // TODO: Stub
        return "0 x played";
    }
}
