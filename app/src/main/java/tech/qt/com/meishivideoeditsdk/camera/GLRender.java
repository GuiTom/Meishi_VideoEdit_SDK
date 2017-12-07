package tech.qt.com.meishivideoeditsdk.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by chenchao on 2017/12/5.
 */

public class GLRender implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener{
    public SurfaceTexture mSurfaceTexture;
    private int mCameraTextureId;
    private GLSurfaceView glSurfaceView;
    private Camera mCamera;


    private static final String vts
            = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uTexMatrix;\n"
            + "attribute highp vec4 aPosition;\n"
            + "attribute highp vec4 aTextureCoord;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "\n"
            + "void main() {\n"
            + "	gl_Position = uMVPMatrix * aPosition;\n"
            + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
            + "}\n";
    private static final String fgs
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
            + "}";
    private static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
    private static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

    private static final int FLOAT_SZ = Float.SIZE / 8;
    private static final int VERTEX_NUM = 4;
    private static final int VERTEX_SZ = 4 * 2;

    private final float[] mMvpMatrix = new float[16];
    private final float[] mTexMatrix = new float[16];
    private int mProgramId = -1;


    private int mMVPMatrixLoc = -1;
    private int mPositionLoc = -1;
    private int mTexMatrixLoc = -1;
    private int mTextureCoordLoc = -1;
    private int mTextureLoc = -1;
    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;

    public GLRender(Camera camera,GLSurfaceView glSurfaceView){
        mCamera = camera;
        this.glSurfaceView = glSurfaceView;
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraTextureId = OpenGLUtils.generateOES_SurfaceTexture();
        mSurfaceTexture = new SurfaceTexture(mCameraTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);

       mProgramId = OpenGLUtils.loadShader(vts,fgs);

        pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(VERTICES);
        pVertex.flip();
        pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put(TEXCOORD);
        pTexCoord.flip();
        GLES20.glUseProgram(mProgramId);
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramId,"uMVPMatrix");
        mPositionLoc = GLES20.glGetAttribLocation(mProgramId,"aPosition");
        mTexMatrixLoc = GLES20.glGetUniformLocation(mProgramId,"uTexMatrix");
        mTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId,"aTextureCoord");
        mTextureLoc = GLES20.glGetUniformLocation(mProgramId,"sTexture");

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }
    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0,0,i,i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mSurfaceTexture.updateTexImage();
        drawVideoFrame();
    }

    private void drawVideoFrame() {
        mSurfaceTexture.getTransformMatrix(mTexMatrix);
        Matrix.setIdentityM(mMvpMatrix,0);
        OpenGLUtils.checkGlError("3");
        GLES20.glUseProgram(mProgramId);
        GLES20.glVertexAttribPointer(mPositionLoc,2, GLES20.GL_FLOAT,false,VERTEX_SZ,pVertex);
        OpenGLUtils.checkGlError("6");
        GLES20.glVertexAttribPointer(mTextureCoordLoc,2,GLES20.GL_FLOAT,false,VERTEX_SZ,pTexCoord);
        OpenGLUtils.checkGlError("5");
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glEnableVertexAttribArray(mTextureCoordLoc);
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc,1,false,mMvpMatrix,0);
        OpenGLUtils.checkGlError("4");
        GLES20.glUniformMatrix4fv(mTexMatrixLoc,1,false,mTexMatrix,0);
        OpenGLUtils.checkGlError("2");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mCameraTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,VERTEX_NUM);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);
        OpenGLUtils.checkGlError("1");
        GLES20.glUseProgram(0);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        glSurfaceView.requestRender();
    }
}
