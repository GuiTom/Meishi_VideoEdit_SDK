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

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageBeautyFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageScreenBlendFilter;
import tech.qt.com.meishivideoeditsdk.camera.CameraManager;
import tech.qt.com.meishivideoeditsdk.camera.CameraWraper;
import tech.qt.com.meishivideoeditsdk.camera.filter.MovieWriter;
import utils.FileUtils;

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
    }

    private void openCamera() {

        mCamera = CameraManager.getManager().openCamera( Camera.CameraInfo.CAMERA_FACING_FRONT);
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
        CameraManager.getManager().setFilter(mMovieWriter);
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.imageButton2:
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
            case R.id.imageButton3:

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

    @Override
    protected void onStop() {
        super.onStop();

    }
}
