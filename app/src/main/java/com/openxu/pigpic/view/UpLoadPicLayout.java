package com.openxu.pigpic.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.openxu.pigpic.R;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * author : openXu
 * created time : 17/5/7 下午3:51
 * class name : UpLoadPicLayout
 * discription :
 */
public class UpLoadPicLayout extends ViewGroup {


    private String TAG = "UpLoadPicLayout";
    private int firstItemW;
    private int firstItemH = 150;
    private int itemWH;
    private int space = 10;
    private boolean hasFirst = false;
    private int lieNum = 3;   //列数
    private int hangNum;

    public UpLoadPicLayout(Context context) {
        this(context, null);
    }
    public UpLoadPicLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public UpLoadPicLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        picList = new ArrayList<>();
        firstItemH = DensityUtil.dip2px(getContext(), firstItemH);
        space = DensityUtil.dip2px(getContext(), space);
    }

    public void refreshChild(UploadPic pic){
        int count = getChildCount();
        if(count>0){
            for(int i =0; i<count; i++){
                UpLoadPicView picView = (UpLoadPicView)getChildAt(i);
                if(pic.getName().equals(picView.getTag())){
                    picView.setViewData(pic);
                    LogUtil.i(TAG, "刷新"+pic.getName()+"的进度");
                }
            }
        }
    }

    private ArrayList<UploadPic> picList;
    public void setViewData(ArrayList<UploadPic> list){
        picList.clear();
        if(list!=null){
            picList.addAll(list);
        }
        hasFirst = false;
        hangNum = 0;
        if(picList.size()>0){
            hasFirst = true;
            int size = picList.size()-1;
            if(size==0){
                hangNum = 0;
            }else{
                hangNum = size/lieNum;
                int yushu = size%lieNum;
                if(yushu>0){
                    hangNum+=1;
                }
            }
        }

        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int layoutWidth = MeasureSpec. getSize(widthMeasureSpec);
        int layoutHeight = 0;
        if(!hasFirst){
            setMeasuredDimension(0, 0);
            return;
        }
        firstItemW = layoutWidth - space*2;
        layoutHeight = firstItemH+space*2;
        itemWH = (layoutWidth - space*(lieNum+1))/lieNum;
        if(hangNum>0){
            layoutHeight+=((itemWH+space)*hangNum);
        }
//        LogUtil.i(TAG, "容器中总共"+picList.size()+"个,行数："+hangNum);
//        LogUtil.w(TAG, "第一宽高："+firstItemW+" * "+firstItemH+" 容器宽高："+layoutWidth+" * "+layoutHeight);
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if(picList.size()<=0){
            return;
        }
        int left = space;
        int top = 0;
        UploadPic firstPic = picList.get(0);
        UpLoadPicView view = new UpLoadPicView(getContext());
        view.setViewData(firstPic);
        LogUtil.w(TAG, "设置第一个图片宽高："+firstItemW+"*"+firstItemH);
        view.measure(firstItemW, firstItemH);
        // 确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
        addView(view);
        view.layout(space, space, space+firstItemW, space+firstItemH);
//        LogUtil.w(TAG, "第一个图片位置："+space+"*"+space+"*"+(space+firstItemW)+"*"+(space+firstItemH));
        top = firstItemH+space*2;

        int lie = 0;
        for(int j = 1; j<picList.size(); j++){
            UploadPic pic = picList.get(j);
            view = new UpLoadPicView(getContext());
            view.setViewData(pic);
//            LogUtil.w(TAG, "设置图片宽高："+itemWH+"*"+itemWH);
            view.measure(itemWH, itemWH);
            if(lie<lieNum){
                left = lie*(itemWH+space)+space;
            }else{
                //换行
                lie=0;
                left = space;
                top += (itemWH+space);
            }
            addView(view);
            view.layout(left, top, left+itemWH, top+itemWH);
//            LogUtil.w(TAG, "图片位置："+left+"*"+top+"*"+(itemWH+left)+"*"+(itemWH+top));
            lie++;
        }
    }

}
