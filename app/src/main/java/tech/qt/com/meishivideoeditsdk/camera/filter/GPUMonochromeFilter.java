package tech.qt.com.meishivideoeditsdk.camera.filter;

import android.opengl.GLES20;

/**
 * Created by chenchao on 2017/12/13.
 */

public class GPUMonochromeFilter extends GPUFilter {
    protected String getFragmentShader(){
        String fgs//绘制视频层
                = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"+
                "  varying highp vec2 textureCoordinate;\n" +
                "  \n" +
                "  uniform "+samplerTypeValue+" inputImageTexture;\n" +
                "  uniform float intensity;\n" +
                "  uniform vec3 filterColor;\n" +
                "  \n" +
                "  const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
                "  \n" +
                "  void main()\n" +
                "  {\n" +
                " 	//desat, then apply overlay blend\n" +
                " 	lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                " 	float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
                " 	\n" +
                " 	lowp vec4 desat = vec4(vec3(luminance), 1.0);\n" +
                " 	\n" +
                " 	//overlay\n" +
                " 	lowp vec4 outputColor = vec4(\n" +
                "                                  (desat.r < 0.5 ? (2.0 * desat.r * filterColor.r) : (1.0 - 2.0 * (1.0 - desat.r) * (1.0 - filterColor.r))),\n" +
                "                                  (desat.g < 0.5 ? (2.0 * desat.g * filterColor.g) : (1.0 - 2.0 * (1.0 - desat.g) * (1.0 - filterColor.g))),\n" +
                "                                  (desat.b < 0.5 ? (2.0 * desat.b * filterColor.b) : (1.0 - 2.0 * (1.0 - desat.b) * (1.0 - filterColor.b))),\n" +
                "                                  1.0\n" +
                "                                  );\n" +
                " 	\n" +
                " 	//which is better, or are they equal?\n" +
                " 	gl_FragColor = vec4( mix(textureColor.rgb, outputColor.rgb, intensity), textureColor.a);\n" +
                "  }";
        return fgs;
    }
    private int mIntensityLocation;
    private float mIntensity;
    private int mFilterColorLocation;
    private float[] mColor;
    @Override
    public void init(){
        super.init();
        mIntensityLocation = GLES20.glGetUniformLocation(getProgram(), "intensity");
        mFilterColorLocation = GLES20.glGetUniformLocation(getProgram(), "filterColor");
    }
    @Override
    public void onInitialized() {
        super.onInitialized();
        setIntensity(1.0f);
        setColor(new float[]{ 0.6f, 0.45f, 0.3f, 1.f });
    }
    public void setIntensity(final float intensity) {
        mIntensity = intensity;
        setFloat(mIntensityLocation, mIntensity);
    }
    public void setColor(final float[] color) {
        mColor = color;
        setColorRed(mColor[0], mColor[1], mColor[2]);

    }

    public void setColorRed(final float red, final float green, final float blue) {
        setFloatVec3(mFilterColorLocation, new float[]{ red, green, blue });
    }
}
