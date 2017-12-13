package tech.qt.com.meishivideoeditsdk.camera.filter.twoInput;

/**
 * Created by chenchao on 2017/12/13.
 */

public class GPUGrayScaleFilter extends GPUTowInputFilter {

    protected String getFragmentShader(){
        String fgs//绘制视频层
                ="#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"+
                "precision highp float;\n" +
                "\n" +
                "varying vec2 vTextureCoord;\n" +
                "\n" +
                "uniform "+samplerTypeValue+" sTexture;\n" +
                "\n" +
                "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "  lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                "  float luminance = dot(textureColor.rgb, W);\n" +
                "\n" +
                "  gl_FragColor = vec4(vec3(luminance), textureColor.a);\n" +
                "}";

        return fgs;
    }

}
