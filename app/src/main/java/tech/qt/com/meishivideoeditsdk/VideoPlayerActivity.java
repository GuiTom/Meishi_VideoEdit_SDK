package tech.qt.com.meishivideoeditsdk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends Activity {

    private VideoView vv_video;
    private MediaController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        vv_video=(VideoView) findViewById(R.id.videoView);

        mController=new MediaController(this);
        String dstMediaPath= getIntent().getStringExtra("videoPath");


        vv_video.setVideoPath(dstMediaPath);

        vv_video.setMediaController(mController);

        mController.setMediaPlayer(vv_video);
        vv_video.start();
        mController.setPrevNextListeners(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        }, new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }



}
