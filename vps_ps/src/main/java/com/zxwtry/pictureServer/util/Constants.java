package com.zxwtry.pictureServer.util;

public class Constants {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String TMP_DIR = OS_NAME.charAt(0) != 'W' ?
            "/var/tmp" : "F:/tmp";
    public static final char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    public static final long TimeMillis20100101 = 1262275200000l;
    public static final String FORMAT_NAME = "jpg";
    public static final String uriAddress = "hdfs://node101:9000/";
    public static final String CHUNK_PATH_PREFIX = "hdfs://node101:9000/";
    public static String pathMiddle = "pic_ser_test/";
    public final static String HADOOP_HOME_KEY = "hadoop.home.dir";
    public final static String HADOOP_HOME_VAL = 
            OS_NAME.charAt(0) != 'W' ? "/var/tmp/hadoop-2.7.1" :
            "E:/file/vmfile/hadoop_2_dot_7/hadoop-2.7.1";
    public final static String HADOOP_APPEND_KEY = "dfs.support.append";
    public final static boolean HADOOP_APPEND_VAL = true;
    public final static String HADOOP_APPEND_POLICY_KEY = 
            "dfs.client.block.write.replace-datanode-on-failure.policy";
    public final static String HADOOP_APPEND_POLICY_VAL = "NEVER";
    public final static String HADOOP_APPEND_ENABLE_KEY = 
            "dfs.client.block.write.replace-datanode-on-failure.enable";
    public final static String HADOOP_APPEND_ENABLE_VAL = "true";
    public final static int CHUNK_ARRAY_LENGTH = 1;
    public static final String CHUNK_INFO_PATH = TMP_DIR + "/picture_server_chunk.info";
    public static final String CHUNK_SINGLE_PATH = TMP_DIR + "/picture_server_chunk.info";
    public static final String DEFAULT_PICTURE_PATH = TMP_DIR + "/default.jpg";
    public static final int CHUNK_SINGLE_LENGTH = 4 + 4;
    public static final int CHUNK_SINGLE_INDEX_POSITION = 0;
    public static final int CHUNK_SINGLE_OFFSET_POSITION = 4;
    public static final int CHUNK_FILE_NAME_LENGTH = 4;
    public static final int CHUNK_INDEX_LENGTH = 4;
    public static final int CHUNK_SIZE = CHUNK_FILE_NAME_LENGTH + CHUNK_INDEX_LENGTH;
                                    //单个CHUNK的长度
    public static final int CHUNK_INFO_LENGTH = CHUNK_ARRAY_LENGTH * CHUNK_SIZE
            + CHUNK_FILE_NAME_LENGTH;     //存放下一个CHUNK的index
    public static final int WAITING_FOR_CHUNK_SLEEP_MILLIS = 100;
    public static final int HADOOP_CHUNK_MAX_SIZE = 128 * 1024 * 1024; //128MB
    public static enum PICTURETYPE {
        wap180, bmiddle, large
    }
    public static final short STATUS_NORMAL = (short)0;
    public static final short STATUS_DELETED = (short)1;
    
    public static final short[] STATUS_ARRAY = {
        STATUS_NORMAL, STATUS_DELETED
    };
    
    public static final int FILE_NAME_LENGTH = 36;
    public static final byte BYTE_COMMA = ',';
    public static final byte BYTE_BRACE_LEFT = '{';
    public static final byte BYTE_BRACE_RIGHT = '}';
    public static final byte BYTE_DOUBLE_QUOTATION = '\"';
    public static final byte BYTE_ZERO = '0';
    public static final byte BYTE_NINE = '9';
    public static final byte BYTE_COLON = ':';
    public static final byte BYTE_NEW_LINE = '\n';
    public static final byte BYTE_BLANK = ' ';
    public static final byte BYTE_DOT = '.';
    public static final byte BYTE_SLASH = '/';
    public static final byte BYTE_s = 's';
    public static final byte BYTE_B = 'B';
    public static final byte[] BYTE_ARRAY_UNIT = {
        'K', 'M', 'G', 'T'
    };
    public static final long[] BYTE_ARRAY_UNIT_RANGE = {
        1 << 10, 1 << 20, 1 << 30, 1 << 40
    };
    public static final byte BYTE_AND = '&';
    public static final byte BYTE_n = 'n';
    public static final byte BYTE_b = 'b';
    public static final byte BYTE_p = 'p';
    public static final byte BYTE_SEMICOLON = ';';
    public static final byte BYTE_LESS_THAN = '<';
    public static final byte BYTE_GREATER_THAN = '>';
    public static final byte BYTE_r = 'r';
    public static final byte BYTE_e = 'e';
    

}
