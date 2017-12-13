package utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenchao on 2017/12/12.
 */

public class UIUtils {
    private static class MusicList {
        public List<String> names = new LinkedList<String>();
        public List<String> resNames = new LinkedList<String>();
        public void addMusic(final String name, String resName) {
            names.add(name);
            resNames.add(resName);
        }
    }
    public interface OnMusicChosenListener {
        void onMusicChosenListener(String path);
    }
    public static void showMusicDialog(final Context context,
                                       final OnMusicChosenListener listener) {

        final MusicList musicList = new MusicList();
        musicList.addMusic("音乐1","sample");
        musicList.addMusic("音乐2","sample2");
        musicList.addMusic("音乐3","sample3");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择一个伴奏音乐");

        builder.setItems(musicList.names.toArray(new String[musicList.names.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        String resName = musicList.resNames.get(item);
                        String mp3Path= Environment.getExternalStorageDirectory()+"/audioMX/"+resName+".mp3";
                        String path = Environment.getExternalStorageDirectory()+"/audioMX";

                        FileUtils.copyFilesFassets(context,"sample",path);
                        listener.onMusicChosenListener(mp3Path);
                    }
                });

        builder.create().show();
    }
}
