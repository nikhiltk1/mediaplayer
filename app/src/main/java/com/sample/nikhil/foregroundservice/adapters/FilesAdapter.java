package com.sample.nikhil.foregroundservice.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.nikhil.foregroundservice.MediaFile;
import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.data.DataManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikhil on 12-08-2018.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    private Context mContext;
    private List<MediaFile> mFiles;
    private static final String TAG = "FilesAdapter";
    private MediaPlayer mMediaPlayer;
    private DataManager mDataManager;


    public FilesAdapter(Context context, ArrayList<MediaFile> files) {
        this.mContext = context;
        this.mFiles = files;
        mDataManager = DataManager.getInstance();
        mMediaPlayer = mDataManager.getMediaPlayer();

    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_files, parent, false);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilesViewHolder holder, final int position) {
        final MediaFile mediaFile = mFiles.get(position);
        holder.textviewTitle.setText(mediaFile.getName());
        String artistName = mediaFile.getMediaMetadataRetriever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                != null ? mediaFile.getMediaMetadataRetriever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                : mContext.getString(R.string.unknown_artist);
        String albumName = mediaFile.getMediaMetadataRetriever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                != null ? mediaFile.getMediaMetadataRetriever().extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                : mContext.getString(R.string.unknown_album);
        String subtitle = artistName + " | " + albumName;
        holder.textviewTitle.setText(mediaFile.getName());
        holder.textviewSubtitle.setText(subtitle);
        byte[] data = mediaFile.getMediaMetadataRetriever().getEmbeddedPicture();
        Bitmap bm = null;
        if (data != null) {
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        if (bm != null) {
            holder.imageview.setImageBitmap(bm);
        } else {
            holder.imageview.setBackgroundColor(mContext.getColor(R.color.colorPrimary));
        }
        if (null != mDataManager.getCurrentPlayingFile() && mDataManager.getCurrentPlayingFile().equals(mediaFile) && mDataManager.getMediaPlayer().isPlaying()) {
            rotateView(holder.imageview);
        }
        holder.imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (null != mDataManager.getCurrentPlayingFile() && mDataManager.getCurrentPlayingFile().equals(mediaFile)) {
                    mMediaPlayer.pause();
                    mDataManager.removeAudioFocus();
                    mDataManager.setLastPlayedFile(mediaFile);
                    mDataManager.setCurrentPlayingFile(null);
                } else if (null != mDataManager.getLastPlayedFile() && mDataManager.getLastPlayedFile().equals(mediaFile)) {
                    int res = mDataManager.getAudioFocus();
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mMediaPlayer.start();
                        mDataManager.setCurrentPlayingFile(mediaFile);
                    }
                } else {
                    try {
                        int res = mDataManager.getAudioFocus();
                        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            mMediaPlayer.reset();
                            Uri myUri = Uri.fromFile(mediaFile); // initialize Uri here
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mMediaPlayer.setDataSource(mContext, myUri);
                            mMediaPlayer.prepare();
                            mMediaPlayer.start();
                            mDataManager.setCurrentPlayingFile(mediaFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                notifyItemChanged(position);
            }
        });

        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {

            }
        });

        mDataManager.getMediaPlayer().setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                mMediaPlayer.release();
                mDataManager.setCurrentPlayingFile(null);
                mDataManager.removeAudioFocus();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void setMp3Files(List<MediaFile> mediaFiles){
        mFiles=mediaFiles;
    }
    public class FilesViewHolder extends RecyclerView.ViewHolder {

        TextView textviewTitle;
        TextView textviewSubtitle;
        ImageView imageview;

        public FilesViewHolder(View view) {
            super(view);
            textviewTitle = view.findViewById(R.id.textview_title);
            textviewSubtitle = view.findViewById(R.id.textview_subtitle);
            imageview = view.findViewById(R.id.imageview);
        }
    }

    private void rotateView(ImageView view) {
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(5000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
        view.startAnimation(rotate);
    }
}
