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
        int color;

        switch (BUILD_INFO) {
            case "dev":
                color = 0xFFB71C1C;
                break;

            case "beta":
                color = 0xFFE65100;
                break;

            case "stable":
                color = 0xFF2196F3;
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + BUILD_INFO);
        }

        watermark.setBackgroundColor(color);

        watermark.setTextColor(Color.parseColor("#FFFFFF"));
        root.setVisibility(SHOW_WATERMARK ? View.VISIBLE : View.GONE);
        watermark.setText(BUILD_INFO);
    }
}
