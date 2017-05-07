package com.openxu.pigpic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.photoview.PhotoView;
import com.openxu.pigpic.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author : openXu
 * created time : 17/5/6 下午9:01
 * class name : PicScanActivity
 * discription :
 */
public class PicScanActivity extends Activity implements View.OnClickListener{


    private String  TAG = "PicScanActivity";
    private ViewPager viewpager;
    private ImageView iv_back;
    private TextView tv_unselect;
    private MyViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanpic);

        this.position = getIntent().getIntExtra("position", 0);
        this.imgs = getIntent().getStringArrayListExtra("imgs");

        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_unselect = (TextView) findViewById(R.id.tv_unselect);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        viewpager.setOffscreenPageLimit(2);

        adapter = new MyViewPagerAdapter(this);
        viewpager.setAdapter(adapter);
        adapter.setData(imgs);
        viewpager.setCurrentItem(position);

        iv_back.setOnClickListener(this);
        tv_unselect.setOnClickListener(this);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int count = viewpager.getChildCount();
                int curIndex = viewpager.getCurrentItem();
                LogUtil.i(TAG, "childCount="+count+"   curIndex="+curIndex);
                for(int i = 0; i<count; i++){
                    if(curIndex!=i) {
                        LinearLayout child = (LinearLayout) viewpager.getChildAt(i);
                        PhotoView photoView = (PhotoView) child.findViewById(R.id.img_plan);
                        photoView.setScale(1);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private ArrayList<String> imgs;

    private int position;


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_unselect:
                int curIndex = viewpager.getCurrentItem();
                if(imgs.size()>0){
                    imgs.remove(curIndex);
                    if(imgs.size()<=0&&curIndex==0){
                        onBackPressed();
                        return;
                    }
                    if(curIndex==0){
                        position = 0;
                    }else if(curIndex==imgs.size()-1){
                        position = imgs.size()-1;
                    }else{
                    }

                    adapter.setData(imgs);
                    viewpager.setAdapter(adapter);
                    viewpager.setCurrentItem(position);
                }

                break;
        }

    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putStringArrayListExtra("imgs", imgs);
        setResult(RESULT_OK, data);
        finish();
    }

    private class MyViewPagerAdapter extends PagerAdapter {

        ArrayList<String> imgList;

        Context mContext;

        public MyViewPagerAdapter(Context context) {
            imgList = new ArrayList<>();
            this.mContext = context;
        }
        public void setData(List<String> list) {
            imgList.clear();
            if(list!=null){
                imgList.addAll(list);
            }
            LogUtil.i(TAG, "设置数据："+imgList);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() { // 获得size
            return imgList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            String imgUrl = imgList.get(position);
            LinearLayout view = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.activity_scanpic_browse, null);
            PhotoView img = (PhotoView) view.findViewById(R.id.img_plan);
            img.setTag(imgUrl);
            ImageLoader.getInstance().displayImage("file://" + imgList.get(position), img);

            container.addView(view);

            return view;

        }


    }
}
