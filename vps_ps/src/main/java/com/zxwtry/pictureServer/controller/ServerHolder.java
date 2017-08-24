package com.zxwtry.pictureServer.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerHolder {
	static int PORT = 9933;
	public static InputStream is;
	public static OutputStream os;
	public static Socket socket;
	
	static final byte[] passwd = new byte[] {
	    //自己设定
	};
	public static final Object lock = new Object();
	
	public static class ServerThread extends Thread {
		
		ServerSocket ss = null;
		private volatile boolean stop = false;
		
		public ServerThread() {
			try {
				ss = new ServerSocket(PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void stopServerThread() {
			this.stop = true;
		}
		
		@Override
		public void run() {
			
			while (! stop) {
				try {
					Socket nsocket = ss.accept(); 
					byte[] bs = new byte[passwd.length];
					nsocket.getInputStream().read(bs);
					if (Arrays.equals(bs, passwd)) {
						synchronized (lock) {
							if (socket != null) {
								try {
									socket.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							is = nsocket.getInputStream();
							os = nsocket.getOutputStream();
							socket = nsocket;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
//				break;	//zxw debug
			}
			
		}
	}
	
}
