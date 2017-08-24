package com.zxwtry.pictureServer.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import com.zxwtry.pictureServer.util.MathUtil;

public class ServerThreadCallable implements Callable<byte[][]> {
	
	byte[] lenUid, file;
	
	public ServerThreadCallable(byte[] lenUid, byte[] file) {
		this.lenUid = lenUid;
		this.file = file;
	}
	
	@Override
	public byte[][] call() throws Exception {
		
		InputStream is = null;
		OutputStream os = null;
		
		synchronized (ServerHolder.lock) {
			is = ServerHolder.is;
			os = ServerHolder.os;
		}
		
		if (lenUid != null) os.write(lenUid);
		if (file != null) os.write(file);
		os.flush();
		
		byte[] tmp = new byte[4];
		
		read(tmp, 0, 4, is);
		
		int len = MathUtil.getInt(tmp, 0);
		
		System.out.println("ServerThreadCallable --> call --> len = " + len);
		
		byte[][] ans = new byte[len][];
		
		for (int i = 0; i < len; i ++) {
			read(tmp, 0, 4, is);
			int oneLen = MathUtil.getInt(tmp, 0);
			byte[] one = new byte[oneLen];
			read(one, 0, oneLen, is);
			ans[i] = one;
		}
		
		return ans;
	}
	
	void read(byte[] bs, int i, int j, InputStream is) throws IOException {
		while (i < j) {
			i += is.read(bs, i, j - i);
		}
	}

}
