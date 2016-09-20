package features;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Created by Owner on 6/21/2016.
 */
public class ServiceCheck {
    String tag="PayFuel:"+getClass().getSimpleName();
    Context context;

    public ServiceCheck(Context context) {
        Log.d(tag,"Check Service initiated");
        this.context = context;
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        Log.d(tag,"Check if app service is running");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
