package com.mariodev.novelapp.notificationServices;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mariodev.novelapp.R;

import java.util.Map;
import java.util.Random;

@SuppressWarnings("ALL")
public class MyFirebaseMessagingServices extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        if (remoteMessage.getData().isEmpty()){
//            showNotification(Objects.requireNonNull(remoteMessage.getNotification()).getTitle() ,
//                    remoteMessage.getNotification().getBody());
//        }else {
//            showNotification(remoteMessage.getData());
//        }
        remoteMessage.getData();
        sendNotification(remoteMessage);



    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String , String> data = remoteMessage.getData();
        String title = data.get("title");
        String content = data.get("content");


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "com.mariodev.novelapp.NotificationServices";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID
                    , "Notification", NotificationManager.IMPORTANCE_MAX);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this
                , NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(content);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }



    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("The token refreshed:" , s);
    }
}
