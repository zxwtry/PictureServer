package com.zxwtry.infoCollect.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.zxwtry.pictureServer.util.Constants;

public class HDFSServer {
    
    static int port = 9936;
    static byte[][] hdfsMsg = new byte[2][256];
    static int hdfsMsgIndex = 0;
    
    public static final Object hdfsLock = new Object();
    
    static HDFSReceiveThread receiveThread = null;
    
    static {
    	for (int i = 0; i < hdfsMsg[0].length; i ++) {
    		hdfsMsg[0][i] = Constants.BYTE_BLANK;
    	}
    	receiveThread = new HDFSReceiveThread();
    	receiveThread.start();
    	System.out.println("hdfs thread start....");
    }
    
    public static byte[] getHDFSMsg() {
    	return hdfsMsg[0];
    }
    public static int getHDFSMsgIndex() {
    	return hdfsMsgIndex;
    }
    
    static class HDFSReceiveThread extends Thread {
        
        private volatile boolean stop = false;
        
        @Override
        public void run() {
            DatagramSocket ss = null;
            try {
				ss = new DatagramSocket(port);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
            DatagramPacket packet = new DatagramPacket
            		(hdfsMsg[1], 0, hdfsMsg[1].length);
            while (! stop) {
                try {
                	ss.receive(packet);
                	int size = packet.getLength();
                	System.arraycopy(hdfsMsg[1], 0, hdfsMsg[0], 0, size);
                	hdfsMsgIndex = size;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                packet.setLength(hdfsMsg[1].length);
            }
            ss.close();
        }
    }
    
}
