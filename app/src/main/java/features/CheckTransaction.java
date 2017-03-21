package features;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.aub.oltranz.mysppayfuel.R;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import appBean.CheckTransactionResponse;
import appBean.MyTransactionBean;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.SellingTransaction;
import models.MapperClass;
import models.TransactionToCheck;

public class CheckTransaction extends Service {
    public static final String myPrefs = "PreferenceManager" ;
    public static final String userIdKey = "userIdKey";
    public static String url;
    String tag="PayFuel: "+getClass().getSimpleName();
    DBHelper db;
    PreferenceManager prefs;
    int userId;
    Context context=this;
    IntentFilter intentFilterilter;
    BroadcastReceiver broadcastReceiver;


    boolean check=false;
    private Boolean serviceRunning = false;

    public CheckTransaction() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "CheckTransaction Service is Started");
        db = new DBHelper(this);
        prefs=new PreferenceManager(this);

//        if(! prefs.isPrefsCheck()){
//            stopService();
//        }else{
        SharedPreferences prefs=getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        userId=prefs.getInt(userIdKey, 0);
        if(userId==0){
            stopService();
        }else{
            url=getResources().getString(R.string.mytransactions);
            setServiceRunning(true);
        }
        //}

//        if(!checkBroadcastRegister()){
//            register();
//        }
    }

    public boolean checkBroadcastRegister(){
        return check;
    }
    public boolean register(){
        try {
            intentFilterilter=new IntentFilter("com.aub.oltranz.payfuel.MAIN_SERVICE");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Handle the received Intent message
                    String msg = intent.getStringExtra("msg");
                    if(msg.equalsIgnoreCase("check_service")){
                        if(getServiceRunning()){

                        }
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilterilter);
            unregisterReceiver(broadcastReceiver);
            check=true;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            check=false;
            return false;
        }
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
        Log.e(tag,"Service Received Start Command");

        int loggedCount=db.getLoggedUserCount();
        if(loggedCount<=0){
            Log.e(tag,"No Logged User Available");
            stopService();
            Calendar cal = Calendar.getInstance();
            Intent alarmIntent = new Intent(context, CheckTransaction.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //clean alarm cache for previous pending intent
            alarm.cancel(pintent);
        }else{

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String now= dateFormat.format(new Date());

            Log.v(tag, "Report date is: " + now);

            MyTransactionBean mtb=new MyTransactionBean();
            mtb.setUserId(userId);
            mtb.setDeviceTransactionDate(now);
            CheckTrans ct=new CheckTrans();
            try{ct.execute(new MapperClass().mapping(mtb));}catch (Exception e){e.printStackTrace();}
        }
        //Toast.makeText(this,"Task Accomplished:",Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    public Boolean getServiceRunning() {
        return serviceRunning;
    }

    public void setServiceRunning(Boolean serviceRunning) {
        this.serviceRunning = serviceRunning;
    }

    public void broadcast(){
        //broadcast a refresh command. Sync finished
        Log.v(tag,"Synchronisation finished, Sending a refresh Broadcast Command");
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh_check");
        sendBroadcast(i);
    }

    //___________________Check Transaction__________________________\\
    private class CheckTrans extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag,"Transaction checking starts, background activity");
            String transData = params[0];
            try {
                //_____________Opening connection and post data____________//
                URL oURL = new URL(url);
                HttpURLConnection con = (HttpURLConnection) oURL.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "Application/json; charset=UTF-8");


                con.setDoOutput(true);
                con.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());

                wr.writeBytes(transData);
                wr.flush();
                wr.close();
                System.out.println("Data to post :" + transData);
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
            }else{
                Log.d(tag,"Response redirected to DeviceRegistrationResponse");
                ObjectMapper mapper= new ObjectMapper();

                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                try {
                    CheckTransactionResponse ctr =mapper.readValue(result,CheckTransactionResponse.class);
                    Log.d(tag,"mapped Object is: "+ctr.getClass().getSimpleName());

                    if(ctr.getStatusCode() == 100){
                        for(TransactionToCheck ttc: ctr.getTransactions()){
                            if(ttc != null){
                                if(userId == ttc.getUserId()){
                                    //check Local database transaction
                                    SellingTransaction st=db.getSingleTransaction(ttc.getDeviceTransactionId());
                                    if((st.getStatus() == 100 || st.getStatus() == 101) && ttc.getPaymentStatus().equalsIgnoreCase("FAILURE")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag,"Updating Nozzle: "+nozzle.getNozzleId()+" Indexes to: "+ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status to: " + 500);
                                        //updating transaction status
                                        st.setStatus(500);
                                        db.updateTransaction(st);
//                                        broadcast();
                                    }else if((st.getStatus() == 100 || st.getStatus() == 101) && ttc.getPaymentStatus().equalsIgnoreCase("PENDING")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag, "Updating Nozzle Indexes to: " + ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status to: " + 301+" / "+302);
                                        //updating transaction status
                                        if(st.getStatus()==100)
                                            st.setStatus(301);
                                        else
                                            st.setStatus(302);
                                        db.updateTransaction(st);

                                        //check in Asynchronous Transactions
                                        AsyncTransaction at=db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
                                        if(at==null){
                                            Log.v(tag, "Creating a pending transaction: " + st.getDeviceTransactionId());
                                            at.setDeviceTransactionId(st.getDeviceTransactionId());
                                            at.setSum(0);
                                            at.setUserId(userId);
                                            at.setBranchId(st.getBranchId());
                                            at.setDeviceId(st.getDeviceNo());
                                            db.createAsyncTransaction(at);

                                            //make alarm for this pending Transaction
                                        }else{
                                            Log.v(tag, "This pending transaction: " + st.getDeviceTransactionId()+" Existed");
                                        }
//                                        broadcast();
                                    }else if((st.getStatus() == 301 || st.getStatus() == 302) && ttc.getPaymentStatus().equalsIgnoreCase("SUCCESS")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag,"Updating Nozzle Indexes to: "+ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status to: " + 100+" / "+101);
                                        //updating transaction status
                                        if(st.getStatus()==301)
                                            st.setStatus(100);
                                        else {
                                            st.setStatus(101);
                                            //print the receipt
                                        }
                                        db.updateTransaction(st);

                                        //check in Asynchronous Transactions
                                        AsyncTransaction at=db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
                                        if(at != null){
                                            Log.v(tag, "Deleting a pending transaction: " + st.getDeviceTransactionId());
                                            db.deleteAsyncTransaction(st.getDeviceTransactionId());
                                        }else{
                                            Log.v(tag, "This pending transaction: " + st.getDeviceTransactionId()+" Doesn't Exist (Okay)");
                                        }
//                                        broadcast();
                                    }else if((st.getStatus() == 301 || st.getStatus() == 302) && ttc.getPaymentStatus().equalsIgnoreCase("FAILURE")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag, "Updating Nozzle Indexes to: " + ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status to: " + 500);
                                        //updating transaction status
                                       st.setStatus(500);
                                        db.updateTransaction(st);

                                        //check in Asynchronous Transactions
                                        AsyncTransaction at=db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
                                        if(at != null){
                                            Log.v(tag, "Deleting a pending transaction: " + st.getDeviceTransactionId());
                                            db.deleteAsyncTransaction(st.getDeviceTransactionId());
                                        }else{
                                            Log.v(tag, "This pending transaction: " + st.getDeviceTransactionId()+" Doesn't Exist (Okay)");
                                        }
//                                        broadcast();
                                    }else if((st.getStatus() == 500 || st.getStatus() == 501) && ttc.getPaymentStatus().equalsIgnoreCase("PENDING")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag,"Updating Nozzle Indexes to: "+ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status "+st.getStatus()+" Status to: " + 301);
                                        Log.v(tag,">>>>the Transaction TO check Payment Status: "+ ttc.getPaymentStatus());
                                        //updating transaction status
                                        st.setStatus(301);
                                        db.updateTransaction(st);

                                        //check in Asynchronous Transactions
                                        AsyncTransaction at=db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
                                        if(at == null){
                                            Log.v(tag, "Creating a pending transaction: " + st.getDeviceTransactionId());
                                            at.setDeviceTransactionId(st.getDeviceTransactionId());
                                            at.setSum(0);
                                            at.setUserId(userId);
                                            at.setBranchId(st.getBranchId());
                                            at.setDeviceId(st.getDeviceNo());
                                            db.createAsyncTransaction(at);

                                            //Make alarm
                                        }else{
                                            Log.v(tag, "This pending transaction: " + st.getDeviceTransactionId()+" Existed");
                                        }
//                                        broadcast();
                                    }else if((st.getStatus() == 500 || st.getStatus() == 501) && ttc.getPaymentStatus().equalsIgnoreCase("SUCCESS")){
                                        //decrease nozzle indexes
                                        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
                                        nozzle.setNozzleIndex((double) ttc.getIndexafter());
                                        Log.v(tag,"Updating Nozzle Indexes to: "+ttc.getIndexafter());
                                        //Updating Nozzle indexes
                                        db.updateNozzle(nozzle);

                                        Log.v(tag, "Updating Transaction Status to: " + 100);
                                        //updating transaction status
                                        st.setStatus(100);
                                        db.updateTransaction(st);

//                                        broadcast();
                                    }else{
                                        Log.v(tag,"This Transaction: "+st.getDeviceTransactionId()+" Is Okay");
                                    }
                                }else{
                                    Log.e(tag,"Transaction User Id don't match Logged User Id");
                                }
                            }else{
                                Log.e(tag,"Null Transaction To Check");
                            }
                        }
                        Log.v(tag,"Synchronisation finished.");
                        stopService();
                    }else{
                        Log.e(tag,""+ctr.getMessage());
                        stopService();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(tag,"Exit With Error:"+e.getMessage());
                }


            }
            //Delete on time out
//            db.deletequeue(Long.parseLong(jo.get("traId").toString()));
//            Intent intent = new Intent("com.example.owner.petrolmanager.PRINTING").putExtra("msg", "Time Out for " + jo.get("traId").toString());
//            sendBroadcast(intent);
            broadcast();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}
