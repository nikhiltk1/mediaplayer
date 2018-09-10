package com.sample.nikhil.foregroundservice.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sample.nikhil.foregroundservice.data.model.ContextualObject;
import com.sample.nikhil.foregroundservice.data.model.MediaFile;
import com.sample.nikhil.foregroundservice.data.model.Song;
import com.sample.nikhil.foregroundservice.utils.ThreadPoolExecutorWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * Created by Nikhil on 18-08-2018.
 */

class MP3DataManager extends ContextualObject {

    private static String TAG = "MP3DataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;

    private ArrayList<MediaFile> mFiles = new ArrayList<>();
    private List<File> mFolderList = new ArrayList<>();
    private ArrayList<Song> mSongs = new ArrayList<>();
    private MediaFile mCurrentPlayingFile;
    private MediaFile mLastPlayedFile;

    private AudioManager mAudioManager;
    private AudioAttributes mAudioAttributes;
    private AudioFocusRequest mAudioFocusRequest;
    private MediaPlayer mMediaPlayer;
    private Queue<Runnable> runnables;

    // Public constants
    public static final String UNKOWN = "UNKNOWN";
    // Uri source of this track
    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";
    // Sort key for this tack
    public static final String CUSTOM_METADATA_SORT_KEY = "__SORT_KEY__";

    MP3DataManager(Context context) {
        super(context);
        mMediaPlayer = new MediaPlayer();
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.MP3FILE_LIST_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
    }

    ArrayList<MediaFile> getFiles() {
        return mFiles;
    }

    List<File> getFolderList() {
        return mFolderList;
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

    private void getFilesRunnable1(final File folder) {

        synchronized (this) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<MediaFile> fileList = new ArrayList<>();
                    try {
                        final File[] files = folder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
                        for (File file : files) {
                            if (file.isDirectory()) {
                                getFilesRunnable1(file);

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
            });
        }
    }

    void getPlayList(Uri uri) {
        retrieveMedia(uri, getContext());
        /*GetMp3FilesCallable callable = new GetMp3FilesCallable(rootFolder, getContext(),mp3FileListener);
        GetMp3FutureTask task = new GetMp3FutureTask(callable, mUiThreadHandler, mp3FileListener);
        mThreadPoolExecutor.execute(task);*/
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
            this.mMp3FileListener = mMp3FileListener;
        }

        @Override
        public List<MediaFile> call() throws Exception {
            getFilesRunnable1(mRootFolder);
            return mFiles;
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

    private static final String MUSIC_SELECT_FILTER = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final String MUSIC_SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";

    private synchronized boolean retrieveMediaOld(Context context) {
        if (context.checkSelfPermission(READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Cursor cursor =
                context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MUSIC_SELECT_FILTER, null, MUSIC_SORT_ORDER);
        if (cursor == null) {
            Log.e(TAG, "Failed to retreive music: cursor is null");
            //mCurrentState = State.NON_INITIALIZED;
            return false;
        }
        if (!cursor.moveToFirst()) {
            Log.d(TAG, "Failed to move cursor to first row (no query result)");
            cursor.close();
            return true;
        }
        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        do {
            long thisId = cursor.getLong(idColumn);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            mFiles.add(new MediaFile(new File(path)));
            // Construct per feature database
        } while (cursor.moveToNext());
        cursor.close();
        return true;
    }


    private void retrieveMedia(Uri uri, Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        if (uri == null) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String sortingOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
        Cursor cursor = contentResolver.query(uri, null, null, null, sortingOrder);
        if (cursor == null) {
        } else if (!cursor.moveToFirst()) {
        } else {

            do {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mFiles.add(new MediaFile(new File(path)));

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    List<File> getMediaFolders(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {MediaStore.Audio.Media.DATA};
        String sortingOrder = MediaStore.Audio.Media.TITLE_KEY + " ASC";
        Cursor cursor = resolver.query(uri, projection, selection, null, sortingOrder);

        if (cursor != null && cursor.getCount() > 0) {
            int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            while (cursor.moveToNext()) {
                String data = cursor.getString(dataIndex);
                int i = 0;
                if (data.toLowerCase().endsWith("mp3")) {
                    int lastSlashIndex = data.lastIndexOf('/');
                    while (i < lastSlashIndex) {
                        data = data.substring(0, lastSlashIndex);
                        File folder = new File(data);
                        if (!mFolderList.contains(folder)) {
                            mFolderList.add(folder);
                        }
                        lastSlashIndex = data.lastIndexOf('/');
                    }
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return mFolderList;
    }

    ArrayList<MediaFile> retrieveMediaFromFolder(File folder, Context context) {
        ArrayList<MediaFile> files = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String sortingOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"%" + folder.getAbsolutePath() + "%"}, sortingOrder);
        if (cursor == null) {
        } else if (!cursor.moveToFirst()) {
        } else {

            do {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                files.add(new MediaFile(new File(path)));

            } while (cursor.moveToNext());
            cursor.close();
        }
        return files;
    }

    private static final class Mp3FilesFetchResult {
        DataManager.Mp3FileListener mMp3FileListener;
        List<MediaFile> mMediaFiles;

        private Mp3FilesFetchResult(List<MediaFile> mediaFiles, DataManager.Mp3FileListener mp3FileListener) {
            mMediaFiles = mediaFiles;
            mMp3FileListener = mp3FileListener;
        }
    }

    private synchronized MediaMetadata retrievMediaMetadata(long musicId, String musicPath) {
        Log.d(TAG, "getting metadata for music: " + musicPath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId);
        if (!(new File(musicPath).exists())) {
            Log.d(TAG, "Does not exist, deleting item");
            getContext().getContentResolver().delete(contentUri, null, null);
            return null;
        }
        retriever.setDataSource(getContext(), contentUri);
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = durationString != null ? Long.parseLong(durationString) : 0;
        MediaMetadata.Builder metadataBuilder =
                new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, String.valueOf(musicId))
                        //.putString(CUSTOM_METADATA_TRACK_SOURCE, musicPath)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title != null ? title : UNKOWN)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, album != null ? album : UNKOWN)
                        .putString(
                                MediaMetadata.METADATA_KEY_ARTIST, artist != null ? artist : UNKOWN)
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, duration);
        byte[] albumArtData = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (albumArtData != null) {
            bitmap = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap);
        }
        retriever.release();
        return metadataBuilder.build();
    }
}
