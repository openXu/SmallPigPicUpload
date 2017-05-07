package com.openxu.pigpic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * author : openXu
 * created time : 17/5/6 下午1:12
 * blog : http://blog.csdn.net/xmxkf
 * github : http://blog.csdn.net/xmxkf
 * class name : sddsds
 * discription :
 */
public class PicUtils {


    private static String TAG = "PicUtils";
    private static String tempDirPath;
    //压缩图片后的集合
    private static ArrayList<String> compressPics = new ArrayList<>();

    public static ArrayList<String>  getSmallPicList(List<String> paths, String tempDir){
        tempDirPath = tempDir;
        compressPics.clear();
        if(paths.size()>0){
            for (int i = 0; i < paths.size(); i++) {
                compressPics.add(getimage(paths.get(i), i));
            }
        }
        return compressPics;
    }

    public static String getimage(String srcPath, int name_pg) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

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
        String bitFilePath = saveBitMaptoSdcard(bitmap);
        bitmap.recycle();
        try {
            long size = getFileSize(bitFilePath);
            LogUtil.i(TAG, "压缩后图片大小i=" + name_pg + ",size=" + size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitFilePath;
    }


    /**
     * 获取指定文件大小
     * @return
     * @throws Exception
     */
    private static long getFileSize(String path) throws Exception {
        File file = new File(path);
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

    public static Bitmap compressImage(Bitmap image) {

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

    private static String saveBitMaptoSdcard(Bitmap bitmap) {
        File dir_file = new File(tempDirPath);
        if (!dir_file.exists() && !dir_file.isDirectory()) {
            dir_file.mkdirs();
        }
        String path = tempDirPath+ "/"+ System.currentTimeMillis() + ".png";
        File file = new File(path);
        LogUtil.v(TAG, "保存压缩图片文件:" + file.getAbsolutePath());
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


}
