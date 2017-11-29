package jp.co.cyberagent.android.gpuimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.opengl.EGL14;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.FloatBuffer;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import jp.co.cyberagent.android.gpuimage.encoder.EglCore;
import jp.co.cyberagent.android.gpuimage.encoder.MediaAudioEncoder;
import jp.co.cyberagent.android.gpuimage.encoder.MediaEncoder;
import jp.co.cyberagent.android.gpuimage.encoder.MediaMuxerWrapper;
import jp.co.cyberagent.android.gpuimage.encoder.MediaVideoEncoder;
import jp.co.cyberagent.android.gpuimage.encoder.WindowSurface;
import transcoder.VideoTranscoder;

import static android.content.Context.MODE_PRIVATE;

@TargetApi(18)
public class GPUImageMovieWriter extends GPUImageFilter {
    private final Context mContext;
    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;
    private WindowSurface mCodecInput;
    public String outputVideoFile;
    private ArrayList<Uri> uriList;

    private ArrayList<Long>times;
    private ArrayList<Long>audioPts;
    private int videoFileIndex = -1;
    private String tmpVideoFilePath;
    private long lastAudioPts;

    public interface RecordCallBack{
        void onRecordProgress(float progress);
        void onRecordTimeEnd();
        void onRecordFinish(String filePath);

    }
    public GPUImageMovieWriter(Context context){
        mContext=context;
    }
    public RecordCallBack recordCallBack;
    public enum RecordStatus {
        Stoped,Paused,Capturing
    }
    public RecordStatus recordStatus=RecordStatus.Stoped;
    private EGLSurface mEGLScreenSurface;
    private EGL10 mEGL;
    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EglCore mEGLCore;
    public int maxDuration=10;

    private long currentMillis=0;
    private Timer timer;
    @Override
    public void onInit() {
        super.onInit();
        mEGL = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEGL.eglGetCurrentDisplay();
        mEGLContext = mEGL.eglGetCurrentContext();
        mEGLScreenSurface = mEGL.eglGetCurrentSurface(EGL10.EGL_DRAW);
        uriList = new ArrayList<Uri>();
        times = new ArrayList<Long>();
        audioPts = new ArrayList<Long>();
    }

    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        // Draw on screen surface
        super.onDraw(textureId, cubeBuffer, textureBuffer);

        if (recordStatus==RecordStatus.Capturing) {

            // create encoder surface
            if (mCodecInput == null) {
                mEGLCore = new EglCore(EGL14.eglGetCurrentContext(), EglCore.FLAG_RECORDABLE);
                mCodecInput = new WindowSurface(mEGLCore, mVideoEncoder.getSurface(), false);
            }
            // Draw on encoder surface
            mCodecInput.makeCurrent();
            super.onDraw(textureId, cubeBuffer, textureBuffer);
            if(mCodecInput!=null) {
                mCodecInput.swapBuffers();
                mVideoEncoder.frameAvailableSoon();
            }
            checkEglError("123442==>");
        }

