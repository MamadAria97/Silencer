package com.example.silencer.classes;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;

import com.example.silencer.activity.MainActivity;

public class SilentModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enableSilent = intent.getBooleanExtra("enableSilent", false);
        int timerId = intent.getIntExtra("timerId", -1);
        String day = intent.getStringExtra("day");

        if (timerId == -1) return;
        if (day == null) return;

        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (today != MainActivity.getDayOfWeek(day)) {
            return;
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentTimeCode = hour * 100 + minute;


        if (enableSilent && currentTimeCode != timerId) {

            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            if (enableSilent) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
    }


}






