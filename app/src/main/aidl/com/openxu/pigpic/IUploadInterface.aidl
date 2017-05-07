package com.openxu.pigpic;
import com.openxu.pigpic.bean.UploadPic;
import java.util.List;

interface IUploadInterface {



    void addToUpload(in String house_id, in List<UploadPic> picList);

    List<UploadPic> getUploadingList(in String house_id);
}
