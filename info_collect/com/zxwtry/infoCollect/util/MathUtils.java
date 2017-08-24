package com.zxwtry.infoCollect.util;

public class MathUtils {
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
	
	public static int getInt(byte[] bs, int bi) {
		int v = 0;
		v = (v << 8) + (bs[bi + 3] & 0xff);
		v = (v << 8) + (bs[bi + 2] & 0xff);
		v = (v << 8) + (bs[bi + 1] & 0xff);
		v = (v << 8) + (bs[bi + 0] & 0xff);
		return v;
	}
	
}
