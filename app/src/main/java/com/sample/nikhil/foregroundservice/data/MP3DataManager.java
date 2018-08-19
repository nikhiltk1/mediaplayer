package com.sample.nikhil.foregroundservice.data;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sample.nikhil.foregroundservice.MediaFile;
import com.sample.nikhil.foregroundservice.data.model.ContextualObject;
import com.sample.nikhil.foregroundservice.utils.ThreadPoolExecutorWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Nikhil on 18-08-2018.
 */

class MP3DataManager extends ContextualObject {

    private static String TAG = "MP3DataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;

    private ArrayList<MediaFile> mFiles = new ArrayList<>();
    private MediaFile mCurrentPlayingFile;
    private MediaFile mLastPlayedFile;

    private AudioManager mAudioManager;
    private AudioAttributes mAudioAttributes;
    private AudioFocusRequest mAudioFocusRequest;
    private MediaPlayer mMediaPlayer;
    private Queue<Runnable> runnables;

    MP3DataManager(Context context) {
        super(context);
        mMediaPlayer = new MediaPlayer();
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.MP3FILE_LIST_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
    }

    ArrayList<MediaFile> getFiles() {
        return mFiles;
    }

    void setFiles(ArrayList<MediaFile> mFiles) {
        this.mFiles = mFiles;
    }

    MediaFile getCurrentPlayingFile() {
        return mCurrentPlayingFile;
    }

    void setCurrentPlayingFile(MediaFile mCurrentPlayingFile) {
        this.mCurrentPlayingFile = mCurrentPlayingFile;
    }

    MediaFile getLastPlayedFile() {
        return mLastPlayedFile;
    }

    void setLastPlayedFile(MediaFile mLastPlayedFile) {
        this.mLastPlayedFile = mLastPlayedFile;
    }

    MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    AudioManager getAudioManager() {
        return mAudioManager;
    }

    void setAudioManager(AudioManager audioManager) {
        this.mAudioManager = audioManager;
    }

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            switch (i) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    try {
                        mMediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mMediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mMediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mMediaPlayer.pause();
                    break;
            }
        }
    };

    int getAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest == null) {
            mAudioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    .build();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } else {
            return mAudioManager.requestAudioFocus(mAudioFocusRequest);
        }

    }

    int removeAudioFocus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        } else {
            return mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        }
    }

    private List<MediaFile> getFilesRunnable(final File folder) {

        synchronized (this) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<MediaFile> fileList = new ArrayList<>();
                    try {
                        final File[] files = folder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
                        for (File file : files) {
                            if (file.isDirectory()) {
                                if (getFilesRunnable(file) != null) {
                                    fileList.addAll(getFilesRunnable(file));
                                } else {
                                    break;
                                }
                            } else if (file.getName().endsWith(".mp3")) {
                                MediaFile mediaFile = new MediaFile(file);
                                fileList.add(mediaFile);
                            }
                        }
                        mFiles.addAll(fileList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
/*

*/
        return null;
    }

    List<MediaFile> getFilesUiThread(final File folder) {
        final ArrayList<MediaFile> fileList = new ArrayList<>();
        try {
            final File[] files = folder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getFilesUiThread(file) != null) {
                        fileList.addAll(getFilesUiThread(file));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    MediaFile mediaFile = new MediaFile(file);
                    fileList.add(mediaFile);
                }
            }
            return fileList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void getPlayList(File rootFolder, DataManager.Mp3FileListener mp3FileListener) {
        GetMp3FilesCallable callable = new GetMp3FilesCallable(rootFolder, getContext(),mp3FileListener);
        GetMp3FutureTask task = new GetMp3FutureTask(callable, mUiThreadHandler, mp3FileListener);
        mThreadPoolExecutor.execute(task);
    }


    private static final class UiThreadHandler extends android.os.Handler {

        private static final int MSG_WHAT_MP3_FILES_FETCH_COMPLETE = 100;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_MP3_FILES_FETCH_COMPLETE) {
                //ToDo call listener to update UI.
                Mp3FilesFetchResult result = (Mp3FilesFetchResult) msg.obj;
                if (result.mMp3FileListener != null) {
                    result.mMp3FileListener.onFilesRetrieved(result.mMediaFiles);
                }
                return;
            }
        }
    }

    private final class GetMp3FilesCallable implements Callable<List<MediaFile>> {

        private Context mContext;
        private File mRootFolder;
        private DataManager.Mp3FileListener mMp3FileListener;

        GetMp3FilesCallable(File rootFolder, Context mContext, DataManager.Mp3FileListener mMp3FileListener) {
            this.mContext = mContext;
            this.mRootFolder = rootFolder;
            this.mMp3FileListener=mMp3FileListener;
        }

        @Override
        public List<MediaFile> call() throws Exception {
            return getFilesUiThread(mRootFolder);
        }
    }

    private static final class GetMp3FutureTask extends FutureTask<List<MediaFile>> {

        private Handler mHandler;
        private DataManager.Mp3FileListener mMp3FileListener;

        public GetMp3FutureTask(@NonNull Callable<List<MediaFile>> callable, Handler handler, DataManager.Mp3FileListener mp3FileListener) {
            super(callable);
            this.mHandler = handler;
            this.mMp3FileListener = mp3FileListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<MediaFile> mediaFiles = get();
                    Mp3FilesFetchResult mp3FilesFetchResult = new Mp3FilesFetchResult(mediaFiles, mMp3FileListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_MP3_FILES_FETCH_COMPLETE, mp3FilesFetchResult);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AccountsFetchTask failed.reason:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static final class Mp3FilesFetchResult {
        DataManager.Mp3FileListener mMp3FileListener;
        List<MediaFile> mMediaFiles;

        private Mp3FilesFetchResult(List<MediaFile> mediaFiles, DataManager.Mp3FileListener mp3FileListener) {
            mMediaFiles = mediaFiles;
            mMp3FileListener = mp3FileListener;
        }
    }
}
