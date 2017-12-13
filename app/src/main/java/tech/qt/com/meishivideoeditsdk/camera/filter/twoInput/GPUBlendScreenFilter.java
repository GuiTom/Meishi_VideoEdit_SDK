package tech.qt.com.meishivideoeditsdk.camera.filter.twoInput;

import android.opengl.Matrix;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUBlendScreenFilter extends GPUTowInputFilter {


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
                + "	vTextureCoord2 =  (uTexMatrix*uTexMatrix*aTextureCoord).xy;\n"
                + "}\n";
        return vts;
    }
    protected String getFragmentShader(){
        String fgs//绘制视频层
        = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform "+samplerTypeValue+" sTexture;\n"
                + "varying highp vec2 vTextureCoord;\n"

                + "uniform sampler2D sTexture2;\n"
                + "varying highp vec2 vTextureCoord2;\n"

                + "void main() {\n"
                + "mediump vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
                + "mediump vec4 textureColor2 = texture2D(sTexture2, vTextureCoord2);\n"
                +  "mediump vec4 whiteColor = vec4(1.0);\n"
                +  "gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));\n"
                + "}";
        return fgs;
    }

    @Override
    public void init(){
        super.init();
//        Matrix.rotateM(mTexMatrix2,0,180,0.0f,0.0f,1.0f);
//         Matrix.setRotateM(mTexMatrix2,0,180,0.0f,0.0f,1.0f);
    }


}
