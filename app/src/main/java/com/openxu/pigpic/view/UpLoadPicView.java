package com.openxu.pigpic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.pigpic.R;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;

import java.util.ArrayList;

/**
 * author : openXu
 * created time : 17/5/7 下午3:51
 * class name : UpLoadPicView
 * discription :
 */
public class UpLoadPicView extends ImageView {

    private String TAG = "UpLoadPicView";

    private UploadPic picBean;

    private Paint paint, tPahint;

    private Bitmap failBit;

    private int proHight = 3;
    private int textSize = 10;  //sp

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
        setScaleType(ScaleType.CENTER_CROP);

        failBit = BitmapFactory.decodeResource(getResources(), R.drawable.choosepic_icon_def);

        proHight = DensityUtil.dip2px(getContext(), proHight);
        textSize = DensityUtil.dip2px(getContext(), textSize);

        paint = new Paint();
        paint.setAntiAlias(true);
        tPahint = new Paint();
        tPahint.setAntiAlias(true);
        tPahint.setColor(Color.WHITE);
        tPahint.setTextSize(textSize);
    }

    public void setViewData(UploadPic pic){
        picBean = pic;
        setTag(picBean.getName());
        ImageLoader.getInstance().displayImage("file://" + picBean.getPath(), this);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec. getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
//        LogUtil.i(TAG, "图片空间宽高："+getMeasuredWidth()+"*"+getMeasuredHeight());
        if(picBean!=null)
            ImageLoader.getInstance().displayImage("file://" + picBean.getPath(), this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int status = picBean.getStatus();
        switch (status){
            case UploadPic.STATUS_UPLODING:
                paint.setColor(Color.BLACK);
                paint.setAlpha(150);
                canvas.drawRect(0,0,getWidth(),getHeight(),paint);

                float proAll = getWidth()-80;
                RectF r1=new RectF();
                r1.left=40;                          //左边
                r1.top=(getHeight()-proHight)/2;     //上边
                r1.right=proAll+40;              //右边
                r1.bottom=(getHeight()+proHight)/2;  //下边
                paint.setColor(Color.WHITE);
                paint.setAlpha(255);
                canvas.drawRoundRect(r1, 10, 10, paint);        //绘制圆角矩形
//                LogUtil.i(TAG, "绘制矩形"+r1);
                RectF r2 = new RectF();
                r2.left = 40;                          //左边
                r2.top=(getHeight()-proHight)/2;     //上边
                r2.right= 40+((proAll/100.0f)*picBean.getProgress()); //右边
                r2.bottom=(getHeight()+proHight)/2;  //下边
                paint.setColor(Color.parseColor("#FF3E96"));
                paint.setAlpha(255);
                canvas.drawRoundRect(r2, 10, 10, paint);        //绘制圆角矩形
                LogUtil.i(TAG, "绘制jindu "+r2);
                String text = picBean.getProgress()+"%";
                canvas.drawText(text, (getMeasuredWidth()-DensityUtil.getFontlength(tPahint, text))/2,
                        r2.bottom+DensityUtil.getFontLeading(tPahint)+12 ,tPahint);
                break;
            case UploadPic.STATUS_SUCC:
                break;
            case UploadPic.STATUS_FAIL:
                paint.setColor(Color.BLACK);
                paint.setAlpha(200);
                canvas.drawRect(0,0,getWidth(),getHeight(),paint);
                canvas.drawBitmap(failBit, (getWidth()-failBit.getWidth()/2),
                        (getHeight()-failBit.getHeight())/2, paint);
                break;
        }
    }
}
