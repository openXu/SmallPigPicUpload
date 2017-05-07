package com.openxu.pigpic.util;

import android.util.Log;


public class LogUtil {
    // 默认不打印log
    private static boolean mIsShowLog = true;


    public static void v(String tag, String msg) {
        if (mIsShowLog){
        	msg=formNull(msg);
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {

        if (mIsShowLog){
        	msg=formNull(msg);
            Log.d(tag, msg);
        }
    }


    public static void i(String tag, String msg) {
        if (mIsShowLog){
        	msg=formNull(msg);
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (mIsShowLog){
        	msg=formNull(msg);
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (mIsShowLog){
        	msg=formNull(msg);
        	Log.e(tag, msg);
        }
           
    }
    //对null值进行替换
    private static String formNull(String value){
    	return value==null ?"null":value;
    }
}
