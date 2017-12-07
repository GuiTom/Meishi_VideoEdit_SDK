package tech.qt.com.meishivideoeditsdk;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tech.qt.com.meishivideoeditsdk.camera.CameraManager;

public class CameraProtraitActivity2 extends AppCompatActivity {

    private Camera mCamera;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_protrait2);
        setUpUIComponentIds();
        openCamera();
    }

    private void setUpUIComponentIds() {
        glSurfaceView = (GLSurfaceView)findViewById(R.id.surfaceView);
    }

    private void openCamera() {

        mCamera = CameraManager.getManager().openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(720,1080);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraManager.getManager().setGlSurfaceView(glSurfaceView);
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
