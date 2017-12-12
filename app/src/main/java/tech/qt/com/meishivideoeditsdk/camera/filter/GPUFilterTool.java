package tech.qt.com.meishivideoeditsdk.camera.filter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUFilterTool {
    private static GPUBlendScreenFilter gpuTowInputFilter;

    private static class FilterList {
        public List<String> names = new LinkedList<String>();

        public List<String> resNames = new LinkedList<String>();
        public void addCover(final String name,String resName) {
            names.add(name);

            resNames.add(resName);
        }
    }
    public interface onGpuFilterChosenListener {
        void onGpuFilterChosenListener(GPUFilter filter);
    }
    public static void showCoverDialog(final Context context,
                                       final onGpuFilterChosenListener listener) {

        final FilterList filterList = new FilterList();
        filterList.addCover("无", "none");
        filterList.addCover("霓虹灯", "nihongdeng");
        filterList.addCover("七彩光","qicaiguang");
        filterList.addCover("下雪了", "xiaxuele");
        filterList.addCover("烟花", "yanhua");
        filterList.addCover("萤火虫", "yinghuochong");
        filterList.addCover("蝴蝶","hudie");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个遮罩");

        builder.setItems(filterList.names.toArray(new String[filterList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        ArrayList<Bitmap>bitmaps = null;
                        gpuTowInputFilter = new GPUBlendScreenFilter();
                        if(gpuTowInputFilter == null){
                            gpuTowInputFilter = new GPUBlendScreenFilter();
                        }else{
                            bitmaps = GPUTowInputFilter.bitmaps;
                        }

                        int frameNum=60;
                        GPUBlendScreenFilter.blockOverLay=true;
                        if(bitmaps!=null&&bitmaps.size()>0){
                            int size=bitmaps.size();
                            for(Bitmap bitmap:bitmaps){
                                bitmap.recycle();
                            }
                        }

                        bitmaps=null;


                        String resName = filterList.resNames.get(item);
                        String folderName = "images_"+resName;

                        try {
                            frameNum = context.getAssets().list(folderName).length;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!resName.contentEquals("none")){
                            bitmaps=new ArrayList<Bitmap>();
                            for(int i=0;i<frameNum;i++){

                                String fileName=String.format("images_%s/image_%d.jpg",resName,(i+1));

                                try {
                                    InputStream is = context.getAssets().open(fileName);
                                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                                    bitmaps.add(bitmap);
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        GPUTowInputFilter.bitmaps = bitmaps;
                        listener.onGpuFilterChosenListener(gpuTowInputFilter);

                    }
                });

        builder.create().show();
    }
    public static void showFilterDialog(final Context context,
                                       final onGpuFilterChosenListener listener) {

        final FilterList filterList = new FilterList();
        filterList.addCover("无", "none");
        filterList.addCover("往事", "nihongdeng");
        filterList.addCover("黑白控","qicaiguang");
        filterList.addCover("复古", "xiaxuele");
        filterList.addCover("思念", "yanhua");
        filterList.addCover("炫彩", "yinghuochong");
        filterList.addCover("唯美","hudie");
        filterList.addCover("时光","hudie");
        filterList.addCover("阳光","hudie");
        filterList.addCover("那一年","hudie");
        filterList.addCover("冰激凌","hudie");
        filterList.addCover("薄荷糖","hudie");
        filterList.addCover("蓝调","hudie");
        filterList.addCover("自然","hudie");
        filterList.addCover("红润","hudie");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个滤镜");

        builder.setItems(filterList.names.toArray(new String[filterList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {

                    }
                });

        builder.create().show();
    }
}
