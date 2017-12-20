package tech.qt.com.meishivideoeditsdk;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import tech.qt.com.meishivideoeditsdk.wiget.Group;
import tech.qt.com.meishivideoeditsdk.wiget.Item;
import tech.qt.com.meishivideoeditsdk.wiget.MyBaseExpandableListAdapter;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA= 0;
    private ArrayList<Group> gData = null;
    private ArrayList<ArrayList<Item>> iData = null;
    private ArrayList<Item> lData = null;
    private Context mContext;
    private ExpandableListView exlist_lol;
    private MyBaseExpandableListAdapter myAdapter = null;
    private int mChildPosition;

    public static int videoProtrait=0;
    public static int videoLandscape=1;
    public static int videoSquare=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        exlist_lol = (ExpandableListView) findViewById(R.id.exlist_lol);


        //分组数据
        gData = new ArrayList<Group>();
        iData = new ArrayList<ArrayList<Item>>();
        gData.add(new Group("录制"));
        gData.add(new Group("后期编辑"));


        lData = new ArrayList<Item>();

        //录制
        lData.add(new Item("竖屏"));
        lData.add(new Item("横屏"));
        lData.add(new Item("方形"));

        iData.add(lData);
        //后期处理
        lData = new ArrayList<Item>();
        lData.add(new Item("视频拼接"));
        lData.add(new Item("视频截取"));
        lData.add(new Item("视频加字幕、动画"));
        iData.add(lData);


        myAdapter = new MyBaseExpandableListAdapter(gData,iData,mContext);
        exlist_lol.setAdapter(myAdapter);
        int groupCount = exlist_lol.getCount();
        for (int i=0; i<groupCount; i++) {
            exlist_lol.expandGroup(i);
        };

        //为列表设置点击事件
        exlist_lol.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Toast.makeText(mContext, "你点击了：" + iData.get(groupPosition).get(childPosition).getiName(), Toast.LENGTH_SHORT).show();
                if(groupPosition==0){
                    if(childPosition==0){
                        Intent intent=new Intent(MainActivity.this,CameraProtraitActivity2.class);
                        intent.putExtra("videoType", videoProtrait);
                        startActivity(intent);
                    }else if(childPosition==1){
                        Intent intent=new Intent(MainActivity.this,CameraLandscapeActivity.class);
                        intent.putExtra("videoType", videoLandscape);
                        startActivity(intent);
                    }else if(childPosition==2){
                        Intent intent=new Intent(MainActivity.this,CameraSquareActivity.class);
                        intent.putExtra("videoType", videoSquare);
                        startActivity(intent);
                    }
                }else if(groupPosition==1) {
                    if(childPosition==0){
                        Intent intent=new Intent(MainActivity.this,VideoJoinActivity.class);
                        startActivity(intent);
                    }else if(childPosition==1){
                        Intent intent=new Intent(MainActivity.this,VideoCutActivity.class);
                        startActivity(intent);
                    }else if(childPosition==2){
                        Intent intent=new Intent(MainActivity.this,VideoEffectActivity.class);
                        startActivity(intent);
                    }
                }
                return true;
            }
        });

//        View button = findViewById(R.id.async_task);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                startAsyncTask();
//            }
//        });
    }



//    @SuppressLint("StaticFieldLeak")
//    void startAsyncTask() {
//        // This async task is an anonymous class and therefore has a hidden reference to the outer
//        // class MainActivity. If the activity gets destroyed before the task finishes (e.g. rotation),
//        // the activity instance will leak.
//        new AsyncTask<Void, Void, Void>() {
//            @Override protected Void doInBackground(Void... params) {
//                // Do some slow work in background
//                SystemClock.sleep(20000);
//                return null;
//            }
//        }.execute();
//    }

}
