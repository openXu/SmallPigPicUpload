package com.openxu.pigpic;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.openxu.pigpic.bean.ImageItem;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.UploadCallBack;
import com.openxu.pigpic.util.BitmapCache;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.PicUtils;
import com.openxu.pigpic.util.PickPhotoUtil;
import com.openxu.pigpic.view.UpLoadPicLayout;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG = "";

    public static final String CATCH_DIR = Environment
            .getExternalStorageDirectory().getPath() + "/CATCH_PIC";
    public static final String tempPicDir = CATCH_DIR + "/pic_temp";

    public ArrayList<String> addedPath;  //选择的照片
    private String tempCameraPath;

    private UpLoadPicLayout pic_layout;
    private LinearLayout ll_choosepic;
    private TextView tv_addpic, tv_choosepic_1, tv_choosepic_2, tv_choosepic_cancel;


    private String house_id = "2448";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pic_layout = (UpLoadPicLayout)findViewById(R.id.pic_layout);
        ll_choosepic = (LinearLayout)findViewById(R.id.ll_choosepic);
        tv_addpic = (TextView) findViewById(R.id.tv_addpic);
        tv_choosepic_1 = (TextView) findViewById(R.id.tv_choosepic_1);
        tv_choosepic_2 = (TextView) findViewById(R.id.tv_choosepic_2);
        tv_choosepic_cancel = (TextView) findViewById(R.id.tv_choosepic_cancel);

        tv_addpic.setOnClickListener(this);
        tv_choosepic_1.setOnClickListener(this);
        tv_choosepic_2.setOnClickListener(this);
        tv_choosepic_cancel.setOnClickListener(this);

        //创建wgh根目录
        File wghFile = new File(CATCH_DIR);
        if (!wghFile.exists() && !wghFile.isDirectory()) {
            wghFile.mkdir();
        }
        File file_dir = new File(tempPicDir);
        if (!file_dir.exists() && !file_dir.isDirectory()) {
            file_dir.mkdir();
        }
        initImageLoader();

        Intent intent = new Intent(this, PicUploadService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
    private PicUploadService.ServiceBinder servie;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            servie = (PicUploadService.ServiceBinder)iBinder;
            servie.setCallBack(new UploadCallBack() {
                @Override
                public void refreshPicPro(UploadPic pic) {
                    pic_layout.refreshChild(pic);
                }
            });

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            servie = null;
        }
    };

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_addpic:
                ll_choosepic.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_choosepic_1:
                Intent intent = new Intent(this, ChoosePhotoActivity.class);
                intent.putExtra("max_number", 10);
                startActivityForResult(intent, 1);
                ll_choosepic.setVisibility(View.GONE);
                break;
            case R.id.tv_choosepic_2:
                //调用系统相机
                tempCameraPath = tempPicDir + "/"+ System.currentTimeMillis() + ".jpg";
                try {
                    //调用系统相机拍照
                    PickPhotoUtil.getInstance().takePhoto(this, "tempUser", tempCameraPath);
                }catch (Exception e){
                    e.printStackTrace();
                    showPermissionDialog();
                }
                ll_choosepic.setVisibility(View.GONE);
                break;
            case R.id.tv_choosepic_cancel:
                ll_choosepic.setVisibility(View.GONE);
                break;
        }
    }
    /**
     * 提示相机权限被拒绝
     */
    private void showPermissionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("无相机使用权限，若希望继续此功能请到设置中开启相机权限。")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();

    }

    // 拍照返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (null == data) {
                return;
            }
            switch (requestCode) {
                case PickPhotoUtil.PickPhotoCode.PICKPHOTO_TAKE:
                    LogUtil.v(TAG, "拍照放回-------------");
                    File fi = new File("");
                    PickPhotoUtil.getInstance().takeResult(this, data, fi);
                    addedPath.add(tempCameraPath);
                    showPic();
                    break;
                case 1:
                    // 判断返回的数据
                    addedPath = data.getExtras().getStringArrayList("addedPath");
                    showPic();
                    break;
            }
        }

    }

    private void showPic(){
        ArrayList<UploadPic> piclist = new ArrayList<>();
        for(String path : addedPath){
            UploadPic pic = new UploadPic(UploadPic.STATUS_UPLODING, new File(path).getName(), path);
            piclist.add(pic);
        }
        try {
            pic_layout.setViewData(piclist);
            if(servie!=null){
                servie.addToUpload(house_id, piclist);
                addedPath.clear();
            }else{
                Toast.makeText(this, "service disconnect",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        new CompressFileTask().execute();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(servie!=null){
            unbindService(serviceConnection);
        }
    }

    class CompressFileTask extends AsyncTask<Void, Void, ArrayList<String>> {

        public CompressFileTask() {

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... arg0) {
            ArrayList<String> picPaths = PicUtils.getSmallPicList(addedPath, tempPicDir);
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
        }

    }




}
