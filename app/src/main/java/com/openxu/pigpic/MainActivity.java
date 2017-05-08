package com.openxu.pigpic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.openxu.pigpic.service.PicUploadService;

/**
 * author : openXu
 * created time : 17/5/7 下午11:02
 * class name : MainActivity
 * discription :
 */
public class MainActivity extends Activity implements View.OnClickListener{


    private TextView tv_1, tv_2, tv_3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_1 = (TextView)findViewById(R.id.tv_1);
        tv_2 = (TextView)findViewById(R.id.tv_2);
        tv_3 = (TextView)findViewById(R.id.tv_3);
        tv_1.setOnClickListener(this);
        tv_2.setOnClickListener(this);
        tv_3.setOnClickListener(this);

        startService(new Intent(this, PicUploadService.class));

        //初始化ImageLoader
        initImageLoader();
    }

    private void initImageLoader(){
        // imageLoader配置
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(imageOptions)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }


    public void onClick(View v){
        String house_id = "";
        switch (v.getId()){
            case R.id.tv_1:
                house_id = "2698";
                break;
            case R.id.tv_2:
                house_id = "2697";
                break;
            case R.id.tv_3:
                house_id = "2696";
                break;
        }
        Intent intent = new Intent(this, UploadPicActivity.class);
        intent.putExtra("house_id", house_id);
        startActivity(intent);
    }
}
