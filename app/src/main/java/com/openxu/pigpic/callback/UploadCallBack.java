package com.openxu.pigpic.callback;

import android.os.Parcel;
import android.os.Parcelable;

import com.openxu.pigpic.bean.UploadPic;

/**
 * author : openXu
 * created time : 17/5/7 下午12:18
 * class name : UploadCallBack
 * discription : service中图片上传状态变化时刷新view的回调
 */
public interface UploadCallBack{

    void refreshPicPro(UploadPic pic);
}
