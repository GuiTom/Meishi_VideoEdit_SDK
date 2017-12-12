package tech.qt.com.meishivideoeditsdk.camera.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImageScreenBlendFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * Created by chenchao on 2017/12/8.
 */

public class GPUTowInputFilter extends GPUFilter {


    @Override
    protected String getVertexShader(){
        String vts
                = "uniform mat4 uMVPMatrix;\n"
                + "uniform mat4 uTexMatrix;\n"

                + "attribute highp vec4 aPosition;\n"
                + "attribute highp vec4 aTextureCoord;\n"

                + "varying highp vec2 vTextureCoord;\n"
                + "varying highp vec2 vTextureCoord2;\n"
                + "\n"
                + "void main() {\n"
                + "	gl_Position = uMVPMatrix * aPosition;\n"
                + "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
                + "	vTextureCoord2 = aTextureCoord.xy;\n"
                + "}\n";
        return vts;
    }

    private int mTexture2Loc = -1;
    private int mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE;

    @Override
    public void init() {
        super.init();
        OpenGlUtils.checkGlError("a1");
        mTexture2Loc = GLES20.glGetUniformLocation(getProgram(), "sTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        OpenGlUtils.checkGlError("a2");

    }

    @Override
    protected void onDrawForeround() {
        OpenGlUtils.checkGlError("a3");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        if(mFilterSourceTexture2 == OpenGlUtils.NO_TEXTURE){
            mFilterSourceTexture2=OpenGlUtils.generateTexture();
        }
        OpenGlUtils.checkGlError("a4");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        OpenGlUtils.checkGlError("a5");
        GLES20.glUniform1i(mTexture2Loc, 3);

        try {
            if (bitmaps != null) {
                numFrames++;
                if (numFrames % 2 == 0) {
                    bitMapIndex++;
                }
                if (bitMapIndex > bitmaps.size() - 1) {
                    bitMapIndex = 0;
                }
                Bitmap bitmap = bitmaps.get(bitMapIndex);

                if(!bitmap.isRecycled()) {
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
                }
            }
        }catch (Exception e){
            Log.e("TowInput",Log.getStackTraceString(e));
        }
        OpenGlUtils.checkGlError("a6");

    }

    public static int numFrames=-1;
    public static int bitMapIndex=-1;
    public static boolean blockOverLay;
    public  static ArrayList<Bitmap> bitmaps;
}
