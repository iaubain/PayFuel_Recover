package features;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Owner on 6/20/2016.
 */
public class PreferenceManager {
    public static final String myPrefs = "PreferenceManager" ;
    public static final String userIdKey = "userIdKey";
    public static final String checkKey = "checkKey";
    private static Context context;
    private static long userId;
    private static boolean prefsCheck=true;
    String tag="PayFuel: "+getClass().getSimpleName();
    SharedPreferences sharedpreferences;

    public PreferenceManager(Context context){
        Log.d(tag,"Preference Initiated");
        PreferenceManager.context =context;
    }

    public boolean createPreference (int userId){
        Log.d(tag,"Creating Sharing Preference");
        PreferenceManager.userId =userId;
        sharedpreferences = context.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedpreferences.edit();

        editor.putInt(userIdKey, userId);
        editor.putBoolean(checkKey, prefsCheck);
        editor.commit();

        return true;
    }

    public boolean isPrefsCheck(){
        Log.d(tag,"Check if the preferences are there");
        if(sharedpreferences !=null){
            return sharedpreferences.getBoolean(checkKey,false);
        }
        return false;
    }

    public boolean deletePrefs(){
        Log.d(tag,"Delete Preferences");
        sharedpreferences = context.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedpreferences.edit();
        editor.clear();
        editor.commit();

        return true;
    }

    public SharedPreferences getPrefs(){
        Log.d(tag,"Getting Shared Preferences");
        return sharedpreferences;
    }
}
