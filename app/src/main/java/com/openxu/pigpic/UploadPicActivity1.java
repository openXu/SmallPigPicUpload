package com.openxu.pigpic;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.ImageViewEventCallBack;
import com.openxu.pigpic.callback.UploadCallBack;
import com.openxu.pigpic.service.PicUploadService;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.PickPhotoUtil;
import com.openxu.pigpic.util.Url;
import com.openxu.pigpic.view.UpLoadPicLayout;
import com.openxu.pigpic.view.UpLoadPicView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;


/**
 * 此类作废
 */
public class UploadPicActivity1 extends UploadPicBaseActivity implements View.OnClickListener{



    private ImageView iv_back;
    private TextView tv_edit;
    private UpLoadPicLayout pic_layout;
    private LinearLayout ll_choosepic;
    private TextView tv_addpic, tv_choosepic_1, tv_choosepic_2, tv_choosepic_cancel;

    private String tempCameraPath;
    private String house_id;

    private ArrayList<UploadPic> netPicList;

    private int lastKey = -1; // 最新的key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadpic);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_edit = (TextView) findViewById(R.id.tv_edit);

        pic_layout = (UpLoadPicLayout)findViewById(R.id.pic_layout);
        ll_choosepic = (LinearLayout)findViewById(R.id.ll_choosepic);
        tv_addpic = (TextView) findViewById(R.id.tv_addpic);
        tv_choosepic_1 = (TextView) findViewById(R.id.tv_choosepic_1);
        tv_choosepic_2 = (TextView) findViewById(R.id.tv_choosepic_2);
        tv_choosepic_cancel = (TextView) findViewById(R.id.tv_choosepic_cancel);

        iv_back.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        tv_addpic.setOnClickListener(this);
        tv_choosepic_1.setOnClickListener(this);
        tv_choosepic_2.setOnClickListener(this);
        tv_choosepic_cancel.setOnClickListener(this);
        pic_layout.setEventCallBack(new ImageViewEventCallBack() {
            @Override
            public void onDelete(UploadPic pic) {
                //删除
                if(servie!=null){
                    servie.deletePic(pic);
                }
                deletePic(pic);
            }

            @Override
            public void onReUpload(UploadPic pic) {
                //重新上传
            }
        });

        house_id = getIntent().getStringExtra("house_id");

