package tech.qt.com.meishivideoeditsdk.camera.filter;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUBlendScreenFilter extends GPUTowInputFilter {
//    public static final String SCREEN_BLEND_FRAGMENT_SHADER =
//            " varying highp vec2 textureCoordinate;\n" +
//            " varying highp vec2 textureCoordinate2;\n" +
//            "\n" +
//            " uniform sampler2D inputImageTexture;\n" +
//            " uniform sampler2D inputImageTexture2;\n" +
//            " \n" +
//            " void main()\n" +
//            " {\n" +
//            "     mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
//            "     mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
//            "     mediump vec4 whiteColor = vec4(1.0);\n" +
//            "     gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));\n" +
//            " }";
    protected static final String fgsExt_OES
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"

            + "uniform sampler2D sTexture2;\n"
            + "varying highp vec2 vTextureCoord2;\n"

            + "void main() {\n"

            + "mediump vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
            + "mediump vec4 textureColor2 = texture2D(sTexture2, vTextureCoord2);\n"
            +  "mediump vec4 whiteColor = vec4(1.0);\n"
            +  "gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));\n"
            + "}";
    protected static final String fgs2D =
            "precision mediump float;\n"

            + "uniform sampler2D sTexture;\n"
            + "varying highp vec2 vTextureCoord;\n"

            + "uniform sampler2D sTexture2;\n"
            + "varying highp vec2 vTextureCoord2;\n"

            + "void main() {\n"

            + "mediump vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
            + "mediump vec4 textureColor2 = texture2D(sTexture2, vTextureCoord2);\n"
            +  "mediump vec4 whiteColor = vec4(1.0);\n"
            +  "gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));\n"
            + "}";

    public GPUBlendScreenFilter() {
        super(fgsExt_OES);
    }

    @Override
    public void setFirstLayer(boolean isFirstLayer){
        if(isFirstLayer == false){
                this.mFragmentShader = fgs2D;
        }else {
                this.mFragmentShader = fgsExt_OES;
        }

    }

}
