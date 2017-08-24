package com.zxwtry.pictureServer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.zxwtry.infoCollect.socket.HDFSServer;
import com.zxwtry.infoCollect.socket.VMsCPUMEMIONETServer;
import com.zxwtry.pictureServer.util.Constants;
import com.zxwtry.pictureServer.util.MathUtil;

@Controller
public class Upload {
    
    static byte[] defaultPicture;
    static ServerHolder.ServerThread serverThread = null;
    static ExecutorService es = null;
    static byte[] ERROR = new byte[] {
    	(byte)'E', (byte)'R', (byte)'R', (byte)'O', (byte)'R' 
    };
    
    static final Logger logger = Logger.getLogger("Upload");
    
    static {
        try {
            File file = new File(Constants.DEFAULT_PICTURE_PATH);
            FileInputStream fis = new FileInputStream(file);
            defaultPicture = new byte[(int)file.length()];
            int di = 0;
            while (di < defaultPicture.length) {
                di += fis.read(defaultPicture, di, defaultPicture.length - di);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverThread = new ServerHolder.ServerThread();
        serverThread.start();
        es = Executors.newFixedThreadPool(1);
    }
    
    
    @RequestMapping("/upload")
    public void upload(@RequestParam MultipartFile file,
            @RequestParam String uid , ServletResponse resp) {
        System.out.println("com.zxwtry.ssm.controller --> upload");
        System.out.println("file == null --> " + (file == null));
        System.out.println("uid --> " + uid);
        
        int un = uid == null ? 0 : uid.length();
        long uidLong = 0;
        for (int i = 0; i < un; i ++) {
        	uidLong = uidLong * 10 + (uid.charAt(i) - '0');
        }
        if (uidLong < 0) {
        	uidLong = - uidLong;
        }
        int times = 0;
        while (true) {
            try {
            
    	        byte[] fileByteArray = file.getBytes();
    	        
    	        int len = fileByteArray.length;
    	        
    	        byte[] lenUid = MathUtil.getByte(len, uidLong);
    	        
    	        ServerThreadCallable callable = new ServerThreadCallable(
    	        		lenUid, fileByteArray);
    	        
    	        FutureTask<byte[][]> futureTask = new FutureTask<>(callable);
    	        
    	        es.submit(futureTask);
    	        
    	        byte[][] rece = futureTask.get();
    	        
    	        resp.getOutputStream().write(rece[0]);
    	        break;
    		} catch (Exception e) {

    		    try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
    		    times ++;
    		    if (times > 5) {
                    try {
                        resp.getOutputStream().write(ERROR);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
    		        break;
    		    }
    		    logger.info("Upload --> upload exception retry " + times);
    		    logger.info("Upload --> upload exception " + e.getMessage());
    		}
        }
        
    }
    
    @RequestMapping(value="/wap180/{fn}", method=RequestMethod.GET)
    public void wap180(@PathVariable("fn") String fn, HttpServletResponse resp) {
        byte[] bs = new byte[41];	//42 = 4 + 1 + 36
        bs[4] = 0;
        try {
			solveShow(fn, resp, bs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    }

    @RequestMapping(value="/bmiddle/{fn}", method=RequestMethod.GET)
    public void bmiddle(@PathVariable("fn") String fn, HttpServletResponse resp) {
        byte[] bs = new byte[41];	//42 = 4 + 1 + 36
        bs[4] = 1;
        try {
			solveShow(fn, resp, bs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    }
    
    @RequestMapping(value="/large/{fn}", method=RequestMethod.GET)
    public void large(@PathVariable("fn") String fn, HttpServletResponse resp) {
        byte[] bs = new byte[41];	//42 = 4 + 1 + 36
        bs[4] = 2;
        try {
			solveShow(fn, resp, bs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    }
    
    
    void solveShow( String fn, HttpServletResponse resp, byte[] bs) throws InterruptedException, ExecutionException {
    	int notNameIndex = 0;
        int fnLenght = fn.length();
        int bi = 5;
        for (; notNameIndex < fnLenght && notNameIndex < Constants.FILE_NAME_LENGTH;
        		notNameIndex ++) {
            char c = fn.charAt(notNameIndex);
            if ( ! (c >= 'a' && c <= 'z')  && ! (c >= 'A' && c <= 'Z') &&
                    ! (c >= '0' && c <= '9')) {
                break;
            } else {
            	bs[bi ++] = (byte) c;
            }
        }
        if (notNameIndex != Constants.FILE_NAME_LENGTH) {
            //返回默认无效图片
            write(defaultPicture, resp);
        } else {
        	int len = -(Constants.FILE_NAME_LENGTH + 1);
        	bs[0] = (byte) ((len >>>  0) & 0xff);
        	bs[1] = (byte) ((len >>>  8) & 0xff);
        	bs[2] = (byte) ((len >>> 16) & 0xff);
        	bs[3] = (byte) ((len >>> 24) & 0xff);
        	ServerThreadCallable callable = new ServerThreadCallable(bs, null);
        	FutureTask<byte[][]> futureTask = new FutureTask<>(callable);
        	es.submit(futureTask);
        	byte[][] ans = futureTask.get();
        	write(ans[0], resp);
        }
    }

    @ExceptionHandler
    public void exception(HttpServletResponse resp) {
        write(defaultPicture, resp);
    }
    
    @RequestMapping(value="/msg")
    public void vmMsg(HttpServletResponse resp) {
    	byte[] msg = VMsCPUMEMIONETServer.getMsg();
    	int size = MathUtil.getInt(msg, 0);
    	try {
			OutputStream os = resp.getOutputStream();
			os.write(msg, 4, size);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    @RequestMapping(value="/hdfs")
    public void hdfs(HttpServletResponse resp) {
		try {
			OutputStream os = resp.getOutputStream();
	    	byte[] msg = HDFSServer.getHDFSMsg();
	    	int msgIndex = HDFSServer.getHDFSMsgIndex();
	    	if (msg != null && msgIndex != 0) {
				os.write(msg, 0, msgIndex);
	    	}
	    	os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @RequestMapping(value="/default")
    public void notFound(HttpServletResponse resp) {
        write(defaultPicture, resp);
    }
    
    @RequestMapping(value="/error")
    public void error(HttpServletResponse resp) {
        resp.setStatus(500);
        throw new RuntimeException("run time exception!");
    }
    
    private void write(byte[] bs, HttpServletResponse resp) {
        try {
            OutputStream os = resp.getOutputStream();
            os.write(bs);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
