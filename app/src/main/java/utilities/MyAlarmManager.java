package utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import databaseBean.DBHelper;

public class MyAlarmManager extends BroadcastReceiver {
    private int userId;
    public static final int REQUEST_CODE = 88888;
    public MyAlarmManager() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if(bundle == null)
            return;

        if(bundle.getInt(PeriodicTransactionService.USER_ID_PARAM) != 0)
            this.userId = bundle.getInt(PeriodicTransactionService.USER_ID_PARAM);
        else
            return;

        Intent i = new Intent(context, PeriodicTransactionService.class);
        i.setAction(PeriodicTransactionService.ACTION);
        Bundle bundle1 = new Bundle();
        bundle1.putInt(PeriodicTransactionService.USER_ID_PARAM, userId);
        i.putExtras(bundle1);
        context.startService(i);
    }
}
