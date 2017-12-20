package tech.qt.com.meishivideoeditsdk;

import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageBeautyFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMovieWriter;
import tech.qt.com.meishivideoeditsdk.camera.CameraManager;
import tech.qt.com.meishivideoeditsdk.camera.CameraWraper;
import tech.qt.com.meishivideoeditsdk.camera.GLRender;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUBeautyFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUFilterTool;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUGourpFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.MovieWriter;
import utils.FileUtils;
import utils.UIUtils;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;

public class CameraProtraitActivity2 extends AppCompatActivity {

    private CameraWraper mCamera;
    private GLSurfaceView glSurfaceView;
    private GPUImageBeautyFilter gpuImageBeautyFilter;
    private MovieWriter mMovieWriter;
    private int videoDegree;
    private int videoHeight;
    private int videoWidth;

    public static int videoProtrait=0;
    public static int videoLandscape=1;
    public static int videoSquare=2;
    private ProgressBar progressBar;

    private long startTime;
    private GPUGourpFilter gpuGourpFilter;

    private GPUFilter gpuBeautyFilter;
    private GPUFilter gpuCommonFilter;
    private GPUFilter gpuBlendScreenFilter;
    private Camera.Size preViewSize;
    private int facingType;
    private String musicPath;
    private TextView timeTextView;
    private ImageButton captureButton;
    private int realProgres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInputParams();
        setContentView(R.layout.activity_camera_protrait2);

