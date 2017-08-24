package com.zxwtry.pictureServer.client;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HDFSClient {
    
    //小测试开始
    public static void main(String[] args) {
        HDFSSendThread thread = new HDFSSendThread();
        thread.start();
    }
    //小测试结束
    
    static int port = 9936;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static InetAddress address = null;
    static {
        try {
//            address = InetAddress.getByName(Constants.HOST);
            address = InetAddress.getByName("115.159.160.199");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    public static class HDFSSendThread extends Thread {
        
        private volatile boolean stop = false;
        
        @Override
        public void run() {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e1) {
                e1.printStackTrace();
            }
            DatagramPacket packet = null;
            while (! stop) {
                try {
                    getHDFSMsg();
                    if (packet == null) {
                        packet = new DatagramPacket
                                (msg, 0, msgIndex, address, port);
                    } else {
                        packet.setData(msg, 0, msgIndex);
                        packet.setAddress(address);
                        packet.setPort(port);
                    }
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static String url = "http://node101:50070/jmx?qry=Hadoop:service=NameNode,name=NameNodeInfo";
    
    static final byte BYTE_COMMA = ',';
    static final byte BYTE_BLANK = ' ';
    static final byte BYTE_BRACE_LEFT = '{';
    static final byte BYTE_BRACE_RIGHT = '}';
    static final byte BYTE_DOUBLE_QUOTATION = '\"';
    
    static byte[] buff = new byte[6 * 1024];
    static byte[] msg = new byte[300];
    static int msgIndex = 0;
    static int bufferIndex = 0;
    static byte[] used = null;
    static byte[] free = null;
    static byte[] liveNodes = null;
    static byte[] deadNodes = null;
    static byte[] saveMode = null;
    
    static {
        String usedString = "\"Used\" : ";
        int len = usedString.length();
        used = new byte[len];
        for (int i = 0; i < len; i ++) {
            used[i] = (byte)usedString.charAt(i);
        }
        String freeString = "\"Free\" : ";
        len = freeString.length();
        free = new byte[len];
        for (int i = 0; i < len; i ++) {
            free[i] = (byte) freeString.charAt(i);
        }
        String liveNodesString = "\"LiveNodes\" : ";
        len = liveNodesString.length();
        liveNodes = new byte[len];
        for (int i = 0; i < len; i ++) {
            liveNodes[i] = (byte) liveNodesString.charAt(i);
        }
        String deadNodesString = "\"DeadNodes\" : ";
        len = deadNodesString.length();
        deadNodes = new byte[len];
        for (int i = 0; i < len; i ++) {
            deadNodes[i] = (byte) deadNodesString.charAt(i);
        }
        String saveModeString = "\"Safemode\" : ";
        len = saveModeString.length();
        saveMode = new byte[len];
        for (int i = 0; i < len; i ++) {
            saveMode[i] = (byte) saveModeString.charAt(i);
        }
    }
    
    static void getHDFSMsg() {
        bufferIndex = 0;
        msgIndex = 0;
        try {
            URLConnection conn = new URL(url).openConnection();
            InputStream is = conn.getInputStream();            
            while (true) {
                int size = is.read(buff, bufferIndex, buff.length - bufferIndex);
                if (size == -1) {
                    break;
                }
                bufferIndex += size;
            }
            msg[msgIndex ++] = 'u';
            msg[msgIndex ++] = 's';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = 'd';
            msg[msgIndex ++] = ':';
            addBlank();
            getUsed();
            addBlank();
            addBlank();
            addBlank();
            msg[msgIndex ++] = 'f';
            msg[msgIndex ++] = 'r';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = ':';
            addBlank();
            getFree();
            addBlank();
            addBlank();
            addBlank();
            msg[msgIndex ++] = 'l';
            msg[msgIndex ++] = 'i';
            msg[msgIndex ++] = 'v';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = ':';
            addBlank();
            getLiveNodes();
            addBlank();
            addBlank();
            addBlank();
            msg[msgIndex ++] = 'd';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = 'a';
            msg[msgIndex ++] = 'd';
            msg[msgIndex ++] = ':';
            addBlank();
            getDeadNodes();
            addBlank();
            addBlank();
            addBlank();
            msg[msgIndex ++] = 's';
            msg[msgIndex ++] = 'a';
            msg[msgIndex ++] = 'f';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = 'm';
            msg[msgIndex ++] = 'o';
            msg[msgIndex ++] = 'd';
            msg[msgIndex ++] = 'e';
            msg[msgIndex ++] = ':';
            addBlank();
            getSafeMode();
            addBlank();
            addBlank();
            addBlank();
            getTime();
//            writeInt(msg, 0, msgIndex - 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void getTime() {
        String f = sdf.format(new Date());
        for (int i = 0, len = f.length(); i < len; i ++) {
            msg[msgIndex ++] = (byte) f.charAt(i);
        }
    }

    static void addBlank() {
        msg[msgIndex ++] = '&';
        msg[msgIndex ++] = 'e';
        msg[msgIndex ++] = 'n';
        msg[msgIndex ++] = 's';
        msg[msgIndex ++] = 'p';
        msg[msgIndex ++] = ';';
    }
    
    static void getUsed() {
        byte[] bs = used;
        for (int i = 0; i < bufferIndex - bs.length; i ++) {
            boolean isSame = true;
            for (int j = 0; j < used.length; j ++) {
                if (bs[j] != buff[i + j]) {
                    isSame = false;
                    break;
                }
            }
            if (isSame) {
                for (int k = i + bs.length; k < bufferIndex; k ++) {
                    byte b = buff[k];
                    if (b == BYTE_COMMA) {
                        break;
                    }
                    msg[msgIndex ++] = b;
                }
                break;
            }
        }
    }
    
    static void getFree() {
        byte[] bs = free;
        for (int i = 0; i < bufferIndex - bs.length; i ++) {
            boolean isSame = true;
            for (int j = 0; j < used.length; j ++) {
                if (bs[j] != buff[i + j]) {
                    isSame = false;
                    break;
                }
            }
            if (isSame) {
                for (int k = i + bs.length; k < bufferIndex; k ++) {
                    byte b = buff[k];
                    if (b == BYTE_COMMA) {
                        break;
                    }
                    msg[msgIndex ++] = b;
                }
                break;
            }
        }
    }
    
    static void getLiveNodes() {
        byte[] bs = liveNodes;
        for (int i = 0; i < bufferIndex - bs.length; i ++) {
            boolean isSame = true;
            for (int j = 0; j < used.length; j ++) {
                if (bs[j] != buff[i + j]) {
                    isSame = false;
                    break;
                }
            }
            if (isSame) {
                int sign = 0;
                int cnt = 0;
                for (int k = i + bs.length; k < bufferIndex; k ++) {
                    byte b = buff[k];
                    if (b == BYTE_BRACE_LEFT) {
                        sign ++;
                    } else if (b == BYTE_BRACE_RIGHT) {
                        sign --;
                        if (sign == 1) {
                            cnt ++;
                        }
                        if (sign == 0) {
                            break;
                        }
                    }
                }
                String v = String.valueOf(cnt);
                for (int k = 0, len = v.length(); k < len; k ++) {
                    msg[msgIndex ++] = (byte) v.charAt(k);
                }
                break;
            }
        }
    }
    
    static void getDeadNodes() {
        byte[] bs = deadNodes;
        for (int i = 0; i < bufferIndex - bs.length; i ++) {
            boolean isSame = true;
            for (int j = 0; j < used.length; j ++) {
                if (bs[j] != buff[i + j]) {
                    isSame = false;
                    break;
                }
            }
            if (isSame) {
                int sign = 0;
                int cnt = 0;
                for (int k = i + bs.length; k < bufferIndex; k ++) {
                    byte b = buff[k];
                    if (b == BYTE_BRACE_LEFT) {
                        sign ++;
                    } else if (b == BYTE_BRACE_RIGHT) {
                        sign --;
                        if (sign == 1) {
                            cnt ++;
                        }
                        if (sign == 0) {
                            break;
                        }
                    }
                }
                String v = String.valueOf(cnt);
                for (int k = 0, len = v.length(); k < len; k ++) {
                    msg[msgIndex ++] = (byte) v.charAt(k);
                }
                break;
            }
        }
    }
    
    static void getSafeMode() {
        byte[] bs = saveMode;
        for (int i = 0; i < bufferIndex - bs.length; i ++) {
            boolean isSame = true;
            for (int j = 0; j < used.length; j ++) {
                if (bs[j] != buff[i + j]) {
                    isSame = false;
                    break;
                }
            }
            if (isSame) {
                int k = i + bs.length;
                String v = "yes";
                if (buff[k] == BYTE_DOUBLE_QUOTATION && 
                        buff[k + 1] == BYTE_DOUBLE_QUOTATION) {
                    v = "no";
                }
                for (int vi = 0, vn = v.length(); vi < vn; vi ++) {
                    msg[msgIndex ++] = (byte) v.charAt(vi);
                }
                break;
            }
        }
    }
    
}
