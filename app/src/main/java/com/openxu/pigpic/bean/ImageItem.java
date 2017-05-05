package com.openxu.pigpic.bean;

import java.io.Serializable;

public class ImageItem implements Serializable {
	public String imageId;
	public String thumbnailPath;    //缩略图路径
	public String imagePath;        //原图路径
	public boolean isSelected = false;
}
