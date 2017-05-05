package com.openxu.pigpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.openxu.pigpic.util.LogUtil;
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

public class MainActivity extends AppCompatActivity {

    private String TAG = "";

    public static final String PICDIR = Environment
            .getExternalStorageDirectory().getPath() + "/pigpic";
    public static final String tempPicDir = PICDIR + "/pic_temp";

    //创建一个存放选中的图片路径的集合
    public ArrayList<String> addedPath;  //选择的照片
    public ArrayList<String> takePics;    //拍摄的照片
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化选中的图片的集合
        addedPath=new ArrayList<>();
        takePics=new ArrayList<>();
    }

    public void btnSelectPic(View v){
        //创建wgh根目录
        File wghFile = new File(PICDIR);
        if (!wghFile.exists() && !wghFile.isDirectory()) {
            wghFile.mkdir();
        }
        File file_dir = new File(tempPicDir);
        if (!file_dir.exists() && !file_dir.isDirectory()) {
            file_dir.mkdirs();
        }
        Intent intent = new Intent(this, ChoosePhotoActivity.class);
        intent.putExtra("max_number", 10);
        intent.putExtra("temp_dir", tempPicDir);
        intent.putStringArrayListExtra("addedPath",addedPath);
        intent.putStringArrayListExtra("takePics",takePics);
        LogUtil.e(TAG, "之前已经选择图片个数："+(addedPath.size()+takePics.size()));
        startActivityForResult(intent, 1);
    }

    // 拍照返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String picDir = "";
            List<String> list = null;
            if (null == data) {
                return;
            }
            switch (requestCode) {
                case 1:
                    // 判断返回的数据
                    addedPath = data.getExtras().getStringArrayList("addedPath");
                    takePics = data.getExtras().getStringArrayList("takePics");
                    if(addedPath!=null&&addedPath.size()==1){
                    }else if(takePics!=null&&takePics.size()==1){
                    }
                    break;
            }
        }

    }







    public void submit() {
        CompressFileTask task = new CompressFileTask(uploadDialog);
        task.execute();
    }

    //    private File x1;
    //压缩图片后的集合
    private ArrayList<File> compressPics;

    class CompressFileTask extends AsyncTask<Void, Void, Void> {
        UpLoadindDialog updialog;

        public CompressFileTask(UpLoadindDialog dialog) {
            updialog = dialog;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //创建wgh根目录
            File wghFile = new File(Constant.WGH_DIR);
            if (!wghFile.exists() && !wghFile.isDirectory()) {
                wghFile.mkdir();
            }
            // 营业执照
            File file_dir1 = new File(tempPicDir);
            if (!file_dir1.exists() && !file_dir1.isDirectory()) {
                file_dir1.mkdir();
            }

            if(addedPath.size()>0){
                for (int i = 0; i < addedPath.size(); i++) {
                    compressPics.add(getimage(addedPath.get(i), i));
                }
            }else if(takePics.size()>0){
                for (int i = 0; i < takePics.size(); i++) {
                    compressPics.add(getimage(takePics.get(i), i));
                }
            }
            LogUtil.d("图片:营业执照:" + addedPath.size()+"  "+takePics.size());

//            int file_size1 = FileUtils.getlist(file_dir1);
//            if (file_size1 > 0) {
//                ArrayList<String> picList = FileUtils.getFileList(file_dir1);
////                if (null != picList && picList.size() > 0) {
////                    int length = picList.size();
////                    x1 = getimage(picList.get(0), 1);
////                }
//                for (int i = 0; i < picList.size(); i++) {
//                    compressPics.add(getimage(picList.get(i), i));
//                }
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            uploadBitFile(updialog);
        }

    }

    public File getimage(String srcPath, int name_pg) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;
        float ww = 480f;
        // 缩放比，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = compressImage(BitmapFactory.decodeFile(srcPath, newOpts));
        File bitFile = saveBitMaptoSdcard(bitmap);
        try {
            long size = getFileSize(bitFile);
            LogUtil.i("cxm", "i=" + name_pg + ",size=" + size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitFile;// 压缩好比例大小后再进行质量压缩
    }



    /**
     * 获取指定文件大小
     *
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
        }
        return size;
    }

    public Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500) { // 循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    private File saveBitMaptoSdcard(Bitmap bitmap) {
        // 得到外部存储卡的路径
        // ff.png是将要存储的图片的名称
        File dir_file = new File(Constant.LEGALD_AILY_SD);
        if (!dir_file.exists() && !dir_file.isDirectory()) {
            // 文件夹不存在，则创建文件夹
            dir_file.mkdir();
        }
        File file = new File(tempPicDir, "/"
                + System.currentTimeMillis() + ".png");
        LogUtil.v(TAG, "svaeBitmapSdacrd===" + file.getAbsolutePath());
        // 从资源文件中选择一张图片作为将要写入的源文件
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void uploadBitFile(final UpLoadindDialog uploadDialog) {
        // 先循环压缩图片
        uploadDialog.show();
        String qyxz = qyxzMap.get(data1.get("qyxzStr"));
        Map<String, String> params = new HashMap<>();
        String url = "";
        if (isDef) {
            params.put("userId", spUtil.getUserId() + "");
            params.put("wgArea", wghmidSpUtil.getUserArea());
            params.put("wgId", wghmidSpUtil.getUserWghId() + "");
            params.put("jobId", wghmidSpUtil.getUserJobId() + "");
            url = UrlUtil.getUpdateIBDataUrl() + "\n" + "&userId=" + spUtil.getUserId() + "&wgArea=" + wghmidSpUtil.getUserArea() +
                    "&wgId=" + wghmidSpUtil.getUserWghId() + "&jobId=" + wghmidSpUtil.getUserJobId() + "\n" +
                    "&name=" + data1.get("name") + "&qyjc=" + data1.get("qyjc") + "&qyxz=" + qyxz
                    + "&dz=" + data1.get("dz") + "&fddbr=" + data1.get("fddbr") + "&fddbr_sjh=" + data1.get("fddbr_sjh") +
                    "&yymj=" + data1.get("yymj") + "&qyrs=" + data1.get("qyrs");
        } else {
            params.put("userId", spUtil.getUserId() + "");
            params.put("wgArea", dianpu.getWgh_area() + "");
            params.put("wgId", dianpu.getWgh_id() + "");
            params.put("jobId", dianpu.getJob_id() + "");
            url = UrlUtil.getUpdateIBDataUrl() + "\n" + "&userId=" + spUtil.getUserId() +
                    "&wgId=" + dianpu.getWgh_id() + "&wgArea=" + dianpu.getWgh_area() + "&jobId=" + dianpu.getJob_id() + "\n" +
                    "&name=" + data1.get("name") + "&qyjc=" + data1.get("qyjc") + "&qyxz=" + qyxz
                    + "&dz=" + data1.get("dz") + "&fddbr=" + data1.get("fddbr") + "&fddbr_sjh=" + data1.get("fddbr_sjh") +
                    "&yymj=" + data1.get("yymj") + "&qyrs=" + data1.get("qyrs");
        }

        params.put("name", data1.get("name"));    // name	企业名称
        params.put("qyjc", data1.get("qyjc"));    //企业简称
//        params.put("qyxz", data1.get("qyxz") + "");    // qyxz	企业性质

        params.put("qyxz", qyxz + "");    // qyxz	企业性质
//		params.addBodyParameter("gsh", data1.get("gsh")+"");      // gsh	营业执照号
        params.put("dz", data1.get("dz"));        // dz	地址
        params.put("fddbr", data1.get("fddbr"));  // fddbr	法定代表人
        params.put("fddbr_sjh", data1.get("fddbr_sjh") + "");   // fddbr_sjh	手机号码
//		params.addBodyParameter("xfaqglr", data1.get("xfaqglr"));           // xfaqglr	消防安全管理人
//		params.addBodyParameter("xfaqglr_sjh", data1.get("xfaqglr_sjh")+"");      // xfaqglr_sjh	手机号码
        params.put("yymj", data1.get("yymj") + "");// yymj	营业面积
        params.put("qyrs", data1.get("qyrs") + "");// qyrs	企业人数
        LogUtil.i("上传数据采集：" + url);
        PostFormBuilder formBuilder = OkHttpUtils.post()
                .url(UrlUtil.getUpdateIBDataUrl())
                .params(params);
        if (compressPics.size()>0) {
            // 营业执照 yyzz_pic
//            LogUtil.i(x1.getName());
            for(int i = 0; i < compressPics.size(); i++) {
                formBuilder.addFile("yyzz_pic", compressPics.get(i).getName(), compressPics.get(i));
            }

        }
        formBuilder.build()
                .execute(new StringCallback() {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                        if (!QyxxActivity.this.isFinishing()
                                && !uploadDialog.isShowing())
                            uploadDialog.show();
                    }

                    @Override
                    public void inProgress(float progress) {
                        super.inProgress(progress);
                        LogUtil.v(progress + "");
                        uploadDialog.setUploadingText((int) (progress * 100) + "%");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        LogUtil.e(TAG, "上传失败" + e.getMessage());
//                        x1 = null;
                        compressPics.clear();
                        ToastAlone.show("上传失败");
                        if (!QyxxActivity.this.isFinishing() && uploadDialog.isShowing())
                            uploadDialog.dismiss();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (!TextUtils.isEmpty(response)) {
                            if (!isDef)
                                hasChanged = true;
                            if (!QyxxActivity.this.isFinishing() && uploadDialog.isShowing())
                                uploadDialog.dismiss();
                            LogUtil.i(TAG, "上传成功:" + response);

                            QYXZ = Integer.parseInt(qyxz);

                            compressPics.clear();

                            setBaseInfoStatus(false);
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
                        }
                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                    }
                });
    }


}
