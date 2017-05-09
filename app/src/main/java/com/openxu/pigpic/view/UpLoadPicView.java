package com.openxu.pigpic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.pigpic.ChoosePhotoActivity;
import com.openxu.pigpic.R;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.ImageViewEventCallBack;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;

import java.util.ArrayList;

/**
 * author : openXu
 * created time : 17/5/7 下午3:51
 * class name : UpLoadPicView
 * discription :此类作废
 */
public class UpLoadPicView extends RelativeLayout implements View.OnClickListener{

    private String TAG = "UpLoadPicView";

    private ImageView iv_image, iv_fial, iv_del;
    private RelativeLayout rl_item, rl_zz;
    private UploadPicProView proview;

    private UploadPic picBean;


    public static int STATUS_SHOW = 1;
    public static int STATUS_EIDT = 2;
    public int status = STATUS_SHOW;   //控件状态

    private ImageViewEventCallBack eventCallBack;

    public UpLoadPicView(Context context) {
        this(context, null);
    }
    public UpLoadPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public UpLoadPicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.uploadpic_grid_item, this);

        rl_item = (RelativeLayout)findViewById(R.id.rl_item);

        proview = (UploadPicProView)findViewById(R.id.proview);
        iv_image = (ImageView)findViewById(R.id.iv_image);
        iv_fial = (ImageView)findViewById(R.id.iv_fial);
        iv_del = (ImageView)findViewById(R.id.iv_del);
        rl_zz = (RelativeLayout)findViewById(R.id.rl_zz);

        rl_zz.setOnClickListener(this);
        iv_fial.setOnClickListener(this);
        iv_del.setOnClickListener(this);
    }

    public void setViewData(UploadPic pic){
        picBean = pic;
        setTag(picBean.getKey());

        if(!TextUtils.isEmpty(picBean.getUrl())){
            //从服务器上获取的图片
            picBean.setStatus(UploadPic.STATUS_SUCC);
            ImageLoader.getInstance().displayImage(picBean.getUrl(), iv_image);
//            LogUtil.v(TAG, "展示服务器上图片:"+picBean.getUrl());
        }else{
            ImageLoader.getInstance().displayImage("file://" + picBean.getPath(), iv_image);
        }

        int status = picBean.getStatus();
        switch (status){
            case UploadPic.STATUS_UPLODING:
                rl_zz.setVisibility(View.VISIBLE);
                proview.setVisibility(View.VISIBLE);
                iv_fial.setVisibility(View.GONE);
                break;
            case UploadPic.STATUS_SUCC:
                proview.setVisibility(View.GONE);
                iv_fial.setVisibility(View.GONE);
                rl_zz.setVisibility(View.GONE);
                break;
            case UploadPic.STATUS_FAIL:
                rl_zz.setVisibility(View.VISIBLE);
                proview.setVisibility(View.GONE);
                iv_fial.setVisibility(View.VISIBLE);
                break;
        }

        //编辑状态
        if(this.status == STATUS_EIDT){
            iv_del.setVisibility(View.VISIBLE);
        }else{
            iv_del.setVisibility(View.GONE);
        }

        invalidate();
        if(picBean.getStatus()==UploadPic.STATUS_UPLODING)
            proview.invalidate();
    }

    public void setEventCallBack(ImageViewEventCallBack callBack) {
        this.eventCallBack = callBack;
    }
    public void setStatus(int status) {
        this.status = status;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec. getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        proview.measure(width, height);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_zz:
                if(picBean.getStatus()==UploadPic.STATUS_FAIL &&eventCallBack != null) {
                    eventCallBack.onDelete(picBean);
                }
                break;
            case R.id.iv_del:
                if(status == STATUS_EIDT && eventCallBack!=null){
//                    LogUtil.i(TAG, eventCallBack+"删除"+picBean);
                    eventCallBack.onDelete(picBean);
                }
                break;
        }
    }

}
