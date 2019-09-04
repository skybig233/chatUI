package com.example.chatui.util;

import com.example.chatui.MyApplication;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;

public class ZipUtil {
    public static String compressImage(String sourcePath, File destinationFile){
        final String finalSourcePath=sourcePath;
        final File finalDestinationFile=destinationFile;
        new Thread(){
            @Override
            public void run() {
                super.run();
                String desdestinationPath= SiliCompressor.with(MyApplication.getGlobalContext()).compress(finalSourcePath,finalDestinationFile);
            }
        }.start();
        return destinationPath;
    }
}
