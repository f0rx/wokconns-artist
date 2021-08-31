package com.wokconns.wokconns;
/**
 * Created by VARUN on 01/01/19.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.BaseActivity;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    SharedPrefs prefrence;
    int i = 0;

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefrence = SharedPrefs.getInstance(this);

        Log.e(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
        }



/*
        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().containsKey("title")) {
                if (remoteMessage.getData().get("title").equalsIgnoreCase("Job")) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), 1);
                } else if (remoteMessage.getData().get("title").equalsIgnoreCase("Chat")) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Consts.BROADCAST);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    i = prefrence.getIntValue("Value");
                    i++;
                    prefrence.setIntValue("Value", i);

                    sendNotification(getValue(remoteMessage.getData(), "body"), 1);
                }else {
                    sendNotification(getValue(remoteMessage.getData(), "body"), 0);
                }
            }

        }
*/

        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().containsKey(Const.TYPE)) {
                if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.CHAT_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.CHAT_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.TICKET_COMMENT_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.TICKET_COMMENT_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.TICKET_STATUS_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.TICKET_STATUS_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.WALLET_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.WALLET_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.DECLINE_BOOKING_ARTIST_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.DECLINE_BOOKING_ARTIST_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.START_BOOKING_ARTIST_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.START_BOOKING_ARTIST_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.BRODCAST_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.BRODCAST_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.ADMIN_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.ADMIN_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.BOOK_ARTIST_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.BOOK_ARTIST_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.JOB_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.JOB_NOTIFICATION);
                } else if (remoteMessage.getData().get(Const.TYPE).equalsIgnoreCase(Const.DELETE_JOB_NOTIFICATION)) {
                    sendNotification(getValue(remoteMessage.getData(), "body"), Const.DELETE_JOB_NOTIFICATION);
                } else {
                    sendNotification(getValue(remoteMessage.getData(), "body"), "");
                }
            }

        }


    }

    public String getValue(Map<String, String> data, String key) {
        try {
            if (data.containsKey(key))
                return data.get(key);
            else
                return getString(R.string.app_name);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getString(R.string.app_name);
        }
    }

    @Override
    public void onNewToken(String token) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Const.DEVICE_TOKEN, token);
        editor.commit();
        SharedPreferences userDetails = MyFirebaseMessagingService.this.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.d(TAG, "Refreshed token: " + token);

    }

    private void sendNotification(String messageBody, String tag) {

//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(tag);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra(Const.SCREEN_TAG, tag);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        Uri defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FabArtist")
                .setSound(defaultSoundUri)
                /*.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))*/
                .setContentText(messageBody).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }


}

