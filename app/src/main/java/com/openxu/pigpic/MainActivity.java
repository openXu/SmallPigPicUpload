package com.openxu.pigpic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * author : openXu
 * created time : 17/5/7 下午11:02
 * blog : http://blog.csdn.net/xmxkf
 * github : http://blog.csdn.net/xmxkf
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

    }


    public void onClick(View v){
        String house_id = "";
        switch (v.getId()){
            case R.id.tv_1:
                house_id = "2448";
                break;
            case R.id.tv_2:
                house_id = "2553";
                break;
            case R.id.tv_3:
                house_id = "2554";
                break;
        }
        Intent intent = new Intent(this, UploadPicActivity.class);
        intent.putExtra("house_id", house_id);
        startActivity(intent);
    }
}
