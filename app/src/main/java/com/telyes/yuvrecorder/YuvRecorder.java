package com.telyes.yuvrecorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by funny on 2018/2/2.
 */

public class YuvRecorder {
    private  HandlerThread mThread;
    private  Handler mHandler;
    private String mPath;
    private Camera mCamera;
    public YuvRecorder(){
    }
    public void Start(String path,Camera camera){
        mCamera = camera;
        mStoped = false;
        mThread = new HandlerThread("yuvthread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mHandler.post(mRecordRunnable);
        mPath = path;
    }
    public void Stop(){
        mStoped = true;
        mThread.quit();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCamera = null;
    }
    List<byte[]> mBufferList = new ArrayList<>();
    public void AddBuffer(byte[] data){
        mBufferList.add(data);
    }
    public boolean IsRecording(){
        return mStoped == false;
    }
    private boolean mStoped = true;
    Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                FileOutputStream fos = new FileOutputStream(new File(mPath));
                while(!mStoped){
                    if(mBufferList.size()>0){
                        byte[] head = mBufferList.remove(0);
                        fos.write(head);
                        mCamera.addCallbackBuffer(head);
                    }
                }
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
