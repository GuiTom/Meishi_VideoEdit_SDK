package tech.qt.com.meishivideoeditsdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;

import VideoHandle.EpVideo;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageMovieWriter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import utils.CameraHelper;
import utils.CameraLoader;
import utils.FileUtils;
import utils.GPUImageFilterTools;
import utils.GPUImageFilterTools.FilterAdjuster;



public class CameraProtraitActivity extends Activity {

    private Button captureButton;

    private GPUImage mGPUImage;
    private GPUImageMovieWriter mMovieWriter;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;

    private GPUImageFilter mFilter;
    private FilterAdjuster mFilterAdjuster;
    private String videoOutPutPath;
    private ProgressBar progressBar;
    private SeekBar filterSeekBar;

    public static int videoProtrait=0;
    public static int videoLandscape=1;
    public static int videoSquare=2;
    private int videoDegree;
    private int videoHeight;
    private int videoWidth;
    private static final int PERMISSIONS_REQUEST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_protrait_camera);
        getInputParams();
        setUpIdsAndListeners();
        initCamera();

    }

    private void getInputParams() {
        Intent intent=getIntent();
        int videoShapeType=intent.getIntExtra("videoType",0);
        videoDegree=0;
        if(videoShapeType==videoProtrait){
            videoDegree=0;
            videoWidth=540;
            videoHeight=960;
        }else if(videoShapeType==videoLandscape){
            videoDegree=90;
            videoWidth=540;
            videoHeight=960;
        }else if(videoShapeType==videoSquare){
            videoDegree=0;
            videoWidth=videoHeight=540;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mGPUImage.setFilter(mMovieWriter);
        progressBar.setProgress(0);
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();

        if (mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Capturing) {
            mMovieWriter.stopRecording();
        }
    }

    private void initCamera() {
        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));

        mMovieWriter = new GPUImageMovieWriter();
        mMovieWriter.maxDuration=10;//多少秒
        mMovieWriter.recordCallBack=new GPUImageMovieWriter.RecordCallBack() {
            public int realProgres;
            @Override
            public void onRecordProgress(float progress) {
                 realProgres=(int)(progress*progressBar.getMax());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Log.e("reprogress",String.valueOf(realProgres));
                        progressBar.setProgress(realProgres);
                    }
                });
            }
            @Override
            public void onRecordEnd() {
                stopRecording();
            }
        };
        //                           //设置遮罩
//        String borderPath= Environment.getExternalStorageDirectory()+"/border1";
        int frameNum=30;
        GPUImageTwoInputFilter.bitmaps=new ArrayList<Bitmap>();
        for(int i=0;i<frameNum;i++){
//            String filePath=borderPath+String.format("/%04d.png",(i+1));
//            BitmapDrawable bitmapDrawable= (BitmapDrawable) BitmapDrawable.createFromPath(filePath);
//            Bitmap bitmap=bitmapDrawable.getBitmap();
//            GPUImageTwoInputFilter.bitmaps.add(bitmap);
            String fileName=String.format("images_yanhua/image_%d.png",(i+1));

            try {
                InputStream is = getApplicationContext().getAssets().open(fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                GPUImageTwoInputFilter.bitmaps.add(bitmap);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader(mCameraHelper,mGPUImage,this);
    }

    private void setUpIdsAndListeners() {
            captureButton=(Button)findViewById(R.id.button3);
            progressBar=(ProgressBar)findViewById(R.id.progressBar);
            filterSeekBar=(SeekBar)findViewById(R.id.seekBar);
            captureButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){//按下
                        if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Stoped) {
//                            videoOutPutPath = Environment.getExternalStorageDirectory() + "/outPut.mp4";
                            videoOutPutPath = FileUtils.getCaptureFile(Environment.DIRECTORY_MOVIES, ".mp4").toString();
                            mMovieWriter.startRecording(videoOutPutPath, videoWidth, videoHeight,videoDegree);
                        }else if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Paused) {
//                           mMovieWriter.resumeRecording();
                        }

                    }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){//抬起
                        if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Capturing) {
//                            mMovieWriter.pauseRecording();
                        }
                    }
                    return false;
                }
            });

            filterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mFilterAdjuster!=null&&mFilterAdjuster.canAdjust()){
                        mFilterAdjuster.adjust(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };



    private void stopRecording(){
        mMovieWriter.stopRecording();

        Intent intent=new Intent(this,VideoPlayerActivity.class);
        intent.putExtra("videoPath",videoOutPutPath);
        startActivity(intent);
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button4://完成
                stopRecording();
                break;
            case R.id.button5://选择滤镜
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                    }
                });
                break;
            case R.id.button6://切换镜头
                mCamera.switchCamera();
                break;
        }
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;

            GPUImageFilterGroup filters = new GPUImageFilterGroup();
            filters.addFilter(mFilter);
            filters.addFilter(mMovieWriter);

            mGPUImage.setFilter(filters);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);


        }
    }


}
