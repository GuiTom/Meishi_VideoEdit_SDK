package tech.qt.com.meishivideoeditsdk;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;

import tech.qt.com.meishivideoeditsdk.camera.CameraManager;
import tech.qt.com.meishivideoeditsdk.camera.CameraWraper;

public class CameraProtraitActivity2 extends AppCompatActivity {

    private CameraWraper mCamera;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_protrait2);
        setUpUIComponentIds();
        openCamera();
        CameraManager.getManager().setGlSurfaceView(glSurfaceView);
    }

    private void setUpUIComponentIds() {
        glSurfaceView = (GLSurfaceView)findViewById(R.id.surfaceView);
    }

    private void openCamera() {

        mCamera = CameraManager.getManager().openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(1080,720);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(params);
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
