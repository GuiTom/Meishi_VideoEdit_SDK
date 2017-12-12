package tech.qt.com.meishivideoeditsdk.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by chenchao on 2017/12/7.
 */

public class CameraWraper {
    public Camera camera;
    public boolean isPreViewing;

    public SurfaceTexture getmSufaceTexTure() {
        return mSufaceTexTure;
    }

    private SurfaceTexture mSufaceTexTure;

    public CameraWraper(){

    }
    public static CameraWraper open(int cameraId){
        CameraWraper cameraWraper;
        cameraWraper = new CameraWraper();
        cameraWraper.camera = Camera.open(cameraId);
        return cameraWraper;
    }
    public void startPreview(){
        camera.startPreview();
        isPreViewing = true;
    }
    public void stopPreview(){
        camera.stopPreview();
        isPreViewing = false;
    }
    public void setPreviewTexture(SurfaceTexture st){
        mSufaceTexTure = st;
        try {
            camera.setPreviewTexture(st);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
    public void setDisplayOrientation(int degree){
        camera.setDisplayOrientation(degree);
    }
    public Camera.Parameters getParameters(){
        return camera.getParameters();
    }
    public void setParameters(Camera.Parameters parameters){
        camera.setParameters(parameters);
    }
    public void release(){
        camera.release();
    }
}
