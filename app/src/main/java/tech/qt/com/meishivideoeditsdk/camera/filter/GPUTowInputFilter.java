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
    private static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate2 = inputTextureCoordinate2.xy;\n" +
            "}";
    protected static final String vtss
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

    private int mFilterSecondTextureCoordinateAttribute;
    private int mFilterInputTextureUniform2;
    private int mFilterSourceTexture2;
    private FloatBuffer mTexture2CoordinatesBuffer;
    public GPUTowInputFilter(String fragShader){
        super(VERTEX_SHADER,fragShader);
    }
    @Override
    public void init() {
        super.init();

        mTexture2CoordinatesBuffer = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexture2CoordinatesBuffer.put(TEXCOORD);
        mTexture2CoordinatesBuffer.flip();

        mFilterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinateAttribute);

    }

    @Override
    protected void onDrawForeround() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        if(mFilterSourceTexture2== OpenGlUtils.NO_TEXTURE){
            mFilterSourceTexture2=OpenGlUtils.generateTexture();
        }
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinateAttribute);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        GLES20.glUniform1i(mFilterInputTextureUniform2, 3);
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
        mTexture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(mFilterSecondTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, mTexture2CoordinatesBuffer);

    }
    public static int numFrames=-1;
    public static int bitMapIndex=-1;
    public static boolean blockOverLay;
    public ArrayList<Bitmap> bitmaps;
}
