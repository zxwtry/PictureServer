package com.zxwtry.pictureServer.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.zxwtry.pictureServer.entity.PictureInfo;
import com.zxwtry.pictureServer.util.Constants.PICTURETYPE;

public class HadoopUtil {
    
    public static void main(String[] args) {
        String pn = Constants.CHUNK_PATH_PREFIX + "a";
        Path path = new Path(pn);
        try {
            fs.delete(path, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static Configuration conf = null;
    static URI uri = null;
    static FileSystem fs = null;
    static ByteBuffer chunkSingle = null;
    static {
        System.setProperty(Constants.HADOOP_HOME_KEY, Constants.HADOOP_HOME_VAL);
        conf = new Configuration();
        conf.setBoolean(Constants.HADOOP_APPEND_KEY, Constants.HADOOP_APPEND_VAL);
        conf.set(Constants.HADOOP_APPEND_POLICY_KEY, 
                Constants.HADOOP_APPEND_POLICY_VAL);
        conf.set(Constants.HADOOP_APPEND_ENABLE_KEY, 
                Constants.HADOOP_APPEND_ENABLE_VAL);
        try {
            @SuppressWarnings("resource")
            RandomAccessFile raf = new RandomAccessFile(Constants.CHUNK_INFO_PATH, "rw");
            FileChannel channel = raf.getChannel();
            chunkSingle = channel.map(MapMode.READ_WRITE, 0, Constants.CHUNK_SINGLE_LENGTH);
            uri = new URI(Constants.uriAddress);
            fs = FileSystem.get(uri, conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized boolean append(PICTURETYPE pt, byte[] bs, PictureInfo pi) {
        chunkSingle.position(0);
        int index = chunkSingle.getInt();
        int offset = chunkSingle.getInt();
        if (bs.length > Constants.HADOOP_CHUNK_MAX_SIZE) {
            throw new RuntimeException("input file size exceeded LIMIT 128MB!");
        }
        if (offset + bs.length > Constants.HADOOP_CHUNK_MAX_SIZE) {
            index ++;
            offset = 0;
            chunkSingle.position(0);
            chunkSingle.putInt(index);
        }
        String pn = getChunkPath(index);
        while (true) {
            if (appendToChunk(bs, pn)) {
                break;
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (pt == PICTURETYPE.large) {
            pi.setLargeIndex(index);
            pi.setLargeOffset(offset);
            pi.setLargeLength(bs.length);
        } else if (pt == PICTURETYPE.wap180) {
            pi.setWap180Index(index);
            pi.setWap180Offset(offset);
            pi.setWap180Length(bs.length);
        } else {
            pi.setBmiddleIndex(index);
            pi.setBmiddleOffset(offset);
            pi.setBmiddleLength(bs.length);
        }
        pi.setStatus(Constants.STATUS_NORMAL);
        chunkSingle.position(Constants.CHUNK_SINGLE_OFFSET_POSITION);
        chunkSingle.putInt(offset + bs.length);
        return true;
    }

    public static String getChunkPath(int chunkIndex) {
        int prefixLength = Constants.CHUNK_PATH_PREFIX.length();
        char[] chunkPath = new char[prefixLength + 4];
        for (int i = 0; i < prefixLength; i ++) {
            chunkPath[i] = Constants.CHUNK_PATH_PREFIX.charAt(i);
        }
        boolean needMkdir = true;
        needMkdir &= ((chunkIndex % 62) == 0);
        chunkPath[prefixLength + 3] = FileNameUtil.convertINT2FNC(chunkIndex % 62);
        chunkIndex /= 62;
        needMkdir &= ((chunkIndex % 62) == 0);
        chunkPath[prefixLength + 2] = FileNameUtil.convertINT2FNC(chunkIndex % 62);
        chunkIndex /= 62;
        chunkPath[prefixLength + 0] = FileNameUtil.convertINT2FNC(chunkIndex % 62);
        chunkPath[prefixLength + 1] = '/';
        if (needMkdir) {
            HadoopUtil.mkdir(Constants.CHUNK_PATH_PREFIX + chunkPath[prefixLength + 0]);
        }
        return new String(chunkPath);
    }
    
    static boolean mkdir(String pn) {
        try {
            Path path = new Path(pn);
            if (! fs.exists(path)) {
                fs.mkdirs(path);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static boolean appendToChunk(byte[] bs, String pn) {
        try {
            Path path = new Path(pn);
            FSDataOutputStream os = null;
            if (! fs.exists(path)) {
                os = fs.create(path);
            } else {
                os = fs.append(path);
            }
            os.write(bs);
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static synchronized byte[] read(PICTURETYPE pt, PictureInfo pi) {
        int index = 0;
        int offset = 0;
        int length = 0;
        if (pt == PICTURETYPE.large) {
            index = pi.getLargeIndex();
            offset = pi.getLargeOffset();
            length = pi.getLargeLength();
        } else if (pt == PICTURETYPE.wap180) {
            index = pi.getWap180Index();
            offset = pi.getWap180Offset();
            length = pi.getWap180Length();
        } else {
            index = pi.getBmiddleIndex();
            offset = pi.getBmiddleOffset();
            length = pi.getBmiddleLength();
        }
        byte[] ans = new byte[length];
        String pn = getChunkPath(index);
        try {
            FSDataInputStream fis = fs.open(new Path(pn));
            fis.seek(offset);
            int ai = 0;
            while (ai < length) {
                ai += fis.read(ans, ai, length - ai);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }
    
}
