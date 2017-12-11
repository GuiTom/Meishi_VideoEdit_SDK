package tech.qt.com.meishivideoeditsdk;

import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageBeautyFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageScreenBlendFilter;
import tech.qt.com.meishivideoeditsdk.camera.CameraManager;
import tech.qt.com.meishivideoeditsdk.camera.CameraWraper;
import tech.qt.com.meishivideoeditsdk.camera.filter.MovieWriter;
import utils.FileUtils;
import utils.GPUImageFilterTools;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
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
    private void setUpUIComponentIds() {

        glSurfaceView = (GLSurfaceView)findViewById(R.id.surfaceView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar3);
    }

    private void openCamera() {

        mCamera = CameraManager.getManager().openCamera( Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = CameraManager.getClosestSupportedSize(sizes,1280,720);
        if(parameters.getSupportedFocusModes().contains(FOCUS_MODE_CONTINUOUS_VIDEO)){
            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.setPreviewSize(size.width,size.height);

        parameters.setPreviewFrameRate(25);
        parameters.setRecordingHint(true);

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);

        CameraManager.getManager().setGlSurfaceView(glSurfaceView);
        mMovieWriter = new MovieWriter(getApplicationContext());
        mMovieWriter.maxDuration = 100;
        CameraManager.getManager().setFilter(mMovieWriter);
        mMovieWriter.recordCallBack = new MovieWriter.RecordCallBack() {
            @Override
            public void onRecordProgress(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int _progress =(int)(progress * progressBar.getMax());
                        progressBar.setProgress(_progress);
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
        ImageButton imageButton = (ImageButton)view;
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
                    mMovieWriter.startRecording(videoWidth,videoHeight,videoDegree,null);
                }else {
                    mMovieWriter.stopRecording();
                }
                break;
            case R.id.imageButton15://删除

                break;
            case R.id.imageButton16://结束录制
                    finishRecording();
                break;

            case R.id.imageButton17://添加滤镜

                break;
            case R.id.imageButton18://添加特效

                break;
            case R.id.imageButton19://添加音乐

                break;
            case R.id.imageButton20://切换美颜
                GPUImageFilterTools.showCoverDialog();
                break;
            case R.id.imageButton21://切换摄像头前后

                break;
        }
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

}
