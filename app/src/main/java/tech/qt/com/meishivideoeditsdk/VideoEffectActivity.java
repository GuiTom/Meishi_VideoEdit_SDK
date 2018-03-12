package tech.qt.com.meishivideoeditsdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import transcoder.SystemUtil;
import transcoder.IListener;
import transcoder.VideoTranscoder;
import transcoder.engine.EffectLayer;
import transcoder.engine.MediaTranscoder;
import transcoder.engine.displayObject.Animation;
import transcoder.engine.displayObject.AnimationBitmap;
import transcoder.engine.displayObject.AnimationText;
import transcoder.engine.displayObject.Scale;
import transcoder.format.MediaPreSet;
import transcoder.format.Size;
import utils.FileUtils;
import utils.MetaInfoUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static android.widget.ImageView.ScaleType.FIT_CENTER;


public class VideoEffectActivity extends Activity {
    private static final String TAG = "VideoJoinActivity";
    //    private static final String FILE_PROVIDER_AUTHORITY = "net.ypresto.androidtranscoder.example.fileprovider";
    private static final int REQUEST_CODE_PICK = 1;
    private static final int PROGRESS_BAR_MAX = 1000;
    private Future<Void> mFuture;
    private long startTime;
    private ArrayList<Uri>fileUris;

    private String dstMediaPath;
    private File outFile=null;
    private ArrayList<MetaInfo> listItems;
    private ListView list;
    private ArrayList<LinearLayout> cellList;
    private ArrayList<AnimationBitmap> animationBitmaps;
    private ArrayList<AnimationText> animationTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videojoin);
        list=(ListView)findViewById(R.id.myList);
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


                if(fileUris==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoEffectActivity.this,"请选择需要添加字幕或动画的视频",Toast.LENGTH_LONG).show();
                        }
                    });

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
                mediaPreSet.videoBitRate=2000*1000;
                mediaPreSet.audioBitRate=128*1000;
                mediaPreSet.audioSampleRate=48*1000;
                mediaPreSet.audioChannelCount=2;



                String fileName="flower/flower.png";
                Bitmap bitmap = null;
                try {
                    InputStream is = getApplicationContext().getAssets().open(fileName);
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setUpTextAnimation();
                setUpImageAnimation(bitmap);


                dstMediaPath= new File(Environment.getExternalStorageDirectory(),"outPut.mp4").getAbsolutePath();
                EffectLayer effectLayer = new EffectLayer();
                effectLayer.animationBitmaps = animationBitmaps;
                effectLayer.animationTexts = animationTexts;
                effectLayer.getTimeRange();
                VideoTranscoder.getInstance().transcodeVideo(getApplicationContext(), fileUris, dstMediaPath, mediaPreSet, effectLayer,false, listener);
                switchButtonEnabled(true);
            }


        });

        setupListView();

    }
    private void setUpImageAnimation(Bitmap bitmap) {
        AnimationBitmap animationBitmap=new AnimationBitmap();
        animationBitmap.alpha=1.0f;
        animationBitmap.position=new Point(0,0);
        animationBitmap.size=new Size(100,100);
        animationBitmap.setBitmap(bitmap);
        ArrayList<Animation>animations=new ArrayList<Animation>();
        //Positin Animation
        Animation posAnimation=new Animation();
        posAnimation.animationType= Animation.ANIMATIONTYPE.position;
        posAnimation.keyTimes.add(0.5f);
        posAnimation.keyValues.add(new Point(20,20));
        posAnimation.keyTimes.add(2.5f);
        posAnimation.keyValues.add(new Point(200,100));
        posAnimation.keyTimes.add(3.5f);
        posAnimation.keyValues.add(new Point(100,200));
        animations.add(posAnimation);
        //scale Animation
        Animation scaleAnimation=new Animation();
        scaleAnimation.animationType= Animation.ANIMATIONTYPE.scale;
        scaleAnimation.keyTimes.add(0.5f);
        scaleAnimation.keyValues.add(new Scale(1.0f,1.0f));
        scaleAnimation.keyTimes.add(2.5f);
        scaleAnimation.keyValues.add(new Scale(1.5f,1.2f));
        scaleAnimation.keyTimes.add(3.5f);
        scaleAnimation.keyValues.add(new Scale(3.0f,3.0f));
        animations.add(scaleAnimation);
        //alpha Animation
        Animation alphaAnimation = new Animation();
        alphaAnimation.animationType = Animation.ANIMATIONTYPE.alpha;
        alphaAnimation.keyTimes.add(0.0f);
        alphaAnimation.keyValues.add(0.1f);

        alphaAnimation.keyTimes.add(1.5f);
        alphaAnimation.keyValues.add(1.0f);


        alphaAnimation.keyTimes.add(2.5f);
        alphaAnimation.keyValues.add(0.5f);

        alphaAnimation.keyTimes.add(3.5f);
        alphaAnimation.keyValues.add(0.0f);
        animations.add(alphaAnimation);

        animationBitmap.animations = animations;
        if(animationBitmaps == null) {
            animationBitmaps = new ArrayList<>();
        }
        animationBitmaps.add(animationBitmap);

    }
    private void setupListView() {
        listItems = new ArrayList<MetaInfo>();
        cellList=new ArrayList<LinearLayout>();
        BaseAdapter adapter = new BaseAdapter()
        {
            @Override
            public int getCount()
            {


                return listItems.size();
            }
            @Override
            public Object getItem(int position)
            {
                return null;
            }
            // 重写该方法，该方法的返回值将作为列表项的ID
            @Override
            public long getItemId(int position)
            {
                return position;
            }
            // 重写该方法，该方法返回的View将作为列表框
            @Override
            public View getView(int position
                    , View convertView , ViewGroup parent)
            {
                LinearLayout line;
                if(position>cellList.size()-1){
                    MetaInfo metaInfo=listItems.get(position);
                    // 创建一个LinearLayout，并向其中添加两个组件
                    line = new LinearLayout(VideoEffectActivity.this);
                    line.setOrientation(LinearLayout.HORIZONTAL);
                    ImageView image = new ImageView(VideoEffectActivity.this);
                    image.setLayoutParams(new LinearLayout.LayoutParams(200,200));
                    image.setScaleType(FIT_CENTER);
                    image.setImageBitmap(metaInfo.thumbnail);

                    line.addView(image);
                    LinearLayout right = new LinearLayout(VideoEffectActivity.this);
                    right.setOrientation(LinearLayout.VERTICAL);
                    TextView text = new TextView(VideoEffectActivity.this);
                    text.setText("时长:"+metaInfo.durationMS/1000);
                    text.setTextSize(20);
                    text.setTextColor(Color.GRAY);
                    right.addView(text);

                    TextView text2 = new TextView(VideoEffectActivity.this);
                    text2.setText("尺寸:"+metaInfo.videoWidth+"x"+metaInfo.videoHeight);
                    text2.setTextSize(20);
                    text2.setTextColor(Color.GRAY);
                    right.addView(text2);

                    line.addView(right);
                    cellList.add(line);
                }else {
                    line=cellList.get(position);

                }


                // 返回LinearLayout实例
                return line;
            }
        };
        list.setAdapter(adapter);

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

        animation.duration=(float)1/frameRate;
        animation.repeatCount=10000;
        animations.add(animation);
        animationBitmap.animations=animations;
        if(animationBitmaps == null) {
            animationBitmaps = new ArrayList<>();
        }
        animationBitmaps.add(animationBitmap);
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
        if(animationTexts == null) {
            animationTexts = new ArrayList<>();
        }
        animationTexts.add(animationText);
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
                    Uri uri=data.getData();
                    MetaInfo metaInfo= MetaInfoUtil.getMediaInfo(getApplicationContext(),uri);

                    listItems.add(metaInfo);
                    BaseAdapter baseAdapter = (BaseAdapter) list.getAdapter();
                    baseAdapter.notifyDataSetChanged();

                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    IListener listener = new IListener() {
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

            Intent intent=new Intent(VideoEffectActivity.this,VideoPlayerActivity.class);
            intent.putExtra("videoPath",dstMediaPath);
            startActivity(intent);

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
        Toast.makeText(VideoEffectActivity.this, toastMessage, Toast.LENGTH_LONG).show();
    }

    private void switchButtonEnabled(boolean isProgress) {
        findViewById(R.id.select_video_button).setEnabled(!isProgress);
        findViewById(R.id.cancel_button).setEnabled(isProgress);

    }


}
