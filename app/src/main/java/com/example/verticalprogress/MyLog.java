package com.example.verticalprogress;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyLog extends Application {
    public static Context mContext;

    public String TAG = MyLog.class.getSimpleName();

    //디렉토리 생성
    File appDirectory = new File(Environment.getExternalStorageDirectory() + "/MyApp");
    File logDirectory = new File(appDirectory + "/logs");


    //필터링한 Log저장
    public void inputLog(String text) {
        File logFile = new File(logDirectory, "Log_ImgSummary.txt"); //파일명

        if (!logFile.exists()) { //없으면 파일 생성
            try {
                Log.d(TAG, "파일생성");
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss.SSS");
            String strDate = mdformat.format(calendar.getTime());

            //헤더
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(strDate + "|" + text);
            buf.newLine();
            buf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        //Full Log저장
        File logFile = new File(logDirectory, "fullLog.txt"); //파일명

        if (!logFile.exists()) { //없으면 파일 생성
            try {
                Log.d(TAG, "full파일생성");
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //이전 logcat 을 지우고 파일에 새 로그을 씀
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }


}