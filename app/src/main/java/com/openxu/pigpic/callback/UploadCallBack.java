package com.openxu.pigpic.callback;

import android.os.Parcel;
import android.os.Parcelable;

import com.openxu.pigpic.bean.UploadPic;

/**
 * author : openXu
 * created time : 17/5/7 下午12:18
 * class name : UploadCallBack
 * discription :
 */
public interface UploadCallBack{

    void refreshPicPro(UploadPic pic);
}
