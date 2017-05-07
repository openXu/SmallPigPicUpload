package com.openxu.pigpic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : openXu
 * created time : 17/5/7 下午12:18
 * class name : UploadPic
 * discription :
 */
public class UploadPic implements Parcelable {
    public UploadPic(int status, String name, String path) {
        this.status = status;
        this.name = name;
        this.path = path;
    }
    public static final int STATUS_UPLODING = 1;
    public static final int STATUS_FAIL = 2;
    public static final int STATUS_SUCC = 3;
    private int status;
    private int progress;
    private String name;
    private String path;

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.progress);
        dest.writeString(this.name);
        dest.writeString(this.path);
    }

    protected UploadPic(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.name = in.readString();
        this.path = in.readString();
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
