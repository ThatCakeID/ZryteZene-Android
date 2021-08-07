package com.thatcakeid.zrytezene;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.text.CompactDecimalFormat;
import android.icu.util.ULocale;
import android.os.Build;

import com.google.firebase.Timestamp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class HelperClass extends Application {

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            intent.putExtra("error", getStackTrace(ex));

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);


            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);

            uncaughtExceptionHandler.uncaughtException(thread, ex);
        });
    }

    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;

        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

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
