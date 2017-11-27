package tech.qt.com.meishivideoeditsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageMovieWriter;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
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
    public File musicFile;
    public String musicPath;
    private GPUImageFilterGroup filters;
    private GPUImageOverlayBlendFilter gpuImageOverlayBlendFilter;
    private Switch switchButton;

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

        mMovieWriter = new GPUImageMovieWriter(getApplicationContext());
        mMovieWriter.maxDuration=100;//多少秒
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
            public void onRecordTimeEnd() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"已达到最大视频时长",Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onRecordFinish(String filePath) {
                Intent intent=new Intent(getApplicationContext(),VideoPlayerActivity.class);
                intent.putExtra("videoPath",videoOutPutPath);
                startActivity(intent);
            }

            @Override
            public void onRecordFinish() {

            }

        };
        final File dir = CameraProtraitActivity.this.getFilesDir();
        dir.mkdirs();
        musicFile = new File(dir, "sample.mp3");
        try {
            prepareSampleMovie(musicFile);//将Raw 下面的视频文件复制到当前路径
        } catch (IOException e) {
            Log.e("123",Log.getStackTraceString(e));
        }

        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader(mCameraHelper,mGPUImage,this);
    }
    private final void prepareSampleMovie(File path) throws IOException {
        final Activity activity = this;
        if (!path.exists()) {
//            if (DEBUG) Log.i(TAG, "copy sample movie file from res/raw to app private storage");
            final BufferedInputStream in = new BufferedInputStream(activity.getResources().openRawResource(R.raw.sample));
            final BufferedOutputStream out = new BufferedOutputStream(activity.openFileOutput(path.getName(), Context.MODE_PRIVATE));
            byte[] buf = new byte[8192];
            int size = in.read(buf);
            while (size > 0) {
                out.write(buf, 0, size);
                size = in.read(buf);
            }
            in.close();
            out.flush();
            out.close();
        }
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

//                            videoOutPutPath = FileUtils.getCaptureFile(Environment.DIRECTORY_MOVIES, ".mp4").toString();
                            videoOutPutPath = Environment.getExternalStorageDirectory()+"/"+FileUtils.getDateTimeString()+".mp4";
                            mMovieWriter.startRecording(videoWidth, videoHeight,videoDegree,CameraProtraitActivity.this.musicPath);
                            mMovieWriter.outputVideoFile = videoOutPutPath;

                        }

                    }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){//抬起
                        if(mMovieWriter.recordStatus== GPUImageMovieWriter.RecordStatus.Capturing) {
                            mMovieWriter.stopRecording();
                        }
                    }
                    return false;
                }
            });
            switchButton=(Switch)findViewById(R.id.switch1);
            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        musicPath=musicFile.getAbsolutePath();

                    }else {
                        musicPath=null;
                    }
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


    private void finishRecording(){
        mMovieWriter.finishRecording();


    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button4://完成
                finishRecording();
                break;
            case R.id.button5://选择滤镜
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter,false);
                    }
                });
                break;
            case R.id.button8://选择遮罩
                GPUImageFilterTools.showCoverDialog(this, new GPUImageFilterTools.OnGpuImageCoverChosenListener() {
                    @Override
                    public void onGpuImageCoverChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter,true);
                        GPUImageOverlayBlendFilter.blockOverLay=false;
                    }
                });
                break;
            case R.id.button6://切换镜头
                mCamera.switchCamera();
                break;
        }
    }

    private void switchFilterTo(final GPUImageFilter filter,boolean isCover) {

            if(filters==null){
                filters = new GPUImageFilterGroup();
            }

            synchronized (filters) {
                if (isCover) {
                    if (gpuImageOverlayBlendFilter != null && gpuImageOverlayBlendFilter != filter) {
                        gpuImageOverlayBlendFilter.destroy();
                        filters.remoteFilter(gpuImageOverlayBlendFilter.getClass());
                    }
                    gpuImageOverlayBlendFilter = (GPUImageOverlayBlendFilter) filter;

                    filters.addFilter(filter);
                } else {
                    if (mFilter != null && mFilter != filter) {
                        mFilter.destroy();
                        filters.remoteFilter(mFilter.getClass());
                    }
                    mFilter = filter;
                    filters.addFilter(mFilter);

//                if(gpuImageOverlayBlendFilter!=null){
//                    filters.addFilter(gpuImageOverlayBlendFilter);
//                }
                }

                filters.addFilter(mMovieWriter);

                mGPUImage.setFilter(filters);
            }
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);



    }



}
