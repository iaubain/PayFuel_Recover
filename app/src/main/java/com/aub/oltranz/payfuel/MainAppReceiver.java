package com.aub.oltranz.payfuel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class MainAppReceiver extends BroadcastReceiver {
    String tag="PayFuel:"+getClass().getSimpleName();
    public MainAppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(tag,"Broad Cast Message Received");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d(tag, "Boot Complete Broad Cast Received");

            Calendar cal = Calendar.getInstance();
            Intent alarmIntent = new Intent(context, AppMainService.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //clean alarm cache for previous pending intent
            alarm.cancel(pintent);
            // schedule for every 4 seconds
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 4 * 1000, pintent);

            Intent i1=new Intent(context,Home.class);
            i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i1);
        }
    }
}
