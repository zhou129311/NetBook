package com.xzhou.book.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Log {

    //    public static final boolean LOG_DEBUG = BuildConfig.DEBUG;
    public static final boolean LOG_DEBUG = true;

    private static final String TAG = "zx";
    private static ExecutorService mWorker = Executors.newSingleThreadExecutor();

    public static void i(String msg) {
        i("NO_TAG", msg);
    }

    public static void w(String msg) {
        i("NO_TAG", msg);
    }
    public static void e(String msg) {
        i("NO_TAG", msg);
    }
    public static void d(String msg) {
        i("NO_TAG", msg);
    }

    public static void v(String tag, String msg) {
        if (LOG_DEBUG) {
            msg = formatMsg(tag, msg);
            android.util.Log.v(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LOG_DEBUG) {
            msg = formatMsg(tag, msg);
            android.util.Log.i(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOG_DEBUG) {
            msg = formatMsg(tag, msg);
            android.util.Log.d(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LOG_DEBUG) {
            msg = formatMsg(tag, msg);
            android.util.Log.w(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        msg = formatMsg(tag, msg);
        android.util.Log.e(TAG, msg);
    }

    public static void e(String tag, Throwable t) {
        e(tag, getStackMsg(t));
    }

    public static void e(String tag, Exception e) {
        e(tag, getStackMsg(e));
    }

    public static void e(String tag, String message, Throwable t) {
        String msg = formatMsg(tag, getStackMsg(t));
        android.util.Log.e(TAG, message);
        android.util.Log.e(TAG, msg, t);
    }

    private static String getStackMsg(Exception e) {
        StringBuilder sb = new StringBuilder(e.toString()).append("\n");
        StackTraceElement[] stackArray = e.getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private static String getStackMsg(Throwable e) {

        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private static String formatMsg(String tag, String msg) {
        return "[" + getPid() + "]" +
                "[" + tag + "] [thread=" + String.valueOf(Thread.currentThread().getId()) + "] ==>" + msg;
    }

    private static String getPid() {
        return "pid:" + android.os.Process.myPid() + " tid:" + android.os.Process.myTid();
    }

    public static void writeSD(final String tag, final String msg) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                FileWriter fw = null;
                try {
                    File file = new File("/sdcard/", "apk_exception.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fw = new FileWriter(file, true);
                    StringBuffer sb = new StringBuffer();
                    sb.append(tag).append("\n").append(msg);
                    fw.write(sb.toString() + "\n");
                    fw.flush();
                } catch (IOException e) {
                    android.util.Log.e("FileLog", "Store: " + e.getMessage(), e);
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
    }

}
