package com.example.notificationscheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationJobService extends JobService {
    NotificationManager mNotifyManager;

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    private ExecutorService executor;

    @Override
    public boolean onStartJob(JobParameters params) {
        Handler handler = new Handler(Looper.getMainLooper());
        executor = Executors.newFixedThreadPool(1);
        executor.submit(() -> {
            boolean jobFinished = true;
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.d("Thread", "Thread destroyed");
                jobFinished = false;
            }
            boolean finalJobFinished = jobFinished;
            handler.post(() -> {
                if(finalJobFinished){
                    createNotificationChannel();

                    PendingIntent contentPendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            new Intent(getApplicationContext(), MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                            getApplicationContext(), PRIMARY_CHANNEL_ID)
                            .setContentTitle("Job Service")
                            .setContentText("Your Job ran to completion")
                            .setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_job_running)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setAutoCancel(true);

                    mNotifyManager.notify(0, builder.build());
                    Toast.makeText(getApplicationContext(), "Job Complete", Toast.LENGTH_SHORT)
                            .show();
                }
                jobFinished(params, !finalJobFinished);
            });
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (executor != null) {
            executor.shutdownNow();
        }
        Toast.makeText(getApplicationContext(), "Job Failed", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Job Service notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Job Service");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }
}
