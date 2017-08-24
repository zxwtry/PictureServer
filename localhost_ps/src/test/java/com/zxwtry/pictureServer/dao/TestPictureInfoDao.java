package com.zxwtry.pictureServer.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.zxwtry.pictureServer.BaseTest;
import com.zxwtry.pictureServer.entity.PictureInfo;

public class TestPictureInfoDao extends BaseTest {

    @Autowired
    private PictureInfoDao pictureInfoDao;
    
    @Test
    public void testQueryByFileName() {
        String fn = "dOAtuiLSwiEqiKqhSsGlc9aaaaaabelAVLns";
        PictureInfo pi = pictureInfoDao.queryByFileName(fn);
        System.out.println(pi);
    }
    
    
}
