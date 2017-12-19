package tech.qt.com.meishivideoeditsdk.camera.filter.twoInput;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImageScreenBlendFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import tech.qt.com.meishivideoeditsdk.camera.OpenGLUtils;
import tech.qt.com.meishivideoeditsdk.camera.filter.GPUFilter;

/**
 * Created by chenchao on 2017/12/8.
 */

public class GPUTowInputFilter extends GPUFilter {


    private Bitmap mBitmap;
    private int mTexMatrix2Loc = -1;

    @Override
    protected String getVertexShader(){
        String vts
                = "uniform mat4 uMVPMatrix;\n"
                + "uniform mat4 uTexMatrix;\n"
                + "uniform mat4 uTexMatrix2;\n"
                + "attribute highp vec4 aPosition;\n"
                + "attribute highp vec4 aTextureCoord;\n"

                + "varying highp vec2 vTextureCoord;\n"
                + "varying highp vec2 vTextureCoord2;\n"
                + "\n"
                + "void main() {\n"
                + "	gl_Position = uMVPMatrix * aPosition;\n"
                + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
                + "	vTextureCoord2 =  (uTexMatrix2*uTexMatrix2*aTextureCoord).xy;\n"
                + "}\n";
        return vts;
    }
    protected float[] mTexMatrix2 = new float[16];
    private int mTexture2Loc = -1;
    private int mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE;

    @Override
    public void init() {
        super.init();
        OpenGLUtils.checkGlError("a1");
        mTexMatrix2Loc = GLES20.glGetUniformLocation(getProgram(),"uTexMatrix2");
        mTexture2Loc = GLES20.glGetUniformLocation(getProgram(), "sTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        OpenGLUtils.checkGlError("a2");
        Matrix.setIdentityM(mTexMatrix2,0);

    }

    @Override
    protected void onDrawForeround() {
        OpenGLUtils.checkGlError("a3");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        if(mFilterSourceTexture2 == OpenGlUtils.NO_TEXTURE){
            mFilterSourceTexture2=OpenGlUtils.generateTexture();
        }
        OpenGLUtils.checkGlError("a4");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        OpenGLUtils.checkGlError("a5");
        GLES20.glUniform1i(mTexture2Loc, 3);
        GLES20.glUniformMatrix4fv(mTexMatrix2Loc,1,false,mTexMatrix2,0);
        try {
            if (bitmaps != null) {
                numFrames++;
                if (numFrames % 4 == 0) {
                    bitMapIndex++;
                    if (bitMapIndex > bitmaps.size() - 1) {
                        bitMapIndex = 0;
                    }
                    Bitmap bitmap = bitmaps.get(bitMapIndex);

                    if(!bitmap.isRecycled()) {
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
                    }
                }


            }else {
                if(mBitmap!=null){
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
                }
            }
        }catch (Exception e){
            Log.e("TowInput",Log.getStackTraceString(e));
        }
        OpenGLUtils.checkGlError("a6");

    }
    public void setBitmap(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        mBitmap = bitmap;
        if (mBitmap == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (mFilterSourceTexture2 == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

                    mFilterSourceTexture2 = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    public int numFrames=-1;
    public int bitMapIndex=-1;
    public boolean blockOverLay;
    public ArrayList<Bitmap> bitmaps;
}
