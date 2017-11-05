package utils;

import android.app.Activity;
import android.hardware.Camera;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import tech.qt.com.meishivideoeditsdk.CameraProtraitActivity;
import utils.CameraHelper;

public class CameraLoader {
    private final GPUImage mGPUImage;
    private final CameraHelper mCameraHelper;
    private final Activity fatherActivity;

    public CameraLoader(CameraHelper helper, GPUImage gpuImage, Activity activity){
        this.mCameraHelper=helper;
        this.mGPUImage=gpuImage;
        this.fatherActivity=activity;
    }

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
                fatherActivity, mCurrentCameraId);
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