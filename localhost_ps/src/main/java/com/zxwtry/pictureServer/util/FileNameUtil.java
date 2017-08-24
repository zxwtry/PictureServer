package com.zxwtry.pictureServer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class FileNameUtil {
    
    public static String getFileName(byte[] bs, long uid, Date time) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md5 == null) return null;
        //[00-21] md5
        //[22-28] uid
        //[29-35] time
        char[] fileNameChars = new char[36];
        int[] md5Ints = new int[22];
        md5.update(bs);
        byte[] md5Bytes = md5.digest();
        for (int md5ByteIndex = 0; md5ByteIndex < 16; md5ByteIndex ++) {
            for (int adjustIndex = 21; adjustIndex > -1; adjustIndex --)
                md5Ints[adjustIndex] = md5Ints[adjustIndex] << 8;
            md5Ints[21] += md5Bytes[md5ByteIndex] & 0x0FF;
            int carry = 0, sum = 0;
            for (int adjustIndex = 21; adjustIndex > -1; adjustIndex --) {
                sum = md5Ints[adjustIndex] + carry;
                md5Ints[adjustIndex] = sum % 62;
                carry = sum / 62;
            }
        }
        for (int fileNameCharsIndex = 0; fileNameCharsIndex < 22; 
                fileNameCharsIndex ++) 
            fileNameChars[fileNameCharsIndex] = convertINT2FNC(
                    md5Ints[fileNameCharsIndex]);
        for (int fileNameCharsIndex = 28; fileNameCharsIndex >= 22; 
                fileNameCharsIndex --) {
            fileNameChars[fileNameCharsIndex] = convertINT2FNC((int)(uid % 62));
            uid /= 62;
        }
        long timeMills = time.getTime() - Constants.TimeMillis20100101;
        for (int fileNameCharsIndex = 35; fileNameCharsIndex >= 29; 
                fileNameCharsIndex --) {
            fileNameChars[fileNameCharsIndex] = convertINT2FNC((int)
                    (timeMills % 62));
            timeMills /= 62;
        }
        return new String(fileNameChars);
    }
    //val: [0,61]
    //char: ['a' - 'z'] - [00, 25]
    //char: ['0' - '9'] - [26, 35]
    //char: ['A' - 'Z'] - [36, 61]
    public static char convertINT2FNC(int val) {
        if (val < 26) return (char)('a' + val);
        if (val < 36) return (char)('0' + val - 26);
        return (char)('A' + val - 36);
    }
    public static String getMD5FromFileName(String fileName) {
        char[] md5 = new char[32];  //md5 128bit == 32*4 bit
        int[] md5Int = new int[32];
        for (int fileNameIndex = 0; fileNameIndex < 22; fileNameIndex += 2) {
            for (int md5Index = 0; md5Index < 32; md5Index ++)
                md5Int[md5Index] *= 3844;
            md5Int[31] += convertFNC2INT(fileName.charAt(fileNameIndex)) * 62 + 
                    convertFNC2INT(fileName.charAt(fileNameIndex + 1));
            int carry = 0, sum = 0;
            for (int md5Index = 31; md5Index > -1; md5Index --) {
                sum = md5Int[md5Index] + carry;
                md5Int[md5Index] = sum & 0x0F;
                carry = sum >> 4;
            }
        }
        for (int md5Index = 31; md5Index > -1; md5Index --)
            md5[md5Index] = Constants.HEXCHAR[md5Int[md5Index]];
        return new String(md5);
    }
    public static long getUIDFromFileName(String fileName) {
        long uid = 0l;
        for (int fileNameIndex = 22; fileNameIndex < 29; fileNameIndex ++) {
            uid = uid * 62;
            uid += convertFNC2INT(fileName.charAt(fileNameIndex));
        }
        return uid;
    }
    public static Date getTimeFromFileName(String fileName) {
        long time = 0l;
        for (int fileNameIndex = 29; fileNameIndex < 36; fileNameIndex ++) {
            time = time * 62;
            time += convertFNC2INT(fileName.charAt(fileNameIndex));
        }
        return new Date(Constants.TimeMillis20100101 + time);
    }
    //'0' - '9' : 48 - 57  : c-'0'+26
    //'A' - 'Z' : 65 - 90  : c-'A'+36
    //'a' - 'z' : 97 - 122 : c-'a'
    public static int convertFNC2INT(char c) {
        if (c <= '9') return c - '0' + 26;
        if (c <= 'Z') return c - 'A' + 36;
        return c - 'a';
    }
}
