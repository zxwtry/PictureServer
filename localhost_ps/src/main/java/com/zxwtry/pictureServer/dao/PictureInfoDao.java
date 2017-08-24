package com.zxwtry.pictureServer.dao;

import org.apache.ibatis.annotations.Param;

import com.zxwtry.pictureServer.entity.PictureInfo;

public interface PictureInfoDao {
    void insert(PictureInfo pictureInfo);
    PictureInfo queryByFileName(@Param("fileName")String fileName);
}
