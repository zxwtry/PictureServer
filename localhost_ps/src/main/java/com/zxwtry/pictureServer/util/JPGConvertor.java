package com.zxwtry.pictureServer.util;

import java.awt.Graphics;
import java.awt.Image; 
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class JPGConvertor {
    
    public static void main(String[] args) throws Exception {
        String fn = "F:/tmp/f.png";
        File f = new File(fn);
        byte[] bs = new byte[(int)f.length()];
        FileInputStream fis = new FileInputStream(f);
        int bi = 0;
        while (bi < bs.length) {
            bi += fis.read(bs, bi, bs.length - bi);
        }
        fis.close();
        byte[][] nba = convertToWap180AndBmiddle(bs);
        FileOutputStream fos = new FileOutputStream(new File("F:/tmp/f1.png"));
        fos.write(nba[0]);
        fos.close();
        fos = new FileOutputStream(new File("F:/tmp/f2.png"));
        fos.write(nba[1]);
        fos.close();
    }

    public static byte[][] convertToWap180AndBmiddle(byte[] bs) {
        byte[][] ans = new byte[2][];
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            BufferedImage origin = ImageIO.read(bis);
            BufferedImage wap180 = convertWap180(origin);
            BufferedImage bmiddle = convertBMiddle(origin);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(wap180, Constants.FORMAT_NAME, bos);
            ans[0] = bos.toByteArray();
            bos = new ByteArrayOutputStream();
            ImageIO.write(bmiddle, Constants.FORMAT_NAME, bos);
            ans[1] = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
    
    public static BufferedImage convertBMiddle(BufferedImage origin) {
        int largeWidth = origin.getWidth();
        int largeHeight = origin.getHeight();
        int bmiddleWidth = 440;
        int bmiddleHeight = largeHeight * bmiddleWidth / largeWidth;
        return convert(origin, bmiddleWidth, bmiddleHeight);
    }

    public static BufferedImage convertWap180(BufferedImage origin) {
        int largeWidth = origin.getWidth();
        int largeHeight = origin.getHeight();
        int wap180Height = 180;
        int wap180Width = largeWidth * wap180Height / largeHeight;
        return convert(origin, wap180Width, wap180Height);
    }

    public static BufferedImage convert(BufferedImage origin, int width,
            int height) {
        Image targetImage = origin.getScaledInstance(width, height,
                Image.SCALE_DEFAULT);
        BufferedImage target = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics graphics = target.getGraphics();
        graphics.drawImage(targetImage, 0, 0, null);
        graphics.dispose();
        return target;
    }

    public static BufferedImage read(String fileName) throws IOException {
        return ImageIO.read(new File(fileName));
    }

    public static BufferedImage read(InputStream inputStream)
            throws IOException {
        return ImageIO.read(inputStream);
    }

    public static boolean save(BufferedImage image, String fileName)
            throws IOException {
        return ImageIO.write(image, Constants.FORMAT_NAME, new File(fileName));
    }

    public static boolean save(BufferedImage image, OutputStream outputStream)
            throws IOException {
        return ImageIO.write(image, Constants.FORMAT_NAME, outputStream);
    }
}