        setUpUIComponentIds();
        openCamera();

    }
    private void getInputParams() {
        Intent intent=getIntent();
        int videoShapeType=intent.getIntExtra("videoType",0);
        videoDegree=0;
        if(videoShapeType==videoProtrait){
            videoDegree=0;
            videoWidth=1080;
            videoHeight=1920;
        }else if(videoShapeType==videoLandscape){
            videoDegree=90;
            videoWidth=540;
            videoHeight=960;
        }else if(videoShapeType==videoSquare){
            videoDegree=0;
            videoWidth=videoHeight=540;
        }
    }
    private void setUpUIComponentIds() {

        glSurfaceView = (GLSurfaceView)findViewById(R.id.surfaceView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar3);
        timeTextView =(TextView)findViewById(R.id.timeTextView);
        captureButton =(ImageButton)findViewById(R.id.imageButton14);
        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){//按下
                    if(mMovieWriter.recordStatus == MovieWriter.RecordStatus.Stoped ){
                        if(mMovieWriter.outputVideoFile==null) {
                            String videoOutPutPath = Environment.getExternalStorageDirectory() + "/" + FileUtils.getDateTimeString() + ".mp4";
                            File file = new File(videoOutPutPath);
                            if (file.exists()) {
                                file.delete();
                            }
                            mMovieWriter.outputVideoFile = videoOutPutPath;
                        }
                        mMovieWriter.startRecording(videoWidth,videoHeight,videoDegree,musicPath);
                        captureButton.setSelected(!captureButton.isSelected());
                    }

                }else if(event.getAction()==MotionEvent.ACTION_UP){//抬起
                    mMovieWriter.stopRecording();
                    captureButton.setSelected(!captureButton.isSelected());
                }

                return false;
            }
        });
    }

    private void openCamera() {
        facingType = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCamera = CameraManager.getManager().openCamera( facingType);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
         preViewSize = CameraManager.getClosestSupportedSize(sizes,1280,720);
        if(parameters.getSupportedFocusModes().contains(FOCUS_MODE_CONTINUOUS_VIDEO)){
            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.setPreviewSize(preViewSize.width,preViewSize.height);

        parameters.setPreviewFrameRate(25);
        parameters.setRecordingHint(true);

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);

        CameraManager.getManager().setGlSurfaceView(glSurfaceView);
        mMovieWriter = new MovieWriter(getApplicationContext());
        mMovieWriter.maxDuration = 100;
        mMovieWriter.setFirstLayer(true);
        CameraManager.getManager().setFilter(mMovieWriter);
        mMovieWriter.recordCallBack = new MovieWriter.RecordCallBack() {
            @Override
            public void onRecordProgress(final float progress) {

                realProgres=(int)(progress*progressBar.getMax());
                final long timeSeconds = mMovieWriter.maxDuration*realProgres/100;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Log.e("reprogress",String.valueOf(realProgres));
                        progressBar.setProgress(realProgres);

                        timeTextView.setText(String.format("%02d",timeSeconds/60)+":"+String.format("%02d",timeSeconds%60));
                    }
                });
            }

            @Override
            public void onRecordTimeEnd() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"已达到最大视频时长",Toast.LENGTH_LONG);
                        mMovieWriter.stopRecording();
                    }
                });
            }

            @Override
            public void onRecordFinish(String filePath) {

                mMovieWriter.outputVideoFile = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"拼接用时"+(System.currentTimeMillis()-startTime),Toast.LENGTH_LONG);
                    }
                });
                Intent intent=new Intent(getApplicationContext(),VideoPlayerActivity.class);
                intent.putExtra("videoPath",filePath);
                startActivity(intent);
            }
        };

    }
    private void finishRecording(){
        mMovieWriter.finishRecording();
        startTime = System.currentTimeMillis();
    }
    public void onClick(View view){
//        Toast.makeText(getApplicationContext(),"xab",Toast.LENGTH_LONG);
        final ImageButton imageButton = (ImageButton)view;
        switch (imageButton.getId()){
            case R.id.imageButton14://录制
                if(mMovieWriter.recordStatus == MovieWriter.RecordStatus.Stoped ){
                    if(mMovieWriter.outputVideoFile==null) {
                        String videoOutPutPath = Environment.getExternalStorageDirectory() + "/" + FileUtils.getDateTimeString() + ".mp4";
                        File file = new File(videoOutPutPath);
                        if (file.exists()) {
                            file.delete();
                        }
                        mMovieWriter.outputVideoFile = videoOutPutPath;
                    }
                    mMovieWriter.startRecording(videoWidth,videoHeight,videoDegree,musicPath);
                    imageButton.setSelected(!imageButton.isSelected());
                }else {
                    mMovieWriter.stopRecording();
                    imageButton.setSelected(!imageButton.isSelected());
                }
                break;
            case R.id.imageButton15://回删
                if(mMovieWriter.recordStatus != MovieWriter.RecordStatus.Stoped) {
//                    Toast.makeText(t"请先停止拍摄再进行次操作",Toast.LENGTH_LONG);
                   return;
                }
                mMovieWriter.fallBack();
                break;
            case R.id.imageButton16://结束录制
                    finishRecording();
                break;
            case R.id.imageButton17://添加滤镜
                GPUFilterTool.showFilterDialog(this, new GPUFilterTool.onGpuFilterChosenListener() {
                    @Override
                    public void onGpuFilterChosenListener(GPUFilter filter) {
                        imageButton.setSelected(true);
                        gpuCommonFilter = filter;
                        addFilters();
                    }
                });

                break;
            case R.id.imageButton18://添加特效
                GPUFilterTool.showCoverDialog(this, new GPUFilterTool.onGpuFilterChosenListener() {
                    @Override
                    public void onGpuFilterChosenListener(GPUFilter filter) {
                        imageButton.setSelected(true);

                        gpuBlendScreenFilter = filter;

                        addFilters();
                        imageButton.setSelected(true);
                    }
                });


                break;
            case R.id.imageButton19://添加音乐

                UIUtils.showMusicDialog(this, new UIUtils.OnMusicChosenListener() {
                    @Override
                    public void onMusicChosenListener(String path) {
                        musicPath = path;
                        imageButton.setSelected(true);
                    }
                });
                break;
            case R.id.imageButton20://切换美颜
                boolean isSelected = imageButton.isSelected();
                imageButton.setSelected(!isSelected);

                if(gpuBeautyFilter== null){
                    gpuBeautyFilter = new GPUBeautyFilter();
                }else {
                    gpuBeautyFilter.setNeedRealse(true);
                    gpuBeautyFilter = null;
                }
                addFilters();
                break;
            case R.id.imageButton21://切换摄像头前后
                if(facingType == Camera.CameraInfo.CAMERA_FACING_BACK){
                    facingType = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }else {
                    facingType = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                CameraManager.getManager().onPause();
                CameraManager.getManager().releaseCamera();

                mCamera = CameraManager.getManager().openCamera( facingType);
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                preViewSize = CameraManager.getClosestSupportedSize(sizes,1280,720);
                if(parameters.getSupportedFocusModes().contains(FOCUS_MODE_CONTINUOUS_VIDEO)){
                    parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                parameters.setPreviewSize(preViewSize.width,preViewSize.height);

                parameters.setPreviewFrameRate(25);
                parameters.setRecordingHint(true);

                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);


                CameraManager.getManager().setFilter(mMovieWriter);
                CameraManager.getManager().onResume();
                imageButton.setSelected(!imageButton.isSelected());
                break;
        }
    }
    public void addFilters(){
        mCamera.stopPreview();
        if(gpuGourpFilter==null) {
            gpuGourpFilter = new GPUGourpFilter();
        }
        gpuGourpFilter.removeAllFilter();

        if(gpuBeautyFilter!=null){
            gpuBeautyFilter.setFirstLayer(gpuGourpFilter.getFilterCount() == 0);
            if(gpuCommonFilter == null&&gpuBlendScreenFilter==null) {
                gpuGourpFilter.addFilter(gpuBeautyFilter);
            }
        }
        if(gpuCommonFilter!=null){
            gpuCommonFilter.setFirstLayer(gpuGourpFilter.getFilterCount() == 0);
            gpuGourpFilter.addFilter(gpuCommonFilter);
        }
        if(gpuBlendScreenFilter!=null) {
            gpuBlendScreenFilter.setFirstLayer(gpuGourpFilter.getFilterCount() == 0);
            gpuGourpFilter.addFilter(gpuBlendScreenFilter);
        }
        if(mMovieWriter!=null){
            mMovieWriter.setFirstLayer(gpuGourpFilter.getFilterCount() == 0);
            gpuGourpFilter.addFilter(mMovieWriter);
        }
        gpuGourpFilter.filtersChanged(GLRender.mViewWidth,GLRender.mViewHeight);
        CameraManager.getManager().setFilter(gpuGourpFilter);

        mCamera.startPreview();
    }
    @Override
    protected void onResume() {
        super.onResume();
        CameraManager.getManager().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CameraManager.getManager().onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraManager.getManager().onDestory();
    }

}
