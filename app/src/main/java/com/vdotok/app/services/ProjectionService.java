package com.vdotok.app.services;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.vdotok.app.R;


public class ProjectionService extends Service {

    private final IBinder mBinder = new LocalBinder();


    public ProjectionService() {
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void startForeground() {

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = "";
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);

        Notification notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ProjectionService")
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(100, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {

        NotificationChannel channel1 = new NotificationChannel("networkService", "My Background Service", NotificationManager.IMPORTANCE_DEFAULT);
        channel1.setLightColor(Color.BLUE);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel1);
        return "networkService";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class LocalBinder extends Binder {
        public ProjectionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ProjectionService.this;
        }
    }
}
