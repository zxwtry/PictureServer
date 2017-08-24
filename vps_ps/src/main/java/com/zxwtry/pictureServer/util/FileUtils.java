package com.zxwtry.pictureServer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileUtils {
    
    public static final int READ_FILE_ERROR = -3332;
    public static final int READ_FILE_BS_EXPAND = -3333;
    
    public static int readSpecialFile(String path, byte[] bs) {
        FileInputStream fis = null;
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fis == null) return READ_FILE_ERROR;
        return readInputStream(fis, bs);
    }
    
    public static int readInputStream(InputStream is, byte[] bs) {
        try {
            int bi = 0;
            while (true) {
                int size = is.read(bs, bi, bs.length - bi);
                if (size == -1) break;
                bi += size;
                if (bi == bs.length) {
                    return READ_FILE_BS_EXPAND;
                }
            }
            return bi;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                       }
        }
        return READ_FILE_ERROR;
    }
    
    public static int readInputStreamFaultTolerant(InputStream is, byte[] bs, int bn) {
        try {
            int bi = 0;
            while (true) {
                int size = is.read(bs, bi, bn - bi);
                if (size == -1) break;
                bi += size;
                if (bi == bn) {
                    return bn;
                }
            }
            return READ_FILE_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return READ_FILE_ERROR;
    }
    
}
