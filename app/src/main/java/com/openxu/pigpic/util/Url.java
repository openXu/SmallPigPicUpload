package com.openxu.pigpic.util;

/**
 * author : openXu
 * created time : 17/5/7 下午2:24
 * blog : http://blog.csdn.net/xmxkf
 * github : http://blog.csdn.net/xmxkf
 * class name : Url
 * discription :
 */
public class Url{

    private static String URL_ROOT = "http://text.ddlife.club/mobile/index.php?act=houses_publish";
    /**
     * 房源多图上传接口post
     * http://text.ddlife.club/mobile/index.php?act=houses_publish&op=upload_figure
     * house_id 房源id
     * key 图片键值 上传第一图值0 第二张为1 第三张为 2 以此类推
     * image 上传图片标识名
     *
     * 数据返回格式：
     * code：200
     * datas：
     * msg：上传成功
     * image_url：图片链接
     * ------------------------------
     * code：400
     * datas：
     * error：错误提示语句
     */
      public static String URL_PIC_UPLOAD = URL_ROOT+"&op=upload_figure";
    /**
     * 房源多图删除接口post
     * http://text.ddlife.club/mobile/index.php?act=houses_publish&op=del_figure
     * 参数 ：
     * house_id 房源id
     * key 图片键值

     * 数据返回格式：
     * code：200
     * datas：
     * msg：删除成功
     * ------------------------------
     * code：400
     * datas：
     * error：错误提示语句
     */
      public static String URL_PIC_DEL = URL_ROOT+"&op=del_figure";

    /**
     * 房源多图信息接口get
     * http://text.ddlife.club/mobile/index.php?act=houses_publish&op=house_figure
     * house_id 房源id
     *
     * 数据返回格式：
     * code：200
     * datas：house_figure：房源图多片[数组]
     */
      public static String URL_PIC_GET = URL_ROOT+"&op=house_figure";


}
