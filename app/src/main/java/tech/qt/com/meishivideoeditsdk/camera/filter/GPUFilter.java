package tech.qt.com.meishivideoeditsdk.camera.filter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import tech.qt.com.meishivideoeditsdk.camera.OpenGLUtils;

/**
 * Created by chenchao on 2017/12/8.
 */

public class GPUFilter {
    protected static final String vts
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
    protected static final String fgs//绘制视频层的
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
            + "}";

    private final String mVertexShader;
    private final String mFragmentShader;

    protected static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
    protected static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

    protected static final int FLOAT_SZ = Float.SIZE / 8;
    protected static final int VERTEX_NUM = 4;
    protected static final int VERTEX_SZ = 4 * 2;

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
    private LinkedList<Runnable> mRunOnDraw;
    public GPUFilter(){
        this(vts,fgs);
    }
    public GPUFilter(String vs,String fs){
        mVertexShader = vs;
        mFragmentShader = fs;

    }
    public void init(){
        mRunOnDraw = new LinkedList<Runnable>();
        mProgramId = OpenGLUtils.loadShader(mVertexShader,mFragmentShader);

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
    }
    public void onDrawFrame(int textureId, SurfaceTexture st, int mViewWidth, int mViewHeight){
        runPendingOnDrawTasks();
        st.getTransformMatrix(mTexMatrix);
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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        onDrawForeround();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,VERTEX_NUM);
        OpenGLUtils.checkGlError("1");
        GLES20.glUseProgram(0);
    }
    protected void onDrawForeround() {}
    public int getProgram() {
        return mProgramId;
    }
    public void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }
    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }
    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

}
