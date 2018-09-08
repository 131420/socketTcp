package com.zzw.socketdemo.socket.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {
    private final static String SAVE_FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"guanglan";


    public static void saveFileToLocal(byte[] data, boolean isBegin, String name) {
        //文件存放的目录
        File filePath = new File(SAVE_FILE_DIR);

        if (!filePath.exists())
            filePath.mkdirs();
        String fileName = filePath + File.separator + name;
        File file = new File(fileName);

        appendFile(data, isBegin, file);

    }


    public static void appendFile(byte[] data, boolean isBegin, File file) {

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file, !isBegin);
            os.write(data);
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
