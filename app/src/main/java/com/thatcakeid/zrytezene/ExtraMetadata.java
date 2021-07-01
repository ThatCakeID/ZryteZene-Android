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
    public static final boolean SHOW_WATERMARK = true;
    public static final String BUILD_INFO = "dev";

    @SuppressWarnings("ALL")
    public static void setWatermarkColors(TextView watermark, LinearLayout root) {
        if (BUILD_INFO.equals("dev")) {
            watermark.setBackgroundColor(Color.parseColor("#B71C1C"));
        }
        if (BUILD_INFO.equals("beta")) {
            watermark.setBackgroundColor(Color.parseColor("#E65100"));
        }
        if (BUILD_INFO.equals("stable")) {
            watermark.setBackgroundColor(Color.parseColor("#2196F3"));
        }
        watermark.setTextColor(Color.parseColor("#FFFFFF"));
        root.setVisibility(SHOW_WATERMARK ? View.VISIBLE : View.GONE);
        watermark.setText(BUILD_INFO);
    }
}