        //绑定服务
        Intent intent = new Intent(this, PicUploadService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        //获取服务器上图片
        getNetPic();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_edit:
                String text = tv_edit.getText().toString().trim();
                if(text.equals("编辑")){
                    tv_edit.setText("完成");
                    pic_layout.setEditStatus(UpLoadPicView.STATUS_EIDT);
                }else{
                    tv_edit.setText("编辑");
                    pic_layout.setEditStatus(UpLoadPicView.STATUS_SHOW);
                }
                break;
            case R.id.tv_addpic:
                if(lastKey<0){
                    Toast.makeText(this, "获取数据失败，请检查网络",Toast.LENGTH_SHORT).show();
                    //获取服务器上图片
                    getNetPic();
                    return;
                }
                ll_choosepic.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_choosepic_1:
                Intent intent = new Intent(this, ChoosePhotoActivity.class);
                //可选择图片的最大数(每次最多添加10张，一个房源总共30最多
                int max_number = 30 - pic_layout.getPicCount();
                intent.putExtra("max_number", max_number>10?10:(max_number<=0?0:max_number));
                startActivityForResult(intent, 1);
                ll_choosepic.setVisibility(View.GONE);
                break;
            case R.id.tv_choosepic_2:
                //调用系统相机
                tempCameraPath = tempPicDir + "/"+ house_id+"_"+System.currentTimeMillis() + ".jpg";
                try {
                    //调用系统相机拍照
                    PickPhotoUtil.getInstance().takePhoto(this, "tempUser", tempCameraPath);
                }catch (Exception e){
                    e.printStackTrace();
                }
                ll_choosepic.setVisibility(View.GONE);
                break;
            case R.id.tv_choosepic_cancel:
                ll_choosepic.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 获取房源id下已经上传成功的图片
     */
    private void getNetPic() {
        Map<String, String> params = new HashMap<>();
        params.put("house_id", house_id);   //房源id
        params.put("dev", "android");
        OkHttpUtils.get()
                .url(Url.URL_PIC_GET)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        LogUtil.e(TAG, "获取服务器图片失败" + e.getMessage());
                    }
                    @Override
                    public void onResponse(String response) {
//                        LogUtil.i(TAG, "获取服务器图片成功：" + response);
                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jOb = new JSONObject(response);
                                int code = jOb.optInt("code");
                                String datas = jOb.optString("datas");
                                jOb = new JSONObject(datas);
                                String house_figure = jOb.optString("data_figure");
                                Type type = new TypeToken<ArrayList<UploadPic>>()
                                {}.getType();
                                netPicList = new Gson().fromJson(house_figure, type);
                                LogUtil.i(TAG, "返回图片："+netPicList);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mergeNetLocalList();
                    }
                });
    }

    /**
     * 将服务器上图片和本地上传列表中的图片合并
     */
    private void mergeNetLocalList(){
        //获取后台服务正在上传的图片队列展示
        ArrayList<UploadPic> mergeList = new ArrayList<>();
        ArrayList<UploadPic> localList = servie.getUploadingList(house_id);
//        LogUtil.i(TAG, "获取"+house_id+"上传列表："+localList);
        if(netPicList == null || netPicList.size()<=0){
            //服务器上没有图片，展示本地列表
            mergeList = localList;
        }else{
            mergeList = netPicList;
            if(localList.size()<=0){
                //服务器上有图片，本地列表没有，展示服务器上图片
            }else{
                //服务器和本地都有图片，合并
                for(UploadPic local : localList){
                    boolean has = false;
                    for(UploadPic net : netPicList){
                        if(local.getKey()==net.getKey()){
                            has = true;
                            break;
                        }
                    }
                    if(!has){
                        mergeList.add(local);
                    }
                }
            }
        }
        //根据key排序
        Collections.sort(mergeList, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                UploadPic s1 = (UploadPic) o1;
                UploadPic s2 = (UploadPic) o2;
                if (s1.getKey() > s2.getKey()){
                    return 1;
                }else if(s1.getKey()==s2.getKey()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        LogUtil.d(TAG, "排序之后的图片："+mergeList);
        //更新最大key值
        if(mergeList!=null && mergeList.size()>0){
            lastKey = mergeList.get(mergeList.size()-1).getKey()+1;
            LogUtil.i(TAG, "最大的key："+lastKey);
        }else{
            lastKey = 0;
        }
        //绑定view
        pic_layout.setViewData(mergeList);
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
                    File fi = new File("");
                    PickPhotoUtil.getInstance().takeResult(this, data, fi);
                    ArrayList<String> addedPath = new ArrayList<>();
                    addedPath.add(tempCameraPath);
                    checkPicList(addedPath);
                    break;
                case 1:
                    addedPath = data.getExtras().getStringArrayList("addedPath");
                    checkPicList(addedPath);
                    break;
            }
        }
    }


    private void checkPicList(ArrayList<String> picPathList){
        //图片压缩
        //new CompressFileTask().execute();
        int notOkNum = 0;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        //检测图片 像素大小不能低于800*600  单个不大于10M
        final ArrayList<String> checkList = new ArrayList<>();
        for(String path : picPathList){
            boolean add = true;
            Bitmap bitmap = BitmapFactory.decodeFile(path, newOpts);
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            if(w<800 || h<600){
                notOkNum += 1;
                add = false;
            }

            try {
                File file = new File(path);
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    int size = fis.available();
                    LogUtil.i(TAG, "文件大小："+size + "  10M="+(10*1024*1024));
                    if(size>10*1024*1024){
                        notOkNum += 1;
                        add = false;
                    }
                } else {
                    notOkNum += 1;
                    add = false;
                    Log.e("获取文件大小", "文件不存在!");
                }
            }catch (Exception e){
                notOkNum += 1;
                add = false;
            }
            if(add){
                checkList.add(path);
            }
        }

        if(notOkNum<=0){
            if(checkList.size()>0){
                add2UploadServiceQue(checkList);
            }
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您选择的图片中有"+notOkNum+"张不符合要求，标准如下：\n"+
                    "1、图片像素不能低于800*600\n" +
                            "2、单个图片不大于10M\n"+
                    "我们将继续为您上传符合要求的图片。")
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(checkList.size()>0){
                                add2UploadServiceQue(checkList);
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
    /**
     * 将图片集合添加到上传队列，并刷新界面
     * @param picPathList
     */
    private void add2UploadServiceQue(ArrayList<String> picPathList){

        ArrayList<UploadPic> piclist = new ArrayList<>();
        for(int i = 0; i<picPathList.size(); i++){
            String path = picPathList.get(i);

            //构建对象
            UploadPic pic = new UploadPic(house_id,
                    UploadPic.STATUS_UPLODING, lastKey,new File(path).getName(), path);
            lastKey += 1;
            piclist.add(pic);
        }

        if(servie!=null){
            //添加到后台上传队列
            servie.addToUpload(house_id, piclist);
            picPathList.clear();
        }else{
            Toast.makeText(this, "service disconnect",Toast.LENGTH_SHORT).show();
        }

        mergeNetLocalList();
    }


    @Override
    protected void onDestroy() {
        if(servie!=null){
            servie.setCallBack(null);
            //解绑服务
            unbindService(serviceConnection);
        }
        pic_layout.setEventCallBack(null);
        super.onDestroy();
    }

    /**
     * 如果图片需要压缩后上传，请用下面的异步任务
     */
    /*图片压缩任务*/
   /* class CompressFileTask extends AsyncTask<Void, Void, ArrayList<String>> {

        public CompressFileTask() {

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... arg0) {
            *//*返回压缩后图片的路径，用于上传*//*
            ArrayList<String> picPaths = BitmapUtils.getSmallPicList(addedPath, tempPicDir);
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
        }

    }*/


    private void deletePic(final UploadPic pic) {
        Map<String, String> params = new HashMap<>();
        params.put("house_id", house_id);   //房源id
        params.put("key", pic.getKey()+"");
        OkHttpUtils.get()
                .url(Url.URL_PIC_DEL)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        LogUtil.e(TAG, "删除图片失败" + e.getMessage());
                        Toast.makeText(mContext, "删除图片失败" + e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onResponse(String response) {
                        LogUtil.i(TAG, "删除图片成功：" + response);
                        if (!TextUtils.isEmpty(response)) {
                            for(UploadPic picItem : netPicList){
                                if(picItem.getKey() == pic.getKey()){
                                    netPicList.remove(picItem);
                                    return;
                                }
                            }

/*
                            try {
                                JSONObject jOb = new JSONObject(response);
                                int code = jOb.optInt("code");
                                String datas = jOb.optString("datas");
                                jOb = new JSONObject(datas);
                                String house_figure = jOb.optString("data_figure");
                                Type type = new TypeToken<ArrayList<UploadPic>>()
                                {}.getType();
                                netPicList = new Gson().fromJson(house_figure, type);
                                LogUtil.i(TAG, "返回图片："+netPicList);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
*/
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        mergeNetLocalList();
                    }
                });
    }




}
