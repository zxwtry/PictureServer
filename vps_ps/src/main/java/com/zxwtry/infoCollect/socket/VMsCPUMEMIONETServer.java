package com.zxwtry.infoCollect.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zxwtry.pictureServer.util.Constants;
import com.zxwtry.pictureServer.util.MathUtil;

public class VMsCPUMEMIONETServer {

	static int port = 9188;
	static byte[] intBytes = new byte[4];
	static byte[] saveBytes = null;
	static int saveBytesIndex = 0;
	static String[] records = null;
	static byte[] receBuff = new byte[2048];
	static char[] charBuff = new char[receBuff.length];
	static ConcurrentHashMap<String, String[]> map = new ConcurrentHashMap<>();

	static int HOSTNAME_INDEX = 0;
	static int HOSTNAME_LENGTH = 8;
	static int CPU_1MIN_INDEX = 1;
	static int CPU_1MIN_LENGTH = 6;
	static int CPU_5MIN_INDEX = 2;
	static int CPU_5MIN_LENGTH = 6;
	static int CPU_10MIN_INDEX = 3;
	static int CPU_10MIN_LENGTH = 6;
	static int MEM_TOTAL_INDEX = 4;
	static int MEM_TOTAL_LENGTH = 6;
	static int MEM_FREE_INDEX = 6;
	static int MEM_FREE_LENGTH = 6;
	static int IO_READ_INDEX = 12;
	static int IO_READ_LENGTH = 11;
	static int IO_WRITE_INDEX = 13;
	static int IO_WRITE_LENGTH = 11;
	static int NET_RECEIVED_INDEX = 16;
	static int NET_RECEIVED_LENGTH = 12;
	static int NET_SENT_INDEX = 17;
	static int NET_SENT_LENGTH = 12;
	static int TIME_INDEX = 15;
	static int TIME_LENGTH = 22;
	
	static int[] indexArray = new int[] {
		HOSTNAME_INDEX, CPU_1MIN_INDEX, CPU_5MIN_INDEX,
		CPU_10MIN_INDEX, MEM_TOTAL_INDEX, MEM_FREE_INDEX, 
		IO_READ_INDEX, IO_WRITE_INDEX, NET_RECEIVED_INDEX,
		NET_SENT_INDEX, TIME_INDEX
	};
	
	static int[] lengthArray = new int[] {
		HOSTNAME_LENGTH, CPU_1MIN_LENGTH, CPU_5MIN_LENGTH,
		CPU_10MIN_LENGTH, MEM_TOTAL_LENGTH, MEM_FREE_LENGTH, 
		IO_READ_LENGTH, IO_WRITE_LENGTH, NET_RECEIVED_LENGTH,
		NET_SENT_LENGTH, TIME_LENGTH
	};
	
	static String[] nameArray = new String[] {
		"host", "cpu1", "cpu5",
		"cpu10", "memto", "memfr",
		"ior", "iow", "netr",
		"nets", "time"
	};

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static VMServerThread serverThread = null;
	
	static {
		serverThread = new VMServerThread();
		serverThread.start();
	}
	
	public static byte[] getMsg() {
		Set<String> keySet = map == null ? null : map.keySet();
		int keySize = keySet == null ? 0 : keySet.size();
		int msgSize = (keySize + 2) * 600;
		byte[] msg = new byte[msgSize + 4];
		int msgIndex = 4;
		for (int i = 0; i < indexArray.length; i ++) {
			String v = nameArray[i];
			int vn = v == null ? 0 : v.length();
			int blankNumber = lengthArray[i] - vn;
			if (blankNumber < 0) {
				blankNumber = 0;
			}
			for (int vi = 0; vi < vn; vi ++) {
				msg[msgIndex ++] = (byte) v.charAt(vi);
			}
			for (int j = 0; j < blankNumber; j ++) {
				msgIndex = addBlank(msg, msgIndex);
			}
		}
		msgIndex = addNewLine(msg, msgIndex);
		for (String key : keySet) {
			String[] val = map.get(key);
			if (val == null) {
				continue;
			}
			for (int i = 0; i < indexArray.length; i ++) {
				String v = val[indexArray[i]];
				int vn = v == null ? 0 : v.length();
				int blankNumber = lengthArray[i] - vn;
				if (blankNumber < 0) {
					blankNumber = 0;
				}
				for (int vi = 0; vi < vn; vi ++) {
					msg[msgIndex ++] = (byte) v.charAt(vi);
				}
				for (int j = 0; j < blankNumber; j ++) {
					msgIndex = addBlank(msg, msgIndex);
				}
			}
			msgIndex = addNewLine(msg, msgIndex);
		}
		MathUtil.setBytes(msg, 0, msgIndex);
		return msg;
	}
	
	static int addBlank(byte[] bs, int bi) {
		bs[bi ++] = Constants.BYTE_AND;
		bs[bi ++] = Constants.BYTE_e;
		bs[bi ++] = Constants.BYTE_n;
		bs[bi ++] = Constants.BYTE_s;
		bs[bi ++] = Constants.BYTE_p;
		bs[bi ++] = Constants.BYTE_SEMICOLON;
		return bi;
	}

	static int addNewLine(byte[] bs, int bi) {
		bs[bi ++] = Constants.BYTE_LESS_THAN;
		bs[bi ++] = Constants.BYTE_b;
		bs[bi ++] = Constants.BYTE_r;
		bs[bi ++] = Constants.BYTE_SLASH;
		bs[bi ++] = Constants.BYTE_GREATER_THAN;
		return bi;
	}
	
	static class VMServerThread extends Thread {

		private volatile boolean stop = false;

		@Override
		public void run() {
			DatagramSocket ss = null;;
			try {
				ss = new DatagramSocket(port);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			DatagramPacket packet = new DatagramPacket
					(receBuff, 0, receBuff.length);
			while (!stop) {
				try {
					ss.receive(packet);
					int size = packet.getLength();
					for (int i = 0; i < size; i ++) {
						charBuff[i] = (char) receBuff[i];
					}
					String[] splits = new String(charBuff, 0, size).split(" ");
					splits[TIME_INDEX] = sdf.format(new Date());
					map.put(splits[0], splits);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				packet.setLength(receBuff.length);
			}
			ss.close();
		}
	}

}
