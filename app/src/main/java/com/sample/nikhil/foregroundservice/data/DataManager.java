package com.sample.nikhil.foregroundservice.data;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.sample.nikhil.foregroundservice.data.model.MediaFile;
import com.sample.nikhil.foregroundservice.data.model.ContextualObject;

import java.io.File;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Nikhil on 12-08-2018.
 */

public class DataManager extends ContextualObject {
    private static DataManager mDataManager;

    private MP3DataManager mMP3DataManager;
    public static final Uri EXTERNAL_CONTENT_URI= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private DataManager(Context context) {
        super(context);
    }

    public static DataManager getInstance() {
        if (null == mDataManager) {
            throw new IllegalStateException("DashboardDataManager has not been set up yet!.Make sure to call DashboardDataManager.init(context) before accessing an instance");
        }
        return mDataManager;
    }

    public static void init(Context context) {
        if (mDataManager != null) {
            return;
        }
        mDataManager=new DataManager(context);
        mDataManager.initMP3DataManager();
    }

    private void initMP3DataManager() {
        mMP3DataManager = new MP3DataManager(getContext());
    }


    /********MP3 Methods starts***********/

    public int getAudioFocus() {
        return mMP3DataManager.getAudioFocus();
    }

    public int removeAudioFocus() {
        return mMP3DataManager.removeAudioFocus();
    }

    /*public void getPlayList(File rootFolder,Mp3FileListener listener) {
        mMP3DataManager.getPlayList(rootFolder,listener);
    }*/
    public void getPlayList(Uri uri) {
        mMP3DataManager.getPlayList(uri);
    }
    public List<File> getFolderList(){
        return mMP3DataManager.getFolderList();
    }


    public MediaPlayer getMediaPlayer() {
        return mMP3DataManager.getMediaPlayer();
    }

    public MediaFile getCurrentPlayingFile() {
        return mMP3DataManager.getCurrentPlayingFile();
    }

    public MediaFile getLastPlayedFile() {
        return mMP3DataManager.getLastPlayedFile();
    }

    public void setCurrentPlayingFile(MediaFile currentPlayingFile) {
        mMP3DataManager.setCurrentPlayingFile(currentPlayingFile);
    }

    public void setLastPlayedFile(MediaFile lastPlayedFile) {
        mMP3DataManager.setLastPlayedFile(lastPlayedFile);
    }
    public ArrayList<MediaFile> getFiles(){
        return mMP3DataManager.getFiles();
    }
    public List<File> getFolders(){
        return mMP3DataManager.getMediaFolders(getContext());
    }

    public ArrayList<MediaFile> retrieveMediaFromFolder(File folder){
        return mMP3DataManager.retrieveMediaFromFolder(folder,getContext());
    }
    public void setFiles(ArrayList<MediaFile> files) {
        mMP3DataManager.setFiles(files);
    }
    public AudioManager getAudioManager(){
        return mMP3DataManager.getAudioManager();
    }
    public void setAudioManager(AudioManager audioManager){
        mMP3DataManager.setAudioManager(audioManager);
    }

    public interface Mp3FileListener{
        void onFilesRetrieved(List<MediaFile> mp3Files);
    }

    /********MP3 Methods ends*************/

}
