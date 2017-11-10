package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;


import java.lang.reflect.Method;

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
}
