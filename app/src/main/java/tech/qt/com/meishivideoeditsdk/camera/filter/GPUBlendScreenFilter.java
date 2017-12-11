package tech.qt.com.meishivideoeditsdk.camera.filter;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUBlendScreenFilter extends GPUTowInputFilter {
    public static final String SCREEN_BLEND_FRAGMENT_SHADER =
            " varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     mediump vec4 whiteColor = vec4(1.0);\n" +
            "     gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));\n" +
            " }";

    public GPUBlendScreenFilter() {
        super(SCREEN_BLEND_FRAGMENT_SHADER);
    }
}
