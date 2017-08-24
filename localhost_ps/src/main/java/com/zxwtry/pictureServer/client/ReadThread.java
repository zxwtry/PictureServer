package com.zxwtry.pictureServer.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.zxwtry.pictureServer.dao.PictureInfoDao;
import com.zxwtry.pictureServer.entity.PictureInfo;
import com.zxwtry.pictureServer.util.FileNameUtil;
import com.zxwtry.pictureServer.util.HadoopUtil;
import com.zxwtry.pictureServer.util.JPGConvertor;
import com.zxwtry.pictureServer.util.Constants.PICTURETYPE;

public class ReadThread extends Thread {
    public static void main(String[] args) {
        
    }
    
    private PictureInfoDao pictureInfoDao;
    
    private byte[] intByte = new byte[4];
    
    private byte[] longByte = new byte[8];
    
    private volatile boolean stop = false;
    
    private final Object lock = new Object();
    
    private long readTime = 500;
    
    public void setPictureInfoDao(PictureInfoDao pictureInfoDao) {
        if (this.pictureInfoDao == null) {
            this.pictureInfoDao = pictureInfoDao;
        }
    }
    
    @Override
    public void run() {
        while ( ! stop) {
            try {
                InputStream is = null;
                OutputStream os = null;
                
                synchronized (lock) {
                    is = ClientHolder.is;
                    os = ClientHolder.os;
                }
                
                if (read(intByte, 0, 4, is)) {
                    int len = getInt(intByte);
                    System.out.println(len);
                    if (len > 0) {
                        //存入HDFS
                        if (read(longByte, 0, 8, is)) {
                            long uid = getLong(longByte, 0);
                            byte[] file = new byte[len];
                            if (read(file, 0, len, is)) {
                                String fileName = FileNameUtil.getFileName(file, uid, new Date());
                                PictureInfo pi = new PictureInfo();
                                pi.setFileName(fileName);
                                HadoopUtil.append(PICTURETYPE.large, file, pi);
                                byte[][] nba = JPGConvertor.convertToWap180AndBmiddle(file);
                                HadoopUtil.append(PICTURETYPE.wap180, nba[0], pi);
                                HadoopUtil.append(PICTURETYPE.bmiddle, nba[1], pi);
                                System.out.println("pictureInfoDao == null  -->  " + (pictureInfoDao == null));
                                pictureInfoDao.insert(pi);
                                byte[] tosend = new byte[4 + 4 + fileName.length()];
                                setByte(1, tosend, 0);
                                setByte(fileName.length(), tosend, 4);
                                for (int i = 8; i < tosend.length; i ++) {
                                    tosend[i] = (byte) fileName.charAt(i - 8);
                                }
                                os.write(tosend);
                                os.flush();
                            }
                        }
                    } else if (len < 0) {
                        len = - len;
                        //从HDFS读取
                        byte[] rece = new byte[len];
                        if (read(rece, 0, len, is)) {
                            PICTURETYPE pt = null;
                            if (rece[0] == 0) {
                                pt = PICTURETYPE.wap180;
                            } else if (rece[0] == 1) {
                                pt = PICTURETYPE.bmiddle;
                            } else if (rece[0] == 2) {
                                pt = PICTURETYPE.large;
                            }
                            char[] fnc = new char[len - 1];
                            for (int i = 1; i < len; i ++) {
                                fnc[i - 1] = (char) rece[i];
                            }
                            PictureInfo pi = pictureInfoDao.queryByFileName(new String(fnc));
                            byte[] ans = HadoopUtil.read(pt, pi);
                            byte[] cntAndLen = new byte[8];
                            setByte(1, cntAndLen, 0);
                            setByte(ans.length, cntAndLen, 4);
                            os.write(cntAndLen);
                            os.write(ans);
                            os.flush();
                        }
                    } else {
                        throw new RuntimeException("ReadThread --> run --> len==0");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ClientHolder.reflushISOSNow();
            }
            try {
                Thread.sleep(readTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void stopReadThread() {
        this.stop = true;
    }
    
    boolean read(byte[] bs, int i, int j, InputStream is) throws IOException {
        while (i < j) {
            int size = is.read(bs, i, j - i);
            if (size < 1) return false;
            i += size;
        }
        return true;
    }
    
    int getInt(byte[] bs) {
        int v = 0;
        v = (v << 8) + (bs[3] & 0xff);
        v = (v << 8) + (bs[2] & 0xff);
        v = (v << 8) + (bs[1] & 0xff);
        v = (v << 8) + (bs[0] & 0xff);
        return v;
    }
    
    long getLong(byte[] bs, int i) {
        long v = 0;
        v = (v << 8) + (bs[i + 7] & 0xff);
        v = (v << 8) + (bs[i + 6] & 0xff);
        v = (v << 8) + (bs[i + 5] & 0xff);
        v = (v << 8) + (bs[i + 4] & 0xff);
        v = (v << 8) + (bs[i + 3] & 0xff);
        v = (v << 8) + (bs[i + 2] & 0xff);
        v = (v << 8) + (bs[i + 1] & 0xff);
        v = (v << 8) + (bs[i + 0] & 0xff);
        return v;
    }
    
    static void setByte(int val, byte[] bs, int i) {
        bs[i + 0] = (byte) ((val >>>  0) & 0xff);
        bs[i + 1] = (byte) ((val >>>  8) & 0xff);
        bs[i + 2] = (byte) ((val >>> 16) & 0xff);
        bs[i + 3] = (byte) ((val >>> 24) & 0xff);
    }
    
}
