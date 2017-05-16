package com.xilingyuli.demos.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

/**
 * Created by xilingyuli on 2017/3/12.
 */

public class FileUtil {
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory()+ File.separator + "Demos" + File.separator;
    public static boolean requestWritePermission(Activity activity){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return false;
            }
        }
        return true;
    }
    public static boolean saveFile(String name, byte[] content)
    {
        if(name==null||name.isEmpty())
            return false;
        FileOutputStream fos = null;
        try {
            File dir = new File(ROOT_PATH);
            if(!dir.exists())
                dir.mkdirs();
            File file = new File(ROOT_PATH+name);
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(content);
            fos.flush();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if(fos!=null)
                    fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static boolean saveImage(String name, Bitmap bitmap)
    {
        if(name==null||name.isEmpty())
            return false;
        FileOutputStream fos = null;
        try {
            File dir = new File(ROOT_PATH);
            if(!dir.exists())
                dir.mkdirs();
            File file = new File(ROOT_PATH+name);
            file.createNewFile();
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if(fos!=null)
                    fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static byte[] readFile(String name)
    {
        if(name==null||name.isEmpty())
            return null;
        FileInputStream fis = null;
        byte[] buffer;
        try {
            fis = new FileInputStream(ROOT_PATH+name);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            return buffer;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            try {
                if(fis!=null)
                    fis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static boolean renameFile(String oldName, String newName){
        File old = new File(ROOT_PATH+oldName);
        return old.renameTo(new File(ROOT_PATH+newName));
    }
    public static boolean deleteFile(String name){
        File file = new File(ROOT_PATH+name);
        return file.delete();
    }
}
