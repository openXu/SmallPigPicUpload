package com.openxu.pigpic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.UploadCallBack;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.PermissionUtils;
import com.openxu.pigpic.util.PickPhotoUtil;
import com.openxu.pigpic.util.Url;
import com.openxu.pigpic.view.UpLoadPicLayout;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

import static android.Manifest.permission.CAMERA;
import static com.openxu.pigpic.UploadPicActivity.CATCH_DIR;

/**
 * activity基类，创建缓存目录、申请权限
 */
public class UploadPicBaseActivity extends Activity{

    protected String TAG ;
    protected Context mContext ;

    /*缓存根目录*/
    protected static final String CATCH_DIR = Environment
            .getExternalStorageDirectory().getPath() + "/CATCH_PIC";
    /*拍照临时目录*/
    protected static final String tempPicDir = CATCH_DIR + "/pic_temp";

    static final String[] PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE,  //读取权限
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        mContext = this;
        //创建wgh根目录
        File wghFile = new File(CATCH_DIR);
        if (!wghFile.exists() && !wghFile.isDirectory()) {
            wghFile.mkdir();
        }
        File file_dir = new File(tempPicDir);
        if (!file_dir.exists() && !file_dir.isDirectory()) {
            file_dir.mkdir();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkCameraPermission();
    }

    /************************权限相关**************************/

    /**
     * 权限检测
     */
    private void checkCameraPermission() {
        if (PermissionUtils.checkPermissionArray(this, PERMISSION, PermissionUtils.PERMISSION_ARRAY)) {
            //如果所需权限全部被允许，执行下面方法
            getAllGrantedPermission();
        } else {
            LogUtil.e(TAG, "系列必要权限有部分未通过");
        }
    }
    /**
     * 当获取到所需权限后，进行相关业务操作
     */
    public void getAllGrantedPermission() {
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSION_ARRAY:
                List<String> list = new ArrayList<>();
                if (PermissionUtils.verifyPermissions(grantResults, permissions, list)) {
                    LogUtil.d(TAG, "权限组被允许");
                    getAllGrantedPermission();
                } else {
                    String pers = PermissionUtils.getUnRrantName(list);
                    LogUtil.e(TAG, pers + "权限被拒绝了");
                    // Permission Denied
                    String msg = "当前应用缺少" + (list.size() > 1 ? (list.size() + "项") : " ") + "必要权限。\n详情如下：\n"
                            + pers +
                            "请点击\"设置\"-\"权限\"-打开所需权限。\n" +
                            "返回后退出应用并重新登陆。";
                    new AlertDialog.Builder(this)
                            .setTitle("权限申请")
                            .setMessage(msg)
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionUtils.showInstalledAppDetails(UploadPicBaseActivity.this, getPackageName());
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //退出
                                    finish();
                                }
                            }).setCancelable(false)
                            .show();
                }
                break;
        }
    }
    /************************权限相关**************************/


}
