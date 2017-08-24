package com.zxwtry.infoCollect.socket;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.zxwtry.infoCollect.util.FileUtils;


/**
 * @author      zxwtry
 * @email       zxwtry@qq.com
 * @project     TrafficMonitor
 * @package     com.zxwtry.trafficMonitor.monitor
 * @file        TraffixRXTXMonitor.java
 * @date        Aug 7, 2017 10:48:57 AM
 * @details     output receive data  B/s
 * @details     output send    data  B/s
 * @details     在构造方法中，开启一个线程去获得更新数据
 * @details     有一个public方法，queryRXTXSpeedOfInterface返回需要的rx tx
 */
public class TraffixRXTXMonitor {
    
    //小测试开始
    public static void main(String[] args) throws Exception {
        test();
    }
    
    static void test() throws Exception {
        TraffixRXTXMonitor monitor = new TraffixRXTXMonitor(1500);
        for (int i = 0; i < 10; i ++) {
            Thread.sleep(1000);
            int[] m = monitor.queryRXTXSpeedOfInterface("eth0");
            if (m == null) {
                System.out.println("null");
            } else {
                System.out.printf("receive: %-9d   transmit: %-9d\n", m[0], m[1]);
            }
        }
        monitor.stop();
    }
    //小测试结束
    
    private static final byte BYTE_COLON = (byte) ':';
    private static final byte BYTE_BLANK = (byte) ' ';
    private static final byte BYTE_NEW_LINE = (byte) '\n';
    private static final String DEV_PATH = "/proc/net/dev";
    
    private final Object lock = new Object();
    
    private ArrayList<NetDev> preNetDev = new ArrayList<>(3);
    private ArrayList<NetDev> nowNetDev = new ArrayList<>(3);
    private ArrayList<NetDev> tmpNetDev = new ArrayList<>(3);
    private byte[] bs = new byte[2 * 1024];
    
    private volatile boolean stop = false;
    private final int readIntervalTimeMills;
    
    public TraffixRXTXMonitor() {
        this(600);
    }
    
    public TraffixRXTXMonitor(int readIntervalTimeMills) {
        this.readIntervalTimeMills = readIntervalTimeMills;
        startReadThread();
    }
    
