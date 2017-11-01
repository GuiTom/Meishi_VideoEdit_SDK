package tech.qt.com.meishivideoeditsdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import transcoder.SystemUtil;
import transcoder.VideoTranscoder;
import transcoder.engine.MediaTranscoder;
import transcoder.engine.displayObject.Animation;
import transcoder.engine.displayObject.AnimationBitmap;
import transcoder.engine.displayObject.AnimationText;
import transcoder.format.MediaPreSet;
import transcoder.format.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class VideoJoinActivity extends Activity {
    private static final String TAG = "VideoJoinActivity";
    private static final String FILE_PROVIDER_AUTHORITY = "net.ypresto.androidtranscoder.example.fileprovider";
    private static final int REQUEST_CODE_PICK = 1;
    private static final int PROGRESS_BAR_MAX = 1000;
    private Future<Void> mFuture;
    private long startTime;
    private ArrayList<Uri>fileUris;
//    private ArrayList<ParcelFileDescriptor>parcelFileDescriptors;
    private String dstMediaPath;
    private File outFile=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videojoin);
        findViewById(R.id.select_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video/*"), REQUEST_CODE_PICK);
            }
        });
        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaTranscoder.get_instance().setCanceld(true);
            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {


                if(fileUris==null||fileUris.size()<2){
                    Toast.makeText(getApplicationContext(),"请选择两个或以上的视频",Toast.LENGTH_SHORT);
                    return;
                }
                startTime = SystemClock.uptimeMillis();
                MediaPreSet mediaPreSet=new MediaPreSet();
                //输出的视频的参数设置
                mediaPreSet.videoWidth=540;
                mediaPreSet.videoHeight=960;
                mediaPreSet.videoRotation=0;
                mediaPreSet.keyFrameInterval=5;
                mediaPreSet.videoframeRate=25;
                mediaPreSet.videoBitRate=5000*1000;
                mediaPreSet.audioBitRate=128*1000;
                mediaPreSet.audioSampleRate=48*1000;
                mediaPreSet.audioChannelCount=2;

//                           //设置遮罩
                String borderPath= Environment.getExternalStorageDirectory()+"/border1";
//                setUpBorderAnimation(borderPath,20,30);
                setUpTextAnimation();
                String a= SystemUtil.getDeviceBrand();
                String b=SystemUtil.getSystemModel();
                String srcPath1=fileUris.get(0).toString();
//                srcPath1=null;
                String srcPath2=fileUris.get(1).toString();
//                List<Camera.Size> sizes= getSupportedSize();

                 dstMediaPath= new File(Environment.getExternalStorageDirectory(),"outPut.mp4").getAbsolutePath();
                try {
                    MediaTranscoder.get_instance().transcodeFromSource(getApplicationContext(),srcPath1,srcPath2,dstMediaPath,mediaPreSet,listener);
                } catch (IOException e) {
                    Log.e("trascode",Log.getStackTraceString(e));
                }
                switchButtonEnabled(true);
            }
        });
    }
    private void setUpBorderAnimation(String borderPath,int frameNum,int frameRate) {

        AnimationBitmap animationBitmap=new AnimationBitmap();
        animationBitmap.alpha=0.0f;
        animationBitmap.position=new Point(0,0);
        animationBitmap.size=new Size(960,540);
        ArrayList<Animation>animations=new ArrayList<Animation>();
        Animation animation=new Animation();
        animation.animationType= Animation.ANIMATIONTYPE.bitMap;
        for(int i=0;i<frameNum;i++){
            String filePath=borderPath+String.format("/%04d.png",(i+1));
            BitmapDrawable bitmapDrawable= (BitmapDrawable) BitmapDrawable.createFromPath(filePath);
            Bitmap bitmap=bitmapDrawable.getBitmap();
            float keyTime=(float) i/frameRate;
            animation.keyTimes.add(keyTime);
            animation.keyValues.add(bitmap);
            if(i==0){
                animationBitmap.setBitmap(bitmap);
            }
        }

        animation.duration=(float)20/30;
        animation.repeatCount=10000;
        animations.add(animation);
        animationBitmap.animations=animations;
        MediaTranscoder.get_instance().animationBitmaps.add(animationBitmap);
    }
    private void setUpTextAnimation() {
        AnimationText animationText=new AnimationText();
        animationText.text="文字动画";
        animationText.alpha=0.0f;
        ArrayList<Animation>animations=new ArrayList<Animation>();
        //位置动画
        Animation positionAnimation=new Animation();
        positionAnimation.animationType= Animation.ANIMATIONTYPE.position;

        positionAnimation.keyValues.add(animationText.position);
        positionAnimation.keyTimes.add(0.0f);
        positionAnimation.keyValues.add(new Point(100,200));
        positionAnimation.keyTimes.add(3.0f);
        positionAnimation.keyValues.add(new Point(600,300));
        positionAnimation.keyTimes.add(6.0f);
        animations.add(positionAnimation);

        //透明度动画
        Animation alphaAnimation=new Animation();
        alphaAnimation.animationType= Animation.ANIMATIONTYPE.alpha;

        alphaAnimation.keyValues.add(animationText.alpha);
        alphaAnimation.keyTimes.add(0.0f);

        alphaAnimation.keyValues.add(0.5f);
        alphaAnimation.keyTimes.add(3.0f);

        alphaAnimation.keyValues.add(0.1f);
        alphaAnimation.keyTimes.add(6.0f);
        animations.add(alphaAnimation);

        //字体大小动画
        Animation fontSizeAnimation=new Animation();
        fontSizeAnimation.animationType= Animation.ANIMATIONTYPE.fontSize;

        fontSizeAnimation.keyValues.add(animationText.fontSize);//第一个值应该是原本的值
        fontSizeAnimation.keyTimes.add(0.0f);

        fontSizeAnimation.keyValues.add(60.0f);
        fontSizeAnimation.keyTimes.add(3.0f);

        fontSizeAnimation.keyValues.add(20.0f);
        fontSizeAnimation.keyTimes.add(6.0f);
        animations.add(fontSizeAnimation);

        animationText.animations=animations;
        MediaTranscoder.get_instance().animationTexts.add(animationText);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK: {

                if (resultCode == RESULT_OK) {
                    if(fileUris==null){
                        fileUris=new ArrayList<>();
                    }
                    fileUris.add(data.getData());

                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    VideoTranscoder.Listener listener = new VideoTranscoder.Listener() {
        private double _progress;
        @Override
        public void onTranscodeProgress(double progress) {
            final double _progress=progress;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                    progressBar.setMax(PROGRESS_BAR_MAX);
                    if (_progress < 0) {
                        progressBar.setIndeterminate(true);
                    } else {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress((int) Math.round(_progress * PROGRESS_BAR_MAX));
                        ((TextView)findViewById(R.id.timeView)).setText("timeUse:"+(SystemClock.uptimeMillis() - startTime)/1000);
                    }
                }
            });

        }

        @Override
        public void onTranscodeCompleted() {

            Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.timeView)).setText("timeUse:"+(SystemClock.uptimeMillis() - startTime)/1000);
                }
            });
            Uri uri = Uri.parse(dstMediaPath);
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "video/mp4")
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        }

        @Override
        public void onTranscodeCanceled() {
            onTranscodeFinished(false, "Transcoder canceled.");
        }

        @Override
        public void onTranscodeFailed(Exception exception) {


            Log.e("error",Log.getStackTraceString(exception));
        }
    };




    private void onTranscodeFinished(boolean isSuccess, String toastMessage) {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(isSuccess ? PROGRESS_BAR_MAX : 0);
        switchButtonEnabled(false);

        Toast.makeText(VideoJoinActivity.this, toastMessage, Toast.LENGTH_LONG).show();

    }

    private void switchButtonEnabled(boolean isProgress) {
        findViewById(R.id.select_video_button).setEnabled(!isProgress);
        findViewById(R.id.cancel_button).setEnabled(isProgress);

    }


}
