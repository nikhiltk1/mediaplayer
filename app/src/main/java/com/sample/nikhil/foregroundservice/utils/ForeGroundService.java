package com.sample.nikhil.foregroundservice.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sample.nikhil.foregroundservice.R;
import com.sample.nikhil.foregroundservice.activities.MainActivity;
import com.sample.nikhil.foregroundservice.data.model.MediaFile;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.sample.nikhil.foregroundservice.utils.Constants.INTENT_STRING_CLICKED_PLAYING_POSITION;

/**
 * Created by Nikhil on 08-07-2018.
 */

public class ForeGroundService extends Service {
    private MediaPlayer mMediaPlayer;
    private ArrayList<MediaFile> mMediaFiles;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(getApplicationContext().getExternalMediaDirs()[0].getPath()));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, "")
                .setContentTitle("Title")
                .setContentText("Message")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker("Ticker text")
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaFiles = (ArrayList<MediaFile>) intent.getExtras().getSerializable(Constants.INTENT_STRING_PLAYING_LIST);
        int position = intent.getExtras().getInt(INTENT_STRING_CLICKED_PLAYING_POSITION);
        if (null != intent.getAction())
            if (intent.getAction().equals("ACTION.STOPFOREGROUND_ACTION")) {
                stopForeground(true);
                stopSelf();
            }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("ForeGroundService", "Hellooo");
            }
        }, 0, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
