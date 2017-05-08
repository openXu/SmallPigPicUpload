package com.openxu.pigpic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * author : openXu
 * created time : 17/5/7 下午12:18
 * class name : UploadPic
 * discription :
 */
public class UploadPic implements Parcelable {
    public UploadPic(String house_id, int status, int key, String name, String path) {
        this.house_id = house_id;
        this.status = status;
        this.key = key;
        this.name = name;
        this.path = path;
    }
    public static final int STATUS_UPLODING = 1;
    public static final int STATUS_FAIL = 2;
    public static final int STATUS_SUCC = 3;

    private int status;    //图片上传状态

    private String house_id;
    private int progress;  //上传进度
    private String name;
    private String path;
    /*
     * key作为tag标识，用于上传是找到对应的控件刷新进度，
     * 在UpLoadPicView.setViewData(UploadPic pic) &
     * UpLoadPicLayout.refreshChild(UploadPic pic)方法中使用
     */
    private int key;
    //服务器图片地址
    private String url;

    public String getHouse_id() {
        return house_id;
    }

    public void setHouse_id(String house_id) {
        this.house_id = house_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public UploadPic() {
    }


    @Override
    public String toString() {
        return "UploadPic{" +
                "house_id=" + house_id +
                ", key='" + key + '\'' +
                ", status=" + status +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeString(this.house_id);
        dest.writeInt(this.progress);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeInt(this.key);
        dest.writeString(this.url);
    }

    protected UploadPic(Parcel in) {
        this.status = in.readInt();
        this.house_id = in.readString();
        this.progress = in.readInt();
        this.name = in.readString();
        this.path = in.readString();
        this.key = in.readInt();
        this.url = in.readString();
    }

    public static final Creator<UploadPic> CREATOR = new Creator<UploadPic>() {
        @Override
        public UploadPic createFromParcel(Parcel source) {
            return new UploadPic(source);
        }

        @Override
        public UploadPic[] newArray(int size) {
            return new UploadPic[size];
        }
    };
}
