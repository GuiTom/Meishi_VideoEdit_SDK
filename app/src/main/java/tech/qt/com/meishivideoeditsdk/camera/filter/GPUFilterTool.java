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

//import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.twoInput.GPUBlendScreenFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.twoInput.GPUGrayScaleFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.twoInput.GPULookupFilter;
import tech.qt.com.meishivideoeditsdk.camera.filter.twoInput.GPUTowInputFilter;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUFilterTool {
    private static GPUBlendScreenFilter gpuTowInputFilter;
    private static GPUFilter filter;

    private enum FilterType{
        Speia,GrayScale,LOOK_UP,NONE
    }
    private static class FilterList {
        public List<String> names = new LinkedList<String>();
        public List<FilterType> types = new LinkedList<FilterType>();
        public List<String> resNames = new LinkedList<String>();
        public void addCoverFilter(final String name, String resName) {
            names.add(name);
            resNames.add(resName);
        }
        public void addFilter(final String name, FilterType filterType, String resName) {
            names.add(name);
            types.add(filterType);
            resNames.add(resName);
        }
    }
    public interface onGpuFilterChosenListener {
        void onGpuFilterChosenListener(GPUFilter filter);
    }
    public static void showCoverDialog(final Context context,
                                       final onGpuFilterChosenListener listener) {

        final FilterList filterList = new FilterList();
        filterList.addCoverFilter("无", "none");
        filterList.addCoverFilter("霓虹灯", "nihongdeng");
        filterList.addCoverFilter("七彩光","qicaiguang");
        filterList.addCoverFilter("下雪了", "xiaxuele");
        filterList.addCoverFilter("烟花", "yanhua");
        filterList.addCoverFilter("萤火虫", "yinghuochong");
        filterList.addCoverFilter("蝴蝶","hudie");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个遮罩");

        builder.setItems(filterList.names.toArray(new String[filterList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        ArrayList<Bitmap>bitmaps = null;

                        if(gpuTowInputFilter == null){
                            gpuTowInputFilter = new GPUBlendScreenFilter();
                        }else{
                            gpuTowInputFilter.setNeedRealse(true);
                            bitmaps = gpuTowInputFilter.bitmaps;
                            gpuTowInputFilter.blockOverLay=true;
                            if(bitmaps!=null&&bitmaps.size()>0){
                                int size=bitmaps.size();
                                for(Bitmap bitmap:bitmaps){
                                    bitmap.recycle();
                                }
                            }
                            gpuTowInputFilter = new GPUBlendScreenFilter();

                        }
                        int frameNum=60;


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
                            gpuTowInputFilter.bitmaps = bitmaps;
                            gpuTowInputFilter.blockOverLay=false;
                        }else {
                            gpuTowInputFilter.setNeedRealse(true);
                            gpuTowInputFilter = null;

                        }
                        listener.onGpuFilterChosenListener(gpuTowInputFilter);

                    }
                });

        builder.create().show();
    }
    public static void showFilterDialog(final Context context,
                                       final onGpuFilterChosenListener listener) {

        final FilterList filterList = new FilterList();
        filterList.addFilter("无", FilterType.NONE,"none");

        filterList.addFilter("黑白控",FilterType.GrayScale,"none");
        filterList.addFilter("薄荷糖",FilterType.LOOK_UP,"bohetang.png");
        filterList.addFilter("复古", FilterType.LOOK_UP,"fugu.jpg");
        filterList.addFilter("红润",FilterType.LOOK_UP,"hongrun.jpg");
        filterList.addFilter("蓝调",FilterType.LOOK_UP,"landiao.png");

        filterList.addFilter("年华",FilterType.LOOK_UP,"nianhua.png");
        filterList.addFilter("日系",FilterType.LOOK_UP ,"rixi.jpg");
        filterList.addFilter("思念",FilterType.LOOK_UP ,"sinian.png");
        filterList.addFilter("往事",FilterType.LOOK_UP,"wangshi.jpg");
        filterList.addFilter("夏日",FilterType.LOOK_UP,"xiari.png");
        filterList.addFilter("阳光",FilterType.LOOK_UP,"yangguang.png");
        filterList.addFilter("自然",FilterType.LOOK_UP,"ziruan.png");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个滤镜");

        builder.setItems(filterList.names.toArray(new String[filterList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        if(filter!= null){
                            //以后把内存回收处理好
                            filter.setNeedRealse(true);
                        }
                        filter = null;
                        FilterType filterType = filterList.types.get(item);
                        String filterName = filterList.names.get(item);
                        String resName = filterList.resNames.get(item);
                        if(filterType == FilterType.NONE){
                            filter = null;
                        }else if(filterType == filterType.GrayScale){
                            filter = new GPUGrayScaleFilter();
                        }else if(filterType == filterType.LOOK_UP){
                            filter = new GPULookupFilter();

                            String fileName="filters/lookup_"+resName;
                            Bitmap bitmap = null;
                            try {
                                InputStream is = context.getAssets().open(fileName);
                                bitmap = BitmapFactory.decodeStream(is);
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ((GPUTowInputFilter)filter).setBitmap(bitmap);
                        }
                        listener.onGpuFilterChosenListener(filter);

                    }
                });

        builder.create().show();
    }
}
