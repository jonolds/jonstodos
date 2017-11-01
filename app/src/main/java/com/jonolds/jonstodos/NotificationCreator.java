package com.jonolds.jonstodos;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationCreator extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String[] itd = (intent.getStringExtra("idTitleDesc")).split("___");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.check)
                .setContentTitle(itd[1])
                .setContentText(itd[2])
                .setAutoCancel(true);
        System.out.println(itd[2]);
        // Create an Intent to start the activity for the MainActivity
        Intent alarmIntent = new Intent(context,MainActivity.class);
        // Create a back stack builder object
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Create the parent stack (none in this case)
        stackBuilder.addParentStack(MainActivity.class);
        // Add the MainActivity as the last Intent
        stackBuilder.addNextIntent(alarmIntent);
        //Create a PendingIntent from the stackBuilder to create a new Task with the activity
        //to be launched at the top
        PendingIntent alarmPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the onClick() method for the Notification to the PendingIntent created
        mBuilder.setContentIntent(alarmPendingIntent);
        //Get an Instance of the system's NotificationManager object
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Integer.parseInt(itd[0]), mBuilder.build());
    }
}
