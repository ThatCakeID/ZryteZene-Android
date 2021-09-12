package com.thatcakeid.zrytezene

import android.content.Intent
import com.thatcakeid.zrytezene.DebugActivity
import android.app.PendingIntent
import android.app.AlarmManager
import android.app.Application
import android.os.Build
import android.icu.text.CompactDecimalFormat
import android.icu.util.ULocale
import android.os.Process
import com.google.firebase.Timestamp
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class HelperClass : Application() {
    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    override fun onCreate() {
        super.onCreate()
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread?, ex: Throwable ->
            val intent = Intent(applicationContext, DebugActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("error", getStackTrace(ex))
            val pendingIntent = PendingIntent.getActivity(applicationContext, 11111, intent, PendingIntent.FLAG_ONE_SHOT)
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            am[AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000] = pendingIntent
            Process.killProcess(Process.myPid())
            System.exit(2)
            uncaughtExceptionHandler.uncaughtException(thread, ex)
        }
    }

    private fun getStackTrace(th: Throwable): String {
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        var cause: Throwable? = th
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        val stacktraceAsString = result.toString()
        printWriter.close()
        return stacktraceAsString
    }

    companion object {
        @JvmStatic
        fun parseDuration(duration: Long): String {
            return DecimalFormat("0").format(duration / 60000)
            + ":" + DecimalFormat("00").format(duration / 1000 % 60)
        }

        // TODO: Stub
        @JvmStatic
        fun getPrettyDateFormat(timestamp: Timestamp): String {
            return SimpleDateFormat("dd/MM/yyyy").format(timestamp.toDate())
        }

        @JvmStatic
        fun getPrettyPlaysCount(plays: Number?): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CompactDecimalFormat.getInstance(ULocale.getDefault(),
                        CompactDecimalFormat.CompactStyle.SHORT).format(plays) + " x played"
            } else NumberFormat.getInstance().format(plays) + " x played"
            // TODO: Stub
        }
    }
}