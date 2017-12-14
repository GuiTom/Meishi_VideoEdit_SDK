package tech.qt.com.meishivideoeditsdk;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
//        gData.add(new Group("后期编辑"));


        lData = new ArrayList<Item>();

        //录制
        lData.add(new Item("竖屏"));
        lData.add(new Item("竖屏(老版)"));


        iData.add(lData);
        //后期处理
        lData = new ArrayList<Item>();
        lData.add(new Item("视频拼接"));
        lData.add(new Item("视频截取(时间轴)"));
//        iData.add(lData);


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
                    reqPermission(Manifest.permission.CAMERA,groupPosition);
                }else if(groupPosition==1) {
                    if(childPosition==0){
                        Intent intent=new Intent(MainActivity.this,VideoJoinActivity.class);
                        startActivity(intent);
                    }else if(childPosition==1){
                        Intent intent=new Intent(MainActivity.this,VideoCutActivity.class);
                        startActivity(intent);
                    }
                }
                return true;
            }
        });


    }
    @TargetApi(23)
    public void reqPermission(String permission,int childPosition){
        mChildPosition = childPosition;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            //检查目前是否有权限
            if (checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(
                        permission)) {
                    // 这里写一些向用户解释为什么我们需要读取联系人的提示得代码
                }

                //请求权限，系统会显示一个获取权限的提示对话框，当前应用不能配置和修改这个对话框
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,permission},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                return;
            }else {
                if(childPosition==0){
                    Intent intent=new Intent(MainActivity.this,CameraProtraitActivity2.class);
                    intent.putExtra("videoType", CameraProtraitActivity.videoProtrait);
                    startActivity(intent);
                }else if(childPosition==1){
                    Intent intent=new Intent(MainActivity.this,CameraProtraitActivity.class);
                    intent.putExtra("videoType", CameraProtraitActivity.videoLandscape);
                    startActivity(intent);
                }
            }

        }else {
            if(childPosition==0){
                Intent intent=new Intent(MainActivity.this,CameraProtraitActivity2.class);
                intent.putExtra("videoType", CameraProtraitActivity.videoProtrait);
                startActivity(intent);
            }else if(childPosition==1){
                Intent intent=new Intent(MainActivity.this,CameraProtraitActivity.class);
                intent.putExtra("videoType", CameraProtraitActivity.videoLandscape);
                startActivity(intent);
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 授权成功
                    if(mChildPosition==0){
                        Intent intent=new Intent(MainActivity.this,CameraProtraitActivity2.class);
                        intent.putExtra("videoType", CameraProtraitActivity.videoProtrait);
                        startActivity(intent);
                    }else if(mChildPosition==1){
                        Intent intent=new Intent(MainActivity.this,CameraProtraitActivity.class);
                        intent.putExtra("videoType", CameraProtraitActivity.videoLandscape);
                        startActivity(intent);
                    }
                } else {
                    // 授权失败
                }
                return;
            }

        }
    }

}
