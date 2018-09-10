package com.sample.nikhil.foregroundservice.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.activities.FolderActivity;
import com.sample.nikhil.foregroundservice.data.DataManager;

import java.io.File;
import java.util.List;

import static com.sample.nikhil.foregroundservice.utils.Constants.INTENT_STRING_FOLDER;

/**
 * Created by Nikhil on 12-08-2018.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private Context mContext;
    private List<File> mFolders;
    private static final String TAG = "FolderAdapter";
    private MediaPlayer mMediaPlayer;
    private DataManager mDataManager;


    public FolderAdapter(Context context, List<File> folders) {
        this.mContext = context;
        this.mFolders = folders;
        mDataManager = DataManager.getInstance();
        mMediaPlayer = mDataManager.getMediaPlayer();

    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FolderViewHolder holder, final int position) {
        final File folder = mFolders.get(position);
        holder.textviewTitle.setText(folder.getName());
        holder.textviewSubtitle.setText(folder.getAbsolutePath());
    }

    @Override
    public int getItemCount() {
        return mFolders.size();
    }

    public void setFolders(List<File> folders) {
        mFolders = folders;
    }

    class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textviewTitle;
        TextView textviewSubtitle;
        ImageView imageview;

        FolderViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            textviewTitle = view.findViewById(R.id.textview_title);
            textviewSubtitle = view.findViewById(R.id.textview_subtitle);
            imageview = view.findViewById(R.id.imageview);
        }

        @Override
        public void onClick(View view) {
            Intent intent=new Intent(mContext, FolderActivity.class);
            intent.putExtra(INTENT_STRING_FOLDER,mFolders.get(getLayoutPosition()));
            mContext.startActivity(intent);
        }
    }
}
