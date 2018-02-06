package com.telyes.yuvrecorder;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Image;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private Camera mCamera;
    private int mPictureWidth = 1920;
    private int mPictureHeight = 1080;
    private SurfaceTexture mSurfaceTexture;
    private boolean mStarting;
    private YuvRecorder mYuvRecorder = new YuvRecorder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);
        TextureView view = (TextureView)this.findViewById(R.id.textureView);
        view.setSurfaceTextureListener(this);
    }
    private void initCamera(Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        List<Integer> formatList = parameters.getSupportedPreviewFormats();

        parameters.setPreviewFormat(ImageFormat.YV12);
        parameters.setPreviewFrameRate(25);
        parameters.setPreviewSize(mPictureWidth,mPictureHeight);
        parameters.setRecordingHint(true);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(0);
        final int BUFFER_SIZE = mPictureWidth*mPictureHeight*3/2;
        for(int i = 0;i<3;i++){
            camera.addCallbackBuffer(new byte[BUFFER_SIZE]);
        }
        camera.setPreviewCallbackWithBuffer(this);
    }
    private void openCamera() {
        if(mCamera != null){
            return ;
        }
        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);//注意处,如果没有设置preview,录像会报错.坑1
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera(mCamera);
        mCamera.startPreview();
    }
    private void closeCamera(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean startrecorder = false;
        if (event.getKeyCode() == KeyEvent.KEYCODE_F5) {
            //录像
            if(mYuvRecorder.IsRecording()){
                mYuvRecorder.Stop();
                playWave("av_end.wav");
            }else{
                mYuvRecorder.Start("/storage/emulated/0/1.yuv",mCamera);
                playWave("av_start.wav");
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_F3) {
            //录音
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
            //
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        this.openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(mYuvRecorder.IsRecording()){
            mYuvRecorder.AddBuffer(data);
        }else{
            mCamera.addCallbackBuffer(data);
        }
    }
    void playWave(String name){
        try {
            final AssetManager assetManager = this.getAssets();

            final AssetFileDescriptor afd = assetManager.openFd(name);
            final MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            player.setLooping(false);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        afd.close();
                        player.stop();
                        player.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
