package utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tech.qt.com.meishivideoeditsdk.MetaInfo;
import transcoder.format.MediaPreSet;

/**
 * Created by chenchao on 2017/11/9.
 */

public class MetaInfoUtil {
    public static MetaInfo getMediaInfo(Context context, Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context,uri);
        MetaInfo metaInfo=new MetaInfo();
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String bitRate= retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
//        String frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);

        metaInfo.durationMS=Integer.valueOf(duration);

        metaInfo.thumbnail=bitmap;

        metaInfo.videoBitRate=Integer.valueOf(bitRate);
//        metaInfo.videoFrameRate=Float.valueOf(frameRate);

        metaInfo.videoWidth=Integer.valueOf(width);
        metaInfo.videoHeight=Integer.valueOf(height);
        return metaInfo;

    }
    public static boolean compare(Context context,ArrayList<Uri>uris){
        MediaFormat videoFormat=null,audioFormat=null;
        int roation = -1;
        for(Uri uri:uris){
            MediaExtractor mediaExtractor = new MediaExtractor();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context,uri);
            try {
                mediaExtractor.setDataSource(context,uri,null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trackCount = mediaExtractor.getTrackCount();
            for (int i=0;i<trackCount;i++){
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                if(mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")){
                    if(videoFormat==null){
                        videoFormat = mediaFormat;
                    }else {
                        if(videoFormat.getInteger(MediaFormat.KEY_HEIGHT)!=mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)){
                            return false;
                        }
                        if(videoFormat.getInteger(MediaFormat.KEY_WIDTH)!=mediaFormat.getInteger(MediaFormat.KEY_WIDTH)){
                            return false;
                        }
                        if(videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)!=mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)){
                            return false;
                        }
//                        if(videoFormat.getInteger(MediaFormat.KEY_ROTATION)!=mediaFormat.getInteger(MediaFormat.KEY_ROTATION)){
//                            return false;
//                        }
                        String rotationStr= retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                        int _rotation = Integer.valueOf(rotationStr);

                        if(roation!=-1&&_rotation!=-1){
                            if(roation!=_rotation){
                                return false;
                            }
                        }
                        roation = _rotation;
                        if(!videoFormat.getString(MediaFormat.KEY_MIME).equals(mediaFormat.getString(MediaFormat.KEY_MIME))){
                            return false;
                        }
                    }
                }else {
                    if(audioFormat==null){
                        audioFormat = mediaFormat;
                    }else {
                        if(audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)!=mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)){
                            return false;
                        }

                        if(!audioFormat.getString(MediaFormat.KEY_MIME).equals(mediaFormat.getString(MediaFormat.KEY_MIME))){
                            return false;
                        }
                        if(audioFormat.getInteger(MediaFormat.KEY_AAC_PROFILE)!=mediaFormat.getInteger(MediaFormat.KEY_AAC_PROFILE)){
                            return false;
                        }
                        if(audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)!=mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)){
                            return false;
                        }


                    }
                }
            }
            mediaExtractor.release();

        }
        return true;
    }
}
