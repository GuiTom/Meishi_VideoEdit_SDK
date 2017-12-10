package tech.qt.com.meishivideoeditsdk.camera;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUFilter;

/**
 * Created by chenchao on 2017/12/6.
 */

public class CameraManager {
    private CameraWraper mCamera;
    private int mCameraId = -1;
    private GLSurfaceView glSurfaceView;
    private static CameraManager manager;
//    private boolean preViewRuning;

    private GLRender mRender;

    public static CameraManager getManager(){
        if(manager==null){
            manager = new CameraManager();
        }
        return manager;
    }
    public void CameraManger(){

    }
    public CameraWraper openCamera(int facingTpe){
        int cameraCount = Camera.getNumberOfCameras();
        for(int i=0;i<cameraCount;i++){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i,cameraInfo);
            if(cameraInfo.facing == facingTpe){
                mCamera = CameraWraper.open(i);
                mCameraId = i;
                return mCamera;
            }
        }
        return null;
    }

    public void setGlSurfaceView(GLSurfaceView glSurfaceView) {

        this.glSurfaceView = glSurfaceView;
        this.glSurfaceView.setEGLContextClientVersion(2);
        mRender = new GLRender(mCamera,glSurfaceView);
        this.glSurfaceView.setRenderer(mRender);
    }

    public void onPause(){
        mCamera.stopPreview();
//        glSurfaceView.onPause();
    }
    public void onResume(){
        mCamera.startPreview();
//        glSurfaceView.onResume();

    }
    public void setFilter(GPUFilter filter){
        mRender.setmFilter(filter);
    }
    public void onDestory(){
        mCamera.release();
        mCamera = null;
        glSurfaceView.setRenderer(null);
    }
}
