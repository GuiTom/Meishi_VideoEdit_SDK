package tech.qt.com.meishivideoeditsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import tech.qt.com.meishivideoeditsdk.wiget.Group;
import tech.qt.com.meishivideoeditsdk.wiget.Item;
import tech.qt.com.meishivideoeditsdk.wiget.MyBaseExpandableListAdapter;

public class MainActivity extends Activity {

    private ArrayList<Group> gData = null;
    private ArrayList<ArrayList<Item>> iData = null;
    private ArrayList<Item> lData = null;
    private Context mContext;
    private ExpandableListView exlist_lol;
    private MyBaseExpandableListAdapter myAdapter = null;


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
//        lData.add(new Item("横屏"));


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
                    if(childPosition==0){
                        Intent intent=new Intent(MainActivity.this,CameraProtraitActivity2.class);
                        intent.putExtra("videoType", CameraProtraitActivity.videoProtrait);
                        startActivity(intent);
                    }else if(childPosition==1){
                        Intent intent=new Intent(MainActivity.this,CameraLandscapeActivity.class);
                        intent.putExtra("videoType", CameraProtraitActivity.videoLandscape);
                        startActivity(intent);
                    }
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

}
