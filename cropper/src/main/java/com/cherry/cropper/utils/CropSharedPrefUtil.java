package com.cherry.cropper.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lucien on 15/05/2018.
 */

public class CropSharedPrefUtil {

    private static final String NAME = "common_pref";
    private static SharedPreferences sp;

    public static final String SHOW_CAMERA_GUIDE = "show_camera_guide"; // 标记是否显示过拍照引导
    public static final String NOT_SHOW_CAMERA_REVERSAL_GUIDE = "not_show_camera_reversal_guide"; // 标记是否显示过设备翻转引导
    public static final String SHOW_SELECT_TITLE_GUIDE_VIEW = "show_select_title_guide_view"; // 标记是否显示过添加框题剪切框引导
    public static final String SHOW_SELECT_IMAGE_GUIDE_VIEW = "show_select_image_guide_view"; // 标记是否显示过添加框图剪切框引导
    public static final String GRADE_SUBJECT = "okay_sp_grade_subject";

    public static void setValue(Context context, String name, String value) {
        sp = context.getSharedPreferences(NAME, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(name, value);
        edit.apply();
    }

    public static void setValue(Context context, String name, int value) {
        sp = context.getSharedPreferences(NAME, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(name, value);
        edit.apply();
    }

    public static void setValue(Context context, String name, boolean value) {
        sp = context.getSharedPreferences(NAME, 0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(name, value);
        edit.apply();
    }

    public static int getIntValue(Context context, String name) {
        sp = context.getSharedPreferences(NAME, 0);
        return sp.getInt(name, 0);
    }

    public static String getStringValue(Context context, String name) {
        sp = context.getSharedPreferences(NAME, 0);
        return sp.getString(name, "");
    }

    public static boolean getBooleanValue(Context context, String name) {
        sp = context.getSharedPreferences(NAME, 0);
        return sp.getBoolean(name, false);
    }

    private static int getMutiProcessMode() {
        return Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS;
    }



}