        // Make screen surface be current surface
        mEGL.eglMakeCurrent(mEGLDisplay, mEGLScreenSurface, mEGLScreenSurface, mEGLContext);
        checkEglError("12345==>");

    }
    private void checkEglError(String msg) {
        int error;
        if ((error = mEGL.eglGetError()) != EGL14.EGL_SUCCESS) {
            mEGLDisplay = mEGL.eglGetCurrentDisplay();
            mEGLContext = mEGL.eglGetCurrentContext();
            mEGLScreenSurface = mEGL.eglGetCurrentSurface(EGL10.EGL_DRAW);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //releaseEncodeSurface();
    }

    public void startRecording(final int width, final int height, final int degree, final String musicPath) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (recordStatus!=RecordStatus.Stoped) {
                    return;
                }
                recordStatus = RecordStatus.Capturing;
                videoFileIndex ++;
                times.add(new Long(currentMillis));
                audioPts.add(new Long(lastAudioPts));
                File dic = new File(Environment.getExternalStorageDirectory(),"tmpVideo");
                if(!dic.exists()){
//                    dic.delete();
                    dic.mkdir();
                }

                tmpVideoFilePath = dic.toString()+"/"+videoFileIndex+".mp4";
                File file = new File(tmpVideoFilePath);
                if(file.exists()){
                    file.delete();
                }
//                File dir= mContext.getDir("tmpVideo", MODE_PRIVATE);
//                File file = new File(dir,videoFileIndex + ".mp4");
//                tmpVideoFilePath = file.getAbsolutePath();

                try {
                    mMuxer = new MediaMuxerWrapper(tmpVideoFilePath,degree);

                    // for video capturing
                    mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, width, height);
                    // for audio capturing
                    mAudioEncoder = new MediaAudioEncoder(mMuxer,musicPath, mMediaEncoderListener);

                    mAudioEncoder.startPts = lastAudioPts;
                    mMuxer.prepare();
                    mMuxer.startRecording();
                    if(timer==null){
                        timer=new Timer(true);
                    }
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(recordStatus==RecordStatus.Capturing) {
                                currentMillis += 200;
                                if (currentMillis >= maxDuration * 1000) {
                                    if (recordCallBack != null) {
                                        recordCallBack.onRecordTimeEnd();
                                    }
                                } else {
                                    if (recordCallBack != null) {
                                        recordCallBack.onRecordProgress((float) currentMillis / (maxDuration * 1000));
                                    }
                                }
                            }

                        }
                    },0,200);

                } catch (IOException e) {
                    recordStatus = RecordStatus.Stoped;
//                    e.printStackTrace();
                    throw new Error(e);

                }
            }
        });
    }
    protected void resumeRecording() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (recordStatus!=RecordStatus.Paused) {
                    return;
                }

                mMuxer.resumeRecording();
                recordStatus=RecordStatus.Capturing;

            }
        });
    }
    protected void pauseRecording() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (recordStatus!=RecordStatus.Capturing) {
                    return;
                }

                mMuxer.pauseRecording();
                recordStatus=RecordStatus.Paused;


            }
        });
    }

    public void stopRecording() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (recordStatus==RecordStatus.Stoped) {
                    return;
                }

                timer.cancel();
                timer=null;
                lastAudioPts = mMuxer.getSampleTime();

                mMuxer.stopRecording();
                recordStatus=RecordStatus.Stoped;
                releaseEncodeSurface();
                uriList.add(Uri.parse(tmpVideoFilePath));
            }
        });
    }
    public void fallBack() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if(videoFileIndex>-1) {
                    videoFileIndex--;
                }
                if(times.size()>0) {
                    Long time = times.remove(times.size() - 1);
                    currentMillis = time.longValue();
                    if (recordCallBack != null) {
                        recordCallBack.onRecordProgress((float) currentMillis / (maxDuration * 1000));
                    }
                }
                if(audioPts.size()>0){
                    Long pts = audioPts.remove(audioPts.size()-1);
                    lastAudioPts = pts.longValue();
                }
                if(uriList.size()>0) {
                   Uri uri = uriList.remove(uriList.size() - 1);
                   File file = new File(uri.getPath());
                   if(file.exists()){
                       file.delete();
                   }
                }
            }
        });
    }
    public void finishRecording() {
        currentMillis=0;
        videoFileIndex=-1;
        lastAudioPts = 0;
        times = new ArrayList<Long>();
        audioPts = new ArrayList<Long>();
        File file = new File(outputVideoFile);
        if(file.exists()){
            file.delete();
        }
        VideoTranscoder.getInstance().transcodeVideo(mContext, uriList, outputVideoFile,
                null, null, true, new VideoTranscoder.Listener() {
                    @Override
                    public void onTranscodeProgress(double v) {

                    }

                    @Override
                    public void onTranscodeCompleted() {

//                        for(Uri uri:uriList){
//                            File file = new File(uri.getPath());
//                            if(file.exists()){
//                                file.delete();
//                            }
//                        }
                        uriList = new ArrayList<Uri>();
                        if(recordCallBack!=null){
                            recordCallBack.onRecordFinish(outputVideoFile);
                        }
                    }

                    @Override
                    public void onTranscodeCanceled() {

                    }

                    @Override
                    public void onTranscodeFailed(Exception e) {

                    }
                });
    }
    private void releaseEncodeSurface() {
        if (mEGLCore != null) {
            mEGLCore.makeNothingCurrent();
            mEGLCore.release();
            mEGLCore = null;
        }

        if (mCodecInput != null) {
            mCodecInput.release();
            mCodecInput = null;
        }

    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
        }

        @Override
        public void onMuxerStopped() {
        }
    };
}
