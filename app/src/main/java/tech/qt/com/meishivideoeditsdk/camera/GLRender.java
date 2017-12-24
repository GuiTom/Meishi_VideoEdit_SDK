package tech.qt.com.meishivideoeditsdk.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tech.qt.com.meishivideoeditsdk.camera.filter.GPUFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUGourpFilter;

/**
 * Created by chenchao on 2017/12/5.
 */

public class GLRender implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener{
    public SurfaceTexture mSurfaceTexture;
    private int mCameraTextureId;
    private GLSurfaceView glSurfaceView;


    public void setmCamera(final CameraWraper mCamera) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                }
            });

           this.mCamera = mCamera;
    }

    private CameraWraper mCamera;
    private GPUFilter mFilter;
    public static int mViewWidth;
    public static int mViewHeight;
    private LinkedList<Runnable> mRunOnDraw;
    public GLRender(){

    }

    public GLRender(CameraWraper camera,GLSurfaceView glSurfaceView){
        mCamera = camera;
        this.glSurfaceView = glSurfaceView;
        mRunOnDraw = new LinkedList<>();
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraTextureId = OpenGLUtils.generateOES_SurfaceTexture();
        if(mSurfaceTexture ==null) {
            mSurfaceTexture = new SurfaceTexture(mCameraTextureId);
        }
        mSurfaceTexture.setOnFrameAvailableListener(this);


        if(mFilter!=null){
            mFilter.init();
        }
        mCamera.setPreviewTexture(mSurfaceTexture);

        mCamera.startPreview();
        Log.e("onSurfaceCreated","1");
    }
    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        mViewWidth = i;
        mViewHeight  = i1;
        GLES20.glViewport(0,0,i,i1);
    }
    public void release(){
        if(((GPUGourpFilter)mFilter).mfilters.size()>0){
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    mFilter.setNeedRealse(true);
                    mFilter = null;
                }
            });
        }
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if(mSurfaceTexture!=null){
                    mSurfaceTexture.setOnFrameAvailableListener(null);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mSurfaceTexture.releaseTexImage();
                    }

                    mSurfaceTexture = null;
                }
            }
        });


    }
    @Override
    public void onDrawFrame(GL10 gl10) {
        mSurfaceTexture.updateTexImage();
        drawVideoFrame();
    }

    private void drawVideoFrame() {
        if(mFilter!=null) {
            runPendingOnDrawTasks();
            mFilter.onDrawFrame(mCameraTextureId, mSurfaceTexture,mViewWidth,mViewHeight);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("request Render","1");
        glSurfaceView.requestRender();
    }

    public void setmFilter(final GPUFilter mFilter) {
        this.mFilter = mFilter;
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mFilter.init();
            }
        });
    }
    public void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
    public void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }
}
