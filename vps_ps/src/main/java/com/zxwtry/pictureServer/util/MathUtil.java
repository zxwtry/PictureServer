package com.zxwtry.pictureServer.util;


public class MathUtil {

    public static int humanKindNumber(byte[] bs, int bi, int v) {
        int rangeIndex = 0;
        for (; rangeIndex < Constants.BYTE_ARRAY_UNIT_RANGE.length; rangeIndex ++) {
            if (v < Constants.BYTE_ARRAY_UNIT_RANGE[rangeIndex]) {
                double cut = rangeIndex == 0 ? 1 : 
                    Constants.BYTE_ARRAY_UNIT_RANGE[rangeIndex - 1];
                String s = rangeIndex == 0 ? String.valueOf(v) : 
                    String.format("%.2f", v / cut);
                for (int i = 0, len = s.length(); i < len; i ++) {
                    bs[bi ++] = (byte) s.charAt(i);
                }
                if (rangeIndex != 0) {
                    bs[bi ++] = Constants.BYTE_ARRAY_UNIT[rangeIndex - 1];
                }
                break;
            }
        }
        return bi;
    }
    
    public static void writeInt(byte[] bs, int bi, int v) {
        bs[bi + 0] = (byte) ((v >>>  0) & 0xff);
        bs[bi + 1] = (byte) ((v >>>  8) & 0xff);
        bs[bi + 2] = (byte) ((v >>> 16) & 0xff);
        bs[bi + 3] = (byte) ((v >>> 24) & 0xff);
    }
    
    public static long getLong(byte[] bs, int i) {
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
    
    public static int getInt(byte[] bs, int i) {
        int v = 0;
        v = (v << 8) + (bs[i + 3] & 0xff);
        v = (v << 8) + (bs[i + 2] & 0xff);
        v = (v << 8) + (bs[i + 1] & 0xff);
        v = (v << 8) + (bs[i + 0] & 0xff);
        return v;
    }
    
    public static byte[] getByte(int len, long uid) {
        byte[] bs = new byte[12];
        bs[0] = (byte) ((len >>>  0) & 0xff);
        bs[1] = (byte) ((len >>>  8) & 0xff);
        bs[2] = (byte) ((len >>> 16) & 0xff);
        bs[3] = (byte) ((len >>> 24) & 0xff);
        bs[4] = (byte) ((uid >>>  0) & 0xff);
        bs[5] = (byte) ((uid >>>  8) & 0xff);
        bs[6] = (byte) ((uid >>> 16) & 0xff);
        bs[7] = (byte) ((uid >>> 24) & 0xff);
        bs[8] = (byte) ((uid >>> 32) & 0xff);
        bs[9] = (byte) ((uid >>> 40) & 0xff);
        bs[10] = (byte) ((uid >>> 48) & 0xff);
        bs[11] = (byte) ((uid >>> 56) & 0xff);
        return bs;
    }

    public static void setBytes(byte[] bs, int bi, int v) {
        bs[bi + 0] = (byte) ((v >>>  0) & 0xff);
        bs[bi + 1] = (byte) ((v >>>  8) & 0xff);
        bs[bi + 2] = (byte) ((v >>> 16) & 0xff);
        bs[bi + 3] = (byte) ((v >>> 24) & 0xff);
    }
    
}
