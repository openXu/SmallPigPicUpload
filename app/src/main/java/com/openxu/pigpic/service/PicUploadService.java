package com.openxu.pigpic.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.UploadCallBack;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.Url;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * author : openXu
 * created time : 17/5/7 上午12:45
 * class name : PicUploadService
 * discription :
 */
public class PicUploadService extends Service {

    private String TAG = "PicUploadService";

    private UploadCallBack callBack;

    public class ServiceBinder extends Binder {
        public void setCallBack(UploadCallBack callBack) {
            PicUploadService.this.callBack = callBack;
        }

        public void addToUpload(String house_id, List<UploadPic> picList){
            if(picList!=null){
                picQue.addAll(picList);
            }
            handler.sendEmptyMessage(0);
        }

        /**
         * 获取正在队列中等待上传的图片对象集合（包含上传失败的）
         * @param house_id
         * @return
         */
        public ArrayList<UploadPic> getUploadingList(String house_id){
            ArrayList<UploadPic> list = new ArrayList<>();
            for(UploadPic picItem : picQue){
                if(picItem.getHouse_id().equals(house_id)){
                    list.add(picItem);
                }
            }
            return list;
        }

        /**
         * 删除队列中的某个图片
         * @param pic
         */
        public void deletePic(UploadPic pic){
            for(int i = 0 ;i <picQue.size(); i++){
                UploadPic picItem = picQue.get(i);
                if(picItem.getHouse_id().equals(pic.getHouse_id())
                        && picItem.getKey() == pic.getKey()){
                    if(i>index){

                    }else{
                        index -=1;
                    }
                    picQue.remove(i);
                    break;
                }
            }
        }
        /**
         * 重新上传，将指针index指向该图片
         * @param pic
         */
        public void onReUpload(UploadPic pic){
            for(int i = 0 ;i <picQue.size(); i++){
                UploadPic picItem = picQue.get(i);
                if(picItem.getHouse_id().equals(pic.getHouse_id())
                        && picItem.getKey() == pic.getKey()){
                    if(picItem.getStatus() == UploadPic.STATUS_FAIL){
                        //只需要将reLoadIndex指向该图片即可
                        reLoadIndex = i;
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    break;
                }
            }
        }
    }

    private ServiceBinder binder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private boolean isUploading = false;
    private ArrayList<UploadPic> picQue;
    private int reLoadIndex = -1;   //重新上传的指针
    private int index = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new ServiceBinder();
        picQue = new ArrayList<>();
        handler.sendEmptyMessage(0);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0 && !isUploading){
                if(reLoadIndex>=0){
                    index = reLoadIndex;
                    reLoadIndex = -1;
                }
                if(picQue.size()>0 &&  index<picQue.size()){
                    UploadPic pic = picQue.get(index);
                    //如果上传成功的，上传下一个
                    if(pic.getStatus()==UploadPic.STATUS_SUCC){
                        picQue.remove(index);
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    uploadPic(pic);
                    LogUtil.w(TAG, "开始上传house_id＝"+pic.getHouse_id()+"的"+pic.getPath());
                }else{
                    LogUtil.e(TAG, "一轮上传完毕了");
                    index = 0;
                }
            }
        }
    };


    private void uploadPic(final UploadPic pic) {

        isUploading = true;

        Map<String, String> params = new HashMap<>();
        String url = Url.URL_PIC_UPLOAD;
        params.put("house_id", pic.getHouse_id());   //房源id
        params.put("key", pic.getKey()+"");    //图片键值 上传第一图值0 第二张为1 第三张为 2 以此类推
//        params.put("image", pic.getName());    // 上传图片标识名
        OkHttpUtils.post()
                .url(url)
                .params(params)
                .addFile("image", pic.getName(), new File(pic.getPath()))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request) {
                        pic.setProgress(0);
                        pic.setStatus(UploadPic.STATUS_UPLODING);
                        super.onBefore(request);
                    }

                    @Override
                    public void inProgress(float progress) {
                        super.inProgress(progress);
                        int pro = (int)(progress*100);
                        if(pro>=100){
                            pro = 98;
                        }
                        pic.setProgress(pro);
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
//                        LogUtil.v(TAG, "房源＝" + house_id + "的第" + key + "张图" + pic.getName() + "上传进度" + pro);
//                        uploadDialog.setUploadingText((int) (progress * 100) + "%");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        pic.setProgress(0);
                        pic.setStatus(UploadPic.STATUS_FAIL);
                        LogUtil.e(TAG, "上传失败" + e.getMessage());
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
                    }

                    @Override
                    public void onResponse(String response) {
                        //{"code":200,"datas":{"msg":"\u4e0a\u4f20-\u6210\u529f","image_url":"http:\/\/text.ddlife.club\/data\/house\/20170406\/2448\/2448_70268935482.png"}}
                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jOb = new JSONObject(response);
                                int code = jOb.optInt("code");
                                if (code == 200) {
                                    LogUtil.i(TAG, "上传成功：" + response);
                                    pic.setProgress(100);
                                    pic.setStatus(UploadPic.STATUS_SUCC);
                                    if(callBack!=null)
                                        callBack.refreshPicPro(pic);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        pic.setProgress(0);
                        pic.setStatus(UploadPic.STATUS_FAIL);
                        LogUtil.e(TAG, "上传失败" +response);
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isUploading = false;
                        if(pic.getStatus()==UploadPic.STATUS_SUCC){
                            //上传成功只需要将成功的移除，索引自然就对应下一个了
                            picQue.remove(index);
                        }else{
                            //上传失败后上传下一个
                            index++;
                        }
                        handler.sendEmptyMessage(0);
                    }
                });
    }

}
