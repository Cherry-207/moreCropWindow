package com.cherry.cropper.utils;

import android.util.Log;

/**
 * Created by lucien on 08/05/2017.
 */

public class SimpleLog {

    public static void d(String tag, String msg) {
        if (ContextBridge.isOpenDebug()) {
            Log.d(tag, msg);
        }
    }

    public static void d(boolean isShow, String tag, String msg) {
        if (ContextBridge.isOpenDebug() && isShow) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (ContextBridge.isOpenDebug()) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (ContextBridge.isOpenDebug()) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (ContextBridge.isOpenDebug()) {
            Log.w(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (ContextBridge.isOpenDebug()) {
            Log.v(tag, msg);
        }
    }

    public static void e(Exception e) {
        if (ContextBridge.isOpenDebug()) {
            e.printStackTrace();
        }
    }
}