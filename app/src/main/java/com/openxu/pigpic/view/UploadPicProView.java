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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.pigpic.R;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.ImageViewEventCallBack;
import com.openxu.pigpic.util.DensityUtil;

/**
 * author : openXu
 * created time : 17/5/7 下午3:51
 * class name : UploadPicProView
 * discription :
 */
public class UploadPicProView extends View {

    private String TAG = "UploadPicProView";

    private int progress;

    private Paint paint, tPahint;

    private int proHight = 3;
    private int textSize = 10;  //sp

    public UploadPicProView(Context context) {
        this(context, null);
    }
    public UploadPicProView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public UploadPicProView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        proHight = DensityUtil.dip2px(getContext(), proHight);
        textSize = DensityUtil.dip2px(getContext(), textSize);

        paint = new Paint();
        paint.setAntiAlias(true);
        tPahint = new Paint();
        tPahint.setAntiAlias(true);
        tPahint.setColor(Color.WHITE);
        tPahint.setTextSize(textSize);

    }

    public void setProcess(int pro){

        progress = pro;

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec. getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
        r2.right= 40+((proAll/100.0f)*progress); //右边
        r2.bottom=(getHeight()+proHight)/2;  //下边
        paint.setColor(Color.parseColor("#FF3E96"));
        paint.setAlpha(255);
        canvas.drawRoundRect(r2, 10, 10, paint);        //绘制圆角矩形
//                LogUtil.i(TAG, "绘制jindu "+r2);
        String text = progress+"%";
        canvas.drawText(text, (getMeasuredWidth()-DensityUtil.getFontlength(tPahint, text))/2,
                r2.bottom+DensityUtil.getFontLeading(tPahint)+12 ,tPahint);
    }
}
