package tech.qt.com.meishivideoeditsdk.camera.filter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import tech.qt.com.meishivideoeditsdk.camera.OpenGLUtils;

/**
 * Created by chenchao on 2017/12/8.
 */

public class GPUFilter {
    //samplerExternalOES|sampler2D
    protected String samplerTypeValue ="sampler2D";
    private SamplerType mSamplerType;
    private float texTureHeight = -1;
    private float texTureWidth = -1;
    private boolean isSquare;

    public void setNeedRealse(boolean needRealse) {
        this.needRealse = needRealse;
    }

    private boolean needRealse;

    protected enum SamplerType {
        Sampler2D,SamplerExternalOES
    }
    protected void setSamplerType(SamplerType samplerType){
        if(samplerType == SamplerType.Sampler2D){
                samplerTypeValue = "sampler2D";
        }else if(samplerType == SamplerType.SamplerExternalOES){
                samplerTypeValue = "samplerExternalOES";
        }
        mSamplerType = samplerType;
    }
    protected String getVertexShader(){
        String vts
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
        return vts;
    }
    protected String getFragmentShader(){
        String fgs
                = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform "+samplerTypeValue+" sTexture;\n"
                + "varying highp vec2 vTextureCoord;\n"
                + "void main() {\n"
                + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
                + "}";
        return fgs;
    }
    protected void setShader(String vs,String fs){
        mVertexShader = vs;
        mFragmentShader = fs;
    }
    protected String mVertexShader;
    protected String mFragmentShader;

    protected float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
    protected float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

    protected final int FLOAT_SZ = Float.SIZE / 8;
    protected final int VERTEX_NUM = 4;
    protected final int VERTEX_SZ = 4 * 2;

    private final float[] mMvpMatrix = new float[16];
    private final float[] mTexMatrix = new float[16];
    private int mProgramId = -1;

//    private boolean isInintalized = false;
    private int mMVPMatrixLoc = -1;
    private int mPositionLoc = -1;
    private int mTexMatrixLoc = -1;
    private int mTextureCoordLoc = -1;
    private int mTextureLoc = -1;
    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;
    protected LinkedList<Runnable> mRunOnDraw;

    public void setFirstLayer(boolean isFirstLayer){
        setSamplerType(isFirstLayer?SamplerType.SamplerExternalOES:SamplerType.Sampler2D);
        setShader(getVertexShader(),getFragmentShader());
    }
    public void init(){
        mRunOnDraw = new LinkedList<Runnable>();
        if(mProgramId > 0){
            GLES20.glDeleteProgram(mProgramId);
            mProgramId = -1;
        }
        mProgramId = OpenGLUtils.loadShader(mVertexShader,mFragmentShader);

        pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(VERTICES);
        pVertex.flip();
        setTextureCoord(texTureWidth,texTureHeight,isSquare);
        GLES20.glUseProgram(mProgramId);
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramId,"uMVPMatrix");
        OpenGLUtils.checkGlError("a1.5");
        mPositionLoc = GLES20.glGetAttribLocation(mProgramId,"aPosition");
        OpenGLUtils.checkGlError("a1.4");
        mTexMatrixLoc = GLES20.glGetUniformLocation(mProgramId,"uTexMatrix");
        OpenGLUtils.checkGlError("a1.3");
        mTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId,"aTextureCoord");
        OpenGLUtils.checkGlError("a1.2");
        mTextureLoc = GLES20.glGetUniformLocation(mProgramId,"sTexture");
        OpenGLUtils.checkGlError("a1.1");

    }

//    public void setSquare(){
//
//        for(int i=0;i<16;i++){
//            float f = TEXCOORD[i];
//        }
//        pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        pTexCoord.put(TEXCOORD);
//        pTexCoord.flip();
//    }
    public void setTexureSize(float width,float height,boolean isSquare){
        texTureWidth = width;
        texTureHeight = height;
        this.isSquare = isSquare;

    }

    private void setTextureCoord(float width, float height, boolean isSquare) {
        if(isSquare){
            if(width>height){
                float y1 = ((width - height)/width)/2;
                float y2 = y1 + height/width;
//                { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f }

                float [] fs = { 1.0f,y2,0.0f,y2,1.0f,y1,0.0f,y1};

                pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                pTexCoord.put(fs);
                pTexCoord.flip();
            }else {
                float y1 = ((height - width)/height)/2;
                float y2 = y1 + width/height;
                float [] fs = { y2, 1.0f, y1, 1.0f, y2, 0.0f, 0.0f, y1 };
                pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                pTexCoord.put(fs);
                pTexCoord.flip();
            }
        }
    }

    public void onDrawFrame(int textureId, SurfaceTexture st, int mViewWidth, int mViewHeight){
//        if(isInintalized == false) return;
        OpenGLUtils.checkGlError("3.3");
        runPendingOnDrawTasks();
        OpenGLUtils.checkGlError("3.2");
        if(mSamplerType == SamplerType.SamplerExternalOES) {
            st.getTransformMatrix(mTexMatrix);
        }else {
            Matrix.setIdentityM(mTexMatrix,0);
        }
        OpenGLUtils.checkGlError("3.1");
        Matrix.setIdentityM(mMvpMatrix,0);
        OpenGLUtils.checkGlError("3");
        GLES20.glUseProgram(mProgramId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

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

        if(mSamplerType == SamplerType.Sampler2D) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }else if(mSamplerType == SamplerType.SamplerExternalOES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        }

        GLES20.glUniform1i(mTextureLoc, 0);
        OpenGLUtils.checkGlError("1.1");
        onDrawForeround();
        OpenGLUtils.checkGlError("1.2");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,VERTEX_NUM);
        OpenGLUtils.checkGlError("1.3");
        if(mSamplerType == SamplerType.Sampler2D) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }else if(mSamplerType == SamplerType.SamplerExternalOES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }
        OpenGLUtils.checkGlError("1");
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTextureCoordLoc);
        GLES20.glUseProgram(0);
         if(needRealse){
             release();
         }
    }
    protected void onDrawForeround() {}


    public int getProgram() {
        return mProgramId;
    }
    public void runPendingOnDrawTasks() {
        OpenGLUtils.checkGlError("3.5");
        while (mRunOnDraw!=null&&!mRunOnDraw.isEmpty()) {
            Runnable runnable = mRunOnDraw.removeFirst();
            runnable.run();
//            if(OpenGLUtils.checkGlError("3.4")!=0){
//                runnable.run();
//            }
        }
    }
    protected void release(){
        if(mProgramId > 0){
            GLES20.glDeleteProgram(mProgramId);
            mProgramId = -1;
        }

    }
    protected void runOnDraw(final Runnable runnable) {
        if(mRunOnDraw == null){
            mRunOnDraw = new LinkedList<Runnable>();
        }
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
                OpenGLUtils.checkGlError("setFloat");
                GLES20.glUniform1f(location, floatValue);
                OpenGLUtils.checkGlError("setFloat2");
            }
        });
    }
    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

}
