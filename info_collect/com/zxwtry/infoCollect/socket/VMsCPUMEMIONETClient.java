package com.zxwtry.infoCollect.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.zxwtry.infoCollect.util.Constants;
import com.zxwtry.infoCollect.util.FileUtils;
import com.zxwtry.infoCollect.util.MathUtils;

/**
 * @author      zxwtry
 * @email       zxwtry@qq.com
 * @project     Copy of PictureServerInfoCollectUDP
 * @package     com.zxwtry.infoCollect.socket
 * @file        VMsCPUMEMIONETClient.java
 * @date        2017年8月24日 上午10:23:12
 * @details     VMs：虚机
 * @details     监控：CPU、MEM、IO、NET
 * @details     CPU： uptime获得1min、5min、10min的load average
 * @details     MEM： free -h获得 total memory 和 free memory
 * @details     IO：    iostat -d 1 2 使用第二条信息
 * @details     NET：  使用自己写的TraffixRXTXMonitor实现
 */
public class VMsCPUMEMIONETClient {
	
	static String ip = "115.159.160.199";
//	static String ip = "192.168.15.174";
	
	static InetAddress address = null;
	static DatagramSocket socket = null;
    
    static int port = 9188;
    //只有一个线程
    static byte[] hostNameBytes = null;
    static int hostNameBytesIndex = 0;
    static {
    	try {
			InetAddress localAddress = InetAddress.getLocalHost();
			String hostName = localAddress.getHostName();
			int hostNameLength = hostName == null ? 0 : hostName.length();
			hostNameBytes = new byte[hostNameLength + 1];
			for (int i = 0; i < hostNameLength; i ++) {
				hostNameBytes[i] = (byte) hostName.charAt(i);
			}
			hostNameBytes[hostNameLength] = Constants.BYTE_BLANK;
			hostNameBytesIndex = hostNameLength + 1;
			address = InetAddress.getByName(ip);
			socket = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    static String cpuString = "uptime";
    static String memString = "free -h";
    static String ioString = "iostat -d 1 2";
    static String netInterfaceName = "eth0";
    static byte[] buff = new byte[256];
    static byte[] sendBuff = new byte[2 * 1024];
    static int sendBuffIndex = 0;
    
    
    static TraffixRXTXMonitor net = new TraffixRXTXMonitor(1500);
    
    public static void main(String[] args) throws Exception {
    	solve();
    }
    
    private static void updateAll() {
        sendBuffIndex = 0;
        updateHostName();
        updateCpu();
        updateMem();
        updateIO();
        updateNet();
    }
    
    private static void updateHostName() {
        for (int i = 0; i < hostNameBytesIndex; i ++) {
            sendBuff[sendBuffIndex ++] = hostNameBytes[i];
        }
    }

    private static void updateNet() {
    	int[] rxtx = net.queryRXTXSpeedOfInterface(netInterfaceName);
    	if (rxtx == null) {
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_ZERO;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_B;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_SLASH;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_s;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_ZERO;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_B;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_SLASH;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_s;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
    	} else {
    	    sendBuffIndex = MathUtils.humanKindNumber(sendBuff, sendBuffIndex, rxtx[0]);
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_B;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_SLASH;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_s;
    	    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
    	    sendBuffIndex = MathUtils.humanKindNumber(sendBuff, sendBuffIndex, rxtx[1]);
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_B;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_SLASH;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_s;
    		sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
    	}
	}

	public static void solve() {
    	while (true) {
            try {
        		
                updateAll();
                
        		DatagramPacket packet = new DatagramPacket
        		        (sendBuff, 0, sendBuffIndex, address, port);
        		socket.send(packet);
        		
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void updateIO() {
    	try {
			Process process = Runtime.getRuntime().exec(ioString);
			process.waitFor();
			int size = FileUtils.readInputStream(process.getInputStream(), buff);
			if (size == FileUtils.READ_FILE_BS_EXPAND) {
				buff = new byte[2 * buff.length + 1];
				updateMem();
			} else if (size == FileUtils.READ_FILE_ERROR) {
				System.out.println("read error");
			} else {
				int i = 0;
				int c = 0;
				for (; i < size; i ++) {
					if (buff[i] == Constants.BYTE_NEW_LINE) {
						c ++;
						if (c == 6) {
							break;
						}
					}
				}
				i ++;
				byte p = Constants.BYTE_BLANK;
				for (; i < size; i ++) {
					byte b = buff[i];
					if (b == Constants.BYTE_BLANK) {
						if (p != Constants.BYTE_BLANK) {
							sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
						}
					} else if (b == Constants.BYTE_NEW_LINE) {
						break;
					} else {
					    sendBuff[sendBuffIndex ++] = b;
					}
					p = b;
				}
				if (sendBuff[sendBuffIndex - 1] != Constants.BYTE_BLANK) {
				    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void updateMem() {
    	try {
			Process process = Runtime.getRuntime().exec(memString);
			process.waitFor();
			int size = FileUtils.readInputStream(process.getInputStream(), buff);
			if (size == FileUtils.READ_FILE_BS_EXPAND) {
				buff = new byte[2 * buff.length + 1];
				updateMem();
			} else if (size == FileUtils.READ_FILE_ERROR) {
				System.out.println("read error");
			} else {
				int i = 0;
				for (; i < size; i ++) {
					if (buff[i] == Constants.BYTE_NEW_LINE) {
						break;
					}
				}
				for (; i < size; i ++) {
					if (buff[i] == Constants.BYTE_BLANK) {
						break;
					}
				}
				for (; i < size; i ++) {
					if (buff[i] != Constants.BYTE_BLANK) {
						break;
					}
				}
				byte p = Constants.BYTE_BLANK;
				for (; i < size; i ++) {
					byte b = buff[i];
					if (b == Constants.BYTE_BLANK) {
						if (p != Constants.BYTE_BLANK) {
							sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
						}
					} else if (b == Constants.BYTE_NEW_LINE) {
						break;
					} else {
					    sendBuff[sendBuffIndex ++] = b;
					}
					p = b;
				}
				if (sendBuff[sendBuffIndex - 1] != Constants.BYTE_BLANK) {
				    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void updateCpu() {
    	try {
			Process process = Runtime.getRuntime().exec(cpuString);
			process.waitFor();
			int size = FileUtils.readInputStream(process.getInputStream(), buff);
			if (size == FileUtils.READ_FILE_BS_EXPAND) {
				buff = new byte[2 * buff.length + 1];
				updateCpu();
			} else if (size == FileUtils.READ_FILE_ERROR) {
				System.out.println("read error");
			} else {
				int i = size - 1;
				for (; i > -1; i --) {
					byte b = buff[i];
					if (b == Constants.BYTE_COLON) {
						break;
					}
				}
				boolean p = false;
				for (i ++; i < size; i ++) {
					byte b = buff[i];
					if ((b >= Constants.BYTE_ZERO && b <= Constants.BYTE_NINE) || 
							b == Constants.BYTE_DOT) {
					    sendBuff[sendBuffIndex ++] = b;
						p = true;
					} else {
						if (p) {
						    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
						}
						p = false;
					}
				}
				if (sendBuff[sendBuffIndex - 1] != Constants.BYTE_BLANK) {
				    sendBuff[sendBuffIndex ++] = Constants.BYTE_BLANK;
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    }
}
