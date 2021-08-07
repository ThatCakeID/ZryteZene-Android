package com.thatcakeid.zrytezene;

import android.icu.text.CompactDecimalFormat;
import android.icu.util.ULocale;
import android.os.Build;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class HelperClass {
    public static String parseDuration(long duration) {
        return new DecimalFormat("0").format(duration / 60000)
                .concat(":".concat(new DecimalFormat("00").format((duration / 1000) % 60)));
    }

    // TODO: Stub
    public static String getPrettyDateFormat(Timestamp timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy").format(timestamp.toDate());
    }

    public static String getPrettyPlaysCount(Number plays) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return CompactDecimalFormat.getInstance(ULocale.getDefault(),
                    CompactDecimalFormat.CompactStyle.SHORT).format(plays) + " x played";
        }
        // TODO: Stub
        return NumberFormat.getInstance().format(plays) +  " x played";
    }
}