    private void startReadThread() {
        new Thread(){
            public void run() {
                while (! stop) {
                    try {
                        read();
                        Thread.sleep(readIntervalTimeMills);
                    } catch (InterruptedException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }
    
    public void stop() {
        this.stop = true;
    }
    
    private void read() throws UnsupportedEncodingException {
        int bsSize = 0;
        long readTimeMillis = 0;
        while (true) {
            bsSize = FileUtils.readSpecialFile(DEV_PATH, bs);
            readTimeMillis = System.currentTimeMillis();
            if (bsSize == FileUtils.READ_FILE_ERROR) {
                throw new RuntimeException(DEV_PATH + " read error!");
            } else if (bsSize == FileUtils.READ_FILE_BS_EXPAND) {
                System.out.println(DEV_PATH + " bs need to expand to " + (bs.length * 2 + 1));
                bs = new byte[bs.length * 2 + 1];
            } else {
                break;
            }
        }
        int pi = -1;
        int tmpNetNevIndex = 0;
        for (int i = 0; i < bsSize; i ++) {
            byte b = bs[i];
            if (b == BYTE_BLANK || b == BYTE_NEW_LINE) {
                pi = i;
            } else if (b == BYTE_COLON) {
                String interfaceName = new String(bs, pi + 1, i - pi - 1, "UTF-8");
                NetDev netDev = null;
                if (tmpNetNevIndex >= tmpNetDev.size()) {
                    netDev = new NetDev(interfaceName);
                    tmpNetDev.add(netDev);
                } else {
                    netDev = tmpNetDev.get(tmpNetNevIndex);
                    netDev.interfaceName = interfaceName;
                }
                netDev.timeMillis = readTimeMillis;
                tmpNetNevIndex ++;
                i = parseNetDev(netDev, bs, i + 1);
                pi = i - 1;
            }
        }
        while (tmpNetDev.size() != tmpNetNevIndex) {
            tmpNetDev.remove(tmpNetNevIndex);
        }
        synchronized (lock) {
            ArrayList<NetDev> t1 = preNetDev;
            preNetDev = nowNetDev;
            nowNetDev = tmpNetDev;
            tmpNetDev = t1;
        }
    }
    
    public int[] queryRXTXSpeedOfInterface(String interfaceName) {
        long preReceiveBytes = -1, nowReceiveBytes = -1;
        long preTransmitBytes = -1, nowTransmitBytes = -1;
        long time = 0, nowTime = 0, preTime = 0;
        synchronized (lock) {
            for (int i = 0, len = preNetDev.size(); i < len; i ++) {
                NetDev netDev = preNetDev.get(i);
                if (netDev.interfaceName.equals(interfaceName)) {
                    preReceiveBytes = netDev.receiveBytes;
                    preTransmitBytes = netDev.transmitBytes;
                    preTime = netDev.timeMillis;
                    break;
                }
            }
            if (preReceiveBytes == -1) {
                return null;
            }
            for (int i = 0, len = nowNetDev.size(); i < len; i ++) {
                NetDev netDev = nowNetDev.get(i);
                if (netDev.interfaceName.equals(interfaceName)) {
                    nowReceiveBytes = netDev.receiveBytes;
                    nowTransmitBytes = netDev.transmitBytes;
                    nowTime = netDev.timeMillis;
                    break;
                }
            }
            time = nowTime - preTime;
        }
        if (nowReceiveBytes == -1 || time == 0) {
            return null;
        }
        return new int[] { (int) ((nowReceiveBytes - preReceiveBytes) * 1000 / time), 
                           (int) ((nowTransmitBytes - preTransmitBytes) * 1000 / time) };
    }
    
    public List<TraffixRXTXMonitor.NetDev> getRecentNetDev() {
        List<NetDev> list = new ArrayList<>(nowNetDev.size());
        synchronized (lock) {
            for (int i = 0, len = nowNetDev.size(); i < len; i ++) {
                list.add(nowNetDev.get(i).clone());
            }
        }
        return list;
    }
    
    private int parseNetDev(NetDev netDev, byte[] bs, int i) {
        long v = -1;
        int sign = 0;
        byte b = 0;
        for (; sign < 16; i ++) {
            b = bs[i];
            if (b == BYTE_BLANK || b == BYTE_NEW_LINE) {
                if (v != -1) {
                    setNetDev(netDev, sign ++, v);
                }
                v = -1;
            } else {
                if (v == -1) v = 0;
                v = v * 10 + b - '0';
            }
        }
        return i;
    }
    
    private void setNetDev(NetDev netDev, int sign, long v) {
        switch (sign) {
        case 0:
            netDev.receiveBytes = v;
            break;
        case 1:
            netDev.receivePackets = v;
            break;
        case 2:
            netDev.receiveErrs = v;
            break;
        case 3:
            netDev.receiveDrop = v;
            break;
        case 4:
            netDev.receiveFifo = v;
            break;
        case 5:
            netDev.receiveFrame = v;
            break;
        case 6:
            netDev.receiveCompressed = v;
            break;
        case 7:
            netDev.receiveMulticast = v;
            break;
        case 8:
            netDev.transmitBytes = v;
            break;
        case 9:
            netDev.transmitPackets = v;
            break;
        case 10:
            netDev.transmitErrs = v;
            break;
        case 11:
            netDev.transmitDrop = v;
            break;
        case 12:
            netDev.transmitFifo = v;
            break;
        case 13:
            netDev.transmitColls = v;
            break;
        case 14:
            netDev.transmitCarrier = v;
            break;
        case 15:
            netDev.transmitCompressed = v;
            break;
        default:
            break;
        }
    }

    public static class NetDev {
        public String interfaceName;
        public long receiveBytes;
        public long receivePackets;
        public long receiveErrs;
        public long receiveDrop;
        public long receiveFifo;
        public long receiveFrame;
        public long receiveCompressed;
        public long receiveMulticast;
        public long transmitBytes;
        public long transmitPackets;
        public long transmitErrs;
        public long transmitDrop;
        public long transmitFifo;
        public long transmitColls;
        public long transmitCarrier;
        public long transmitCompressed;
        public long timeMillis;
        public NetDev(String interfaceName) {
            this.interfaceName = interfaceName;
        }
        public NetDev clone() {
            NetDev clone = new NetDev(this.interfaceName);
            clone.receiveBytes = this.receiveBytes;
            clone.receivePackets = this.receivePackets;
            clone.receiveErrs = this.receiveErrs;
            clone.receiveDrop = this.receiveDrop;
            clone.receiveFifo = this.receiveFifo;
            clone.receiveFrame = this.receiveFrame;
            clone.receiveCompressed = this.receiveCompressed;
            clone.receiveMulticast = this.receiveMulticast;
            clone.transmitBytes = this.transmitBytes;
            clone.transmitPackets = this.transmitPackets;
            clone.transmitErrs = this.transmitErrs;
            clone.transmitDrop = this.transmitDrop;
            clone.transmitFifo = this.transmitFifo;
            clone.transmitColls = this.transmitColls;
            clone.transmitCarrier = this.transmitCarrier;
            clone.transmitCompressed = this.transmitCompressed;
            clone.timeMillis = this.timeMillis;
            return clone;
        }
        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append("interfaceName: ");
            st.append(interfaceName);
            st.append("\n");
            st.append("receiveBytes: ");
            st.append(receiveBytes);
            st.append("\n");
            st.append("receivePackets: ");
            st.append(receivePackets);
            st.append("\n");
            st.append("receiveErrs: ");
            st.append(receiveErrs);
            st.append("\n");
            st.append("receiveDrop: ");
            st.append(receiveDrop);
            st.append("\n");
            st.append("receiveFifo: ");
            st.append(receiveFifo);
            st.append("\n");
            st.append("receiveFrame: ");
            st.append(receiveFrame);
            st.append("\n");
            st.append("receiveCompressed: ");
            st.append(receiveCompressed);
            st.append("\n");
            st.append("receiveMulticast: ");
            st.append(receiveMulticast);
            st.append("\n");
            st.append("transmitBytes: ");
            st.append(transmitBytes);
            st.append("\n");
            st.append("transmitPackets: ");
            st.append(transmitPackets);
            st.append("\n");
            st.append("transmitErrs: ");
            st.append(transmitErrs);
            st.append("\n");
            st.append("transmitDrop: ");
            st.append(transmitDrop);
            st.append("\n");
            st.append("transmitFifo: ");
            st.append(transmitFifo);
            st.append("\n");
            st.append("transmitColls: ");
            st.append(transmitColls);
            st.append("\n");
            st.append("transmitCarrier: ");
            st.append(transmitCarrier);
            st.append("\n");
            st.append("transmitCompressed: ");
            st.append(transmitCompressed);
            st.append("\n");
            st.append("time: ");
            st.append(timeMillis);
            st.append("\n");
            return st.toString();
        }
    }
    
}