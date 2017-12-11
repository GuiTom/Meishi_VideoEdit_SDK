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

import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageScreenBlendFilter;
import utils.GPUImageFilterTools;

/**
 * Created by chenchao on 2017/12/11.
 */

public class GPUFilterTool {
    private static GPUBlendScreenFilter gpuTowInputFilter;

    private enum CoverType {

        NONE,NIHONGDENG,QICAIGUANG,XIAXUELE,YANHUA,YINGHUOCHONG,HUDIE
    }
    private static class CoverList {
        public List<String> names = new LinkedList<String>();
        public List<CoverType> covers = new LinkedList<CoverType>();
        public List<String> resNames = new LinkedList<String>();
        public void addCover(final String name, final CoverType coverType,String resName) {
            names.add(name);
            covers.add(coverType);
            resNames.add(resName);
        }

    }
    public static void showCoverDialog(final Context context,
                                       final GPUImageFilterTools.OnGpuImageCoverChosenListener listener) {

        final CoverList coverList = new CoverList();
        coverList.addCover("无", CoverType.NONE,"none");
        coverList.addCover("霓虹灯", CoverType.NIHONGDENG,"nihongdeng");
        coverList.addCover("七彩光",CoverType.QICAIGUANG,"qicaiguang");
        coverList.addCover("下雪了", CoverType.XIAXUELE,"xiaxuele");
        coverList.addCover("烟花", CoverType.YANHUA,"yanhua");
        coverList.addCover("萤火虫", CoverType.YINGHUOCHONG,"yinghuochong");
        coverList.addCover("蝴蝶", CoverType.HUDIE,"hudie");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个遮罩");

        builder.setItems(coverList.names.toArray(new String[coverList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        ArrayList<Bitmap>bitmaps = null;
                        if(gpuTowInputFilter == null){
                            gpuTowInputFilter = new GPUBlendScreenFilter();
                        }else{
                            bitmaps = gpuTowInputFilter.bitmaps;
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

                        CoverType coverType=coverList.covers.get(item);
                        String resName = coverList.resNames.get(item);
                        if(coverType!= CoverType.NONE){
                            bitmaps=new ArrayList<Bitmap>();
                            for(int i=0;i<frameNum;i++){
                                frameNum=30;
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
                        gpuTowInputFilter.bitmaps = bitmaps;

                    }
                });
        builder.create().show();
    }
}
