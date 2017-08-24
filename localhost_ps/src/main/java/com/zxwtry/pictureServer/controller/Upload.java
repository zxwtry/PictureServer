package com.zxwtry.pictureServer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.zxwtry.pictureServer.client.ClientHolder;
import com.zxwtry.pictureServer.client.HDFSClient;
import com.zxwtry.pictureServer.client.ReadThread;
import com.zxwtry.pictureServer.dao.PictureInfoDao;
import com.zxwtry.pictureServer.entity.PictureInfo;
import com.zxwtry.pictureServer.util.Constants;
import com.zxwtry.pictureServer.util.Constants.PICTURETYPE;
import com.zxwtry.pictureServer.util.FileNameUtil;
import com.zxwtry.pictureServer.util.HadoopUtil;
import com.zxwtry.pictureServer.util.JPGConvertor;

@Controller
public class Upload {
    
    static byte[] defaultPicture;
    static ClientHolder.ClientThread clientThread;
    static ReadThread readThread;
    static HDFSClient.HDFSSendThread hdfsClient = null;
    
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
        clientThread = new ClientHolder.ClientThread();
        readThread = new ReadThread();
        hdfsClient = new HDFSClient.HDFSSendThread();
        clientThread.start();
        readThread.start();
        hdfsClient.start();
    }
    
    @Autowired
    private PictureInfoDao pictureInfoDao;
    
    @RequestMapping("/upload")
    public void upload(@RequestParam MultipartFile file,
            @RequestParam long uid , PrintWriter pw) {
        
        System.out.println("com.zxwtry.ssm.controller --> upload");
        System.out.println("file == null --> " + (file == null));
        System.out.println("uid --> " + uid);
        try {
            byte[] bs = file.getBytes();
            String fileName = FileNameUtil.getFileName(bs, uid, new Date());
            PictureInfo pi = new PictureInfo();
            pi.setFileName(fileName);
            HadoopUtil.append(PICTURETYPE.large, bs, pi);
            byte[][] nba = JPGConvertor.convertToWap180AndBmiddle(bs);
            HadoopUtil.append(PICTURETYPE.wap180, nba[0], pi);
            HadoopUtil.append(PICTURETYPE.bmiddle, nba[1], pi);
            pw.write(fileName);
            pw.flush();
            pictureInfoDao.insert(pi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @RequestMapping(value="/wap180/{fn}", method=RequestMethod.GET)
    public void wap180(@PathVariable("fn") String fn, HttpServletResponse resp) {
        solvePicture(PICTURETYPE.wap180, fn, resp);
    }

    @RequestMapping(value="/bmiddle/{fn}", method=RequestMethod.GET)
    public void bmiddle(@PathVariable("fn") String fn, HttpServletResponse resp) {
        solvePicture(PICTURETYPE.bmiddle, fn, resp);
    }
    
    @RequestMapping(value="/large/{fn}", method=RequestMethod.GET)
    public void large(@PathVariable("fn") String fn, HttpServletResponse resp) {
        solvePicture(PICTURETYPE.large, fn, resp);
    }
    
    @ExceptionHandler
    public void exception(HttpServletResponse resp) {
        write(defaultPicture, resp);
    }
    
    @RequestMapping(value="/default")
    public void notFound(HttpServletResponse resp) {
        readThread.setPictureInfoDao(pictureInfoDao);
        write(defaultPicture, resp);
    }
    
    @RequestMapping(value="/error")
    public void error(HttpServletResponse resp) {
        resp.setStatus(500);
        throw new RuntimeException("run time exception!");
    }
    
    private void solvePicture(PICTURETYPE pt, String fn,
            HttpServletResponse resp) {
        int notNameIndex = 0;
        int fnLenght = fn.length();
        for (; notNameIndex < fnLenght; notNameIndex ++) {
            char c = fn.charAt(notNameIndex);
            if ( ! (c >= 'a' && c <= 'z')  && ! (c >= 'A' && c <= 'Z') &&
                    ! (c >= '0' && c <= '9')) {
                break;
            }
        }
        if (notNameIndex != Constants.FILE_NAME_LENGTH) {
            //返回默认无效图片
            write(defaultPicture, resp);
        } else {
            fn = fn.substring(0, notNameIndex);
            PictureInfo pi = pictureInfoDao.queryByFileName(fn);
            byte[] bs = HadoopUtil.read(pt, pi);
            write(bs, resp);
        }
    }
    
    private void write(byte[] bs, HttpServletResponse resp) {
        try {
            OutputStream os = resp.getOutputStream();
            os.write(bs);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
