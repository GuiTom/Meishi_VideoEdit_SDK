package tech.qt.com.meishivideoeditsdk.camera;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by chenchao on 2017/12/6.
 */

public class CameraManager {
    private Camera mCamera;
    private int mCameraId = -1;
    private GLSurfaceView glSurfaceView;
    private static CameraManager manager;
//    private boolean preViewRuning;
    private GPUImageFilter mFilter;
    public static CameraManager getManager(){
        if(manager==null){
            manager = new CameraManager();
        }
        return manager;
    }
    public Camera openCamera(int facingTpe){
        int cameraCount = Camera.getNumberOfCameras();
        for(int i=0;i<cameraCount;i++){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i,cameraInfo);
            if(cameraInfo.facing == facingTpe){
                mCamera = Camera.open(i);
                mCameraId = i;
                return mCamera;
            }
        }
        return null;
    }

    public void setGlSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.glSurfaceView.setRenderer(new GLRender(mCamera,glSurfaceView));
    }
    public void onPause(){
        mCamera.stopPreview();

        glSurfaceView.onPause();
    }
    public void onResume(){
        if(mCameraId > -1){
            mCamera.startPreview();
            glSurfaceView.onResume();
        }
    }
    public void onDestory(){
        mCamera.release();
        mCamera = null;
        glSurfaceView.setRenderer(null);
    }
}
