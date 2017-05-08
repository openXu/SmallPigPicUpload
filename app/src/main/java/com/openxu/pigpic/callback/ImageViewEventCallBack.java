package com.openxu.pigpic.callback;

import com.openxu.pigpic.bean.UploadPic;

/**
 * author : openXu
 * created time : 17/5/8 下午10:20
 * class name : ImageViewEventCallBack
 * discription :
 */
public interface ImageViewEventCallBack {

    //删除
    public void onDelete(UploadPic pic);
    //重新上传
    public void onReUpload(UploadPic pic);

}
