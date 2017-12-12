package tech.qt.com.meishivideoeditsdk.camera.filter;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUBlendScreenFilter extends GPUTowInputFilter {



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




}
