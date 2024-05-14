package edu.cuhk.languee;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyBroaodcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";
    private static final String CHANNEL_ID = "CHANNEL_ID_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MyBroadcastReceiver", "onReceive");
        showNotification(context, intent);
    }

    private boolean checkPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("MyBroadcastReceiver", "Notification permission denied");

            return false;
        }
        return true;
    }

    private void showNotification(Context context, Intent intent) {

        String title = intent.getStringExtra("notificationTitle");
        String contentText = intent.getStringExtra("notificationContentText");

        // Create an explicit intent for an Activity in your app
        // Users can tap the notification to open your app
        // The flag FLAG_ACTIVITY_CLEAR_TOP is used to make sure that the activity is not created multiple times
        Intent newIntent = new Intent(context, MainActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (checkPermission(context)) {
            notificationManager.notify(1, builder.build());
        }
    }
}
