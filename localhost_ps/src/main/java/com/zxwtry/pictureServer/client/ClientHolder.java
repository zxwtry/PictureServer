package com.zxwtry.pictureServer.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.zxwtry.pictureServer.util.Constants;

public class ClientHolder {
    
    public static final Object lock = new Object();
    
    static int PORT = 9933;
    public static InputStream is;
    public static OutputStream os;
    public static Socket socket;
    
    static final byte[] passwd = new byte[] {
        //自己设定
    };
    
    //定期更新socket连接
    //时间为20分钟
    public static class ClientThread extends Thread {
        static long refreshTime = 20 * 60 * 1000;
        
        private volatile boolean stop = false;
        
        public ClientThread() {
            
        }
        
        public void stopClientThread() {
            this.stop = true;
        }
        
        @Override
        public void run() {
            while (! stop) {
                try {
                    Socket csocket = new Socket(Constants.HOST, PORT);
                    InputStream cis = csocket.getInputStream();
                    OutputStream cos = csocket.getOutputStream();
                    cos.write(passwd);
                    cos.flush();
                    synchronized (lock) {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        is = cis;
                        os = cos;
                        socket = csocket;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(refreshTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void reflushISOSNow() {
        try {
            Socket csocket = new Socket(Constants.HOST, PORT);
            InputStream cis = csocket.getInputStream();
            OutputStream cos = csocket.getOutputStream();
            cos.write(passwd);
            cos.flush();
            synchronized (lock) {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                is = cis;
                os = cos;
                socket = csocket;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
