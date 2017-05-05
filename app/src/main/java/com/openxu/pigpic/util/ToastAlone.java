package com.openxu.pigpic.util;

import android.app.Application;
import android.widget.Toast;

/**
 * @author 
 *
 */
public class ToastAlone {
    private static Application mApp;
    /**
     * 唯一的toast
     */
    private static Toast mToast = null;

    public static void init(Application context) {
    	mApp = context;
    }
     

    /**
     * @param stringid
     * @param lastTime
     * @return
     */
    public static Toast showToast(int stringid, int lastTime) {
        if (mToast != null) {
            //mToast.cancel();
        } else {
            mToast = Toast.makeText(mApp, stringid, lastTime);
        }
        mToast.setText(stringid);
        mToast.show();
        return mToast;
    }

    /**
     * @param tips
     * @param lastTime
     * @return
     */
    public static Toast showToast(String tips, int lastTime) {
        if (mToast != null) {
            //mToast.cancel();
        } else {
            mToast = Toast.makeText(mApp, tips, lastTime);
        }
        mToast.setText(tips);
        mToast.show();
        return mToast;
    }

    public static void show(String text){
        if(null == mToast){
            mToast = Toast.makeText(mApp, text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }
    public static void show(int textRid){
        if(null == mToast){
            mToast = Toast.makeText(mApp, textRid, Toast.LENGTH_SHORT);
        }
        mToast.setText(textRid);
        mToast.show();
    }

}