package features;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.aub.oltranz.payfuel.R;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import appBean.AsyncResponce;
import appBean.LogoutResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.SellingTransaction;
import models.LogoutData;
import models.MapperClass;
import models.TransactionPrint;

public class LogoutService extends Service {
    String tag="PayFuel: "+getClass().getSimpleName();
    DBHelper db;
    String url;
    int userId;
    Context context=this;
    public LogoutService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "LogoutService Service is Started");
        db = new DBHelper(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(tag, "Service Destroyed");
    }

    public void stopService() {
        Log.e(tag, "Service Self Destroyed");
        try{
            // unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        this.stopSelf();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(tag, "Service report device low memory");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "Service Received Start Command");
        try{
            db.deleteOldTransaction();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(intent != null && intent.getExtras() != null){
            try {
                Bundle extras = intent.getExtras();
                LogoutData ld = new LogoutData();
                ld.setUserId(extras.getInt("userId"));
                ld.setDevId(extras.getString("deviceNo"));

                userId=ld.getUserId();

                String logoutData=new MapperClass().mapping(ld);
                Log.d(tag,"Logging out with those data: "+logoutData);
                url=getResources().getString(R.string.logouturl);

                //run the logout
                Logout lg=new Logout();
                lg.execute(logoutData);
               // HandleUrl handleUrl = new HandleUrl(this, this, getResources().getString(R.string.logouturl), getResources().getString(R.string.post), new MapperClass().mapping(ld));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

        @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //___________________LogOut__________________________\\
    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag, "Transaction checking starts, background activity");
            String data = params[0];
            try {
                //_____________Opening connection and post data____________//
                URL oURL = new URL(url);
                HttpURLConnection con = (HttpURLConnection) oURL.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "Application/json; charset=UTF-8");


                con.setDoOutput(true);
                con.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());

                wr.writeBytes(data);
                wr.flush();
                wr.close();
                System.out.println("Data to post :" + data);
                BufferedReader in1 = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }
                in1.close();
                con.disconnect();
                return response.toString();

            }  catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            Log.d(tag,"Checking Transaction Status server Result: \n"+result);

            if(result==null){
                Log.e(tag,"Error Occurred, During Checking Transaction Status");
            }else {
                Log.d(tag, "Response redirected to DeviceRegistrationResponse");
                ObjectMapper mapper = new ObjectMapper();

                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                try {
                    LogoutResponse lr =mapper.readValue(result,LogoutResponse.class);
                    Log.d(tag,"mapped Object is: "+lr.getClass().getSimpleName());

                    if(lr.getStatusCode() != 100)
                        uiFeedBack(lr.getMessage());
                    else{
                        long log=0;
                        //delete Work Status
                        db.deleteStatusByUser(userId);

                        //delete user
                        // db.truncateTransactions();
                        Logged_in_user user=new Logged_in_user();
                        user.setLogged(0);
                        log=db.updateUser(user);
                        Log.v(tag, "User log status 0: " + log);
                        db.deleteUser(userId);
                        db.deleteStatusByUser(userId);

                        //removing shared preferences
                        PreferenceManager prefs=new PreferenceManager(context);
                        if(!prefs.deletePrefs()){
                            Log.e(tag,"Deleting Shared preference failed");
                            uiFeedBack("Deleting Shared preference failed");
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                    uiFeedBack(e.getMessage());
                }

            }

            uiFeedBack("Logout Task Accomplished");
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    public void uiFeedBack(String message){
        Log.e(tag, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        serviceDone();
    }

    public void serviceDone(){
        Log.d(tag,"Service Job Accomplished");
        stopService();
    }
}
