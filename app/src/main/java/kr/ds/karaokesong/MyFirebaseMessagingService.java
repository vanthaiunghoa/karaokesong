/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.ds.karaokesong;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import kr.ds.utils.DsObjectUtils;
import kr.ds.utils.Helper;
import kr.ds.utils.SharedPreference;
import kr.ds.utils.UniqueID;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        int badgerCount = SharedPreference.getIntSharedPreference(getApplicationContext(), "badger_count");
        badgerCount++;
        SharedPreference.putSharedPreference(getApplicationContext(),"badger_count",badgerCount);
        ShortcutBadger.applyCount(getApplicationContext(), badgerCount);

        String message = remoteMessage.getData().get("msg");
        String image_url = remoteMessage.getData().get("image_url");
        String push_type = remoteMessage.getData().get("push_type");

        if (!DsObjectUtils.getInstance(this).isEmpty(push_type)) {
            if(push_type.matches("down")){
                sendDownNotification(message);
            }else{
                if (!DsObjectUtils.getInstance(this).isEmpty(image_url)) {
                    Bitmap bitmap = Helper.tryToGetBitmapFromInternet(image_url, this, -1);
                    sendNotification(message, bitmap);
                } else {
                    sendNotification(message);
                }
            }
        }else{
            if(!DsObjectUtils.getInstance(this).isEmpty(image_url)){
                Bitmap bitmap = Helper.tryToGetBitmapFromInternet(image_url, this, -1);
                sendNotification(message,  bitmap);
            }else{
                sendNotification(message);
            }
        }
    }

    private void sendDownNotification(String message) {
        Intent i = new Intent(getApplicationContext(), DownActivity.class);
        Bundle b = new Bundle();
        b.putString("text", message);
        i.putExtras(b);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    private void sendNotification(String message, Bitmap bitmap) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.channel_message_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        if(bitmap != null){
            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
            style.setBigContentTitle(getString(R.string.app_name));
            style.setSummaryText(message);
            style.bigPicture(bitmap);
            notificationBuilder.setStyle(style);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,getString(R.string.channel_message_id), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.channel_message));
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(UniqueID.getRandomNumber(1000), notificationBuilder.build());
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.channel_message_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,getString(R.string.channel_message_id), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.channel_message));
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(UniqueID.getRandomNumber(1000), notificationBuilder.build());
    }
}