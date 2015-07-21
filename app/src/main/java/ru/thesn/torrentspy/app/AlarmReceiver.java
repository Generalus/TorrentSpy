package ru.thesn.torrentspy.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ru.thesn.torrentspy.app.download.DownloadService;


public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        boolean restart = "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) &&
                MyApplication.mSettings.getBoolean(MyApplication.APP_PREFERENCES_BACKGROUND_CHECKS, true) &&
                MyApplication.mSettings.getBoolean(MyApplication.APP_PREFERENCES_CHECK_AFTER_RESTART, true);

        boolean startApp = "ru.thesn.torrentspy.app.START_AlarmReceiver".equals(intent.getAction()) &&
                MyApplication.mSettings.getBoolean(MyApplication.APP_PREFERENCES_BACKGROUND_CHECKS, true);

        if (restart || startApp){
            Log.e("DEV_", "AlarmReceiver start");
            Intent alarmIntent = new Intent(context, DownloadService.class);
            MyApplication.isReceiverAlive = true;
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long interval = AlarmManager.INTERVAL_DAY / MyApplication.mSettings.getInt(MyApplication.APP_PREFERENCES_NUMBER_OF_TIMES, 1);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, interval, interval, pendingIntent);
            Log.i("DEV_", "Alarm Set");
        }

    }
}