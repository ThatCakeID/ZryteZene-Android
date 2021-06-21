package com.thatcakeid.zrytezene;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a simple class that contains
 * the app's build extra information
 */
public class ExtraMetadata {
    public static boolean SHOW_WATERMARK = true;
    public static String BUILD_INFO = "dev";

    public static void setWatermarkColors(TextView watermark, LinearLayout root) {
        if (BUILD_INFO == "dev") {
            watermark.setBackgroundColor(Color.parseColor("#B71C1C"));
        }
        if (BUILD_INFO == "beta") {
            watermark.setBackgroundColor(Color.parseColor("#E65100"));
        }
        if (BUILD_INFO == "stable") {
            watermark.setBackgroundColor(Color.parseColor("#2196F3"));
        }
        watermark.setTextColor(Color.parseColor("#FFFFFF"));
        root.setVisibility(SHOW_WATERMARK ? View.VISIBLE : View.GONE);
        watermark.setText(BUILD_INFO);
    }
}
