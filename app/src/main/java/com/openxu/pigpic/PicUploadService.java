package com.openxu.pigpic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.UploadCallBack;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.Url;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;

import okhttp3.Call;
import okhttp3.Request;

/**
 * author : openXu
 * created time : 17/5/7 上午12:45
 * blog : http://blog.csdn.net/xmxkf
 * github : http://blog.csdn.net/xmxkf
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
            ArrayList<UploadPic> list = picMap.get(house_id);
            if(list==null){
                list = new ArrayList();
            }
            list.addAll(picList);
            picMap.put(house_id, list);
            if(!idList.contains(house_id)){
                idList.add(house_id);
            }
            handler.sendEmptyMessage(0);
        }

        public List<UploadPic> getUploadingList(String house_id){
            ArrayList<UploadPic> list = picMap.get(house_id);
            return list;
        }
    }

    private ServiceBinder binder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private boolean isUploading = false;
    private Map<String, ArrayList<UploadPic>> picMap;
    private List<String> idList;
    private int index1 = 0;   //当前上传的房源的索引
    private int index2 = 0;   //当前上传的房源的索引
    private boolean allSucc = true;   //某一个房源的图片是否全部上传成功

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new ServiceBinder();
        picMap = new HashMap<>();
        idList = new ArrayList<>();
        handler.sendEmptyMessage(0);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0 && !isUploading){
                if(idList!=null && idList.size()>0 && index1<idList.size()){
                    String house_id = idList.get(index1);
                    ArrayList<UploadPic> picList = picMap.get(house_id);
                    if(picList!=null && picList.size()>0 && index2<picList.size()){
                        UploadPic pic = picList.get(index2);
                        upload(house_id, index2+"", pic);
                        LogUtil.i(TAG, "开始上传house_id＝"+house_id+"的第"+index2+"张"+pic.getName());
                        index2 ++;
                    }else{
                        if(allSucc){
                            //上一个全部上传成功，清楚
                            idList.remove(index1);
                            picMap.remove(house_id);
                        }else{
                            index1++;
                        }
                        //开始上传下一个
                        index2 = 0;
                        allSucc = true;
                        LogUtil.w(TAG, "开始上传下一个房源图片index1＝"+index1);
                        handler.sendEmptyMessage(0);
                    }
                }else{
                    index1 =0;
//                    handler.sendEmptyMessage(0);
                    LogUtil.w(TAG, "所有房源图片上传完毕");
                }
            }
        }
    };


    private void upload(final String house_id, final String key, final UploadPic pic) {

        isUploading = true;

        Map<String, String> params = new HashMap<>();
        String url = Url.URL_PIC_UPLOAD;
        params.put("house_id", house_id);   //房源id
        params.put("key", key);    //图片键值 上传第一图值0 第二张为1 第三张为 2 以此类推
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
                        pic.setProgress(pro);
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
//                        LogUtil.v(TAG, "房源＝" + house_id + "的第" + key + "张图" + pic.getName() + "上传进度" + pro);
//                        uploadDialog.setUploadingText((int) (progress * 100) + "%");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        allSucc = false;
                        pic.setProgress(0);
                        pic.setStatus(UploadPic.STATUS_FAIL);
                        LogUtil.e(TAG, "上传失败" + e.getMessage());
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtil.i(TAG, "上传成功：" + response);
                        //{"code":200,"datas":{"msg":"\u4e0a\u4f20-\u6210\u529f","image_url":"http:\/\/text.ddlife.club\/data\/house\/20170406\/2448\/2448_70268935482.png"}}
                        pic.setProgress(100);
                        pic.setStatus(UploadPic.STATUS_SUCC);
                        if(callBack!=null)
                            callBack.refreshPicPro(pic);
                       /* if (!TextUtils.isEmpty(response)) {
                            // Json解析
                            String jsonResult = response;
                            if (!TextUtils.isEmpty(jsonResult)) {
                                try {
                                    JSONObject jOb = new JSONObject(jsonResult);
                                    int result = jOb.optInt("result");
                                    String msg = jOb.optString("msg");
                                    ToastAlone.show(msg);
                                    if (result == 1) {
                                        clearPic();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ToastAlone.show("上传失败");
                            }
                        }*/
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        isUploading = false;
                        handler.sendEmptyMessage(0);
                    }
                });
    }

}
