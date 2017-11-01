package tech.qt.com.meishivideoeditsdk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityManager;
import android.widget.MediaController;

/**
 * Created by chenchao on 2017/9/5.
 */

public class MyMediaController extends MediaController {
    public MyMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyMediaController(Context context) {
        super(context, true);
    }
    @Override
    public void hide() {
        super.show();
    }
}
