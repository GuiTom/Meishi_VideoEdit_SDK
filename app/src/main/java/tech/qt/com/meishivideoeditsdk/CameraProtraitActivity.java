package tech.qt.com.meishivideoeditsdk;

import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageMovieWriter;
import utils.CameraHelper;
import utils.GPUImageFilterTools;
import utils.GPUImageFilterTools.FilterAdjuster;



public class CameraProtraitActivity extends AppCompatActivity {

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
        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader();
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
                            videoOutPutPath = Environment.getExternalStorageDirectory() + "/outPut.mp4";
                            mMovieWriter.startRecording(videoOutPutPath, videoWidth, videoHeight,videoDegree);
                        }else if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Paused) {
                           mMovieWriter.resumeRecording();
                        }

                    }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){//抬起
                        if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Capturing) {
                            mMovieWriter.pauseRecording();
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
    private class CameraLoader {

        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Camera.Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    CameraProtraitActivity.this, mCurrentCameraId);
            CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }

}
