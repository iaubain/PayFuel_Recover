package com.aub.oltranz.payfuel;

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
import android.text.TextUtils;
import android.util.Log;

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
import java.util.Calendar;
import java.util.List;

import appBean.AsyncResponce;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import features.PreferenceManager;
import features.PrintHandler;
import models.MapperClass;
import models.TransactionPrint;
import modules.PostPendingTransactionModule;

public class AppMainService extends Service {
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

    public AppMainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "Main Service is Started");
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
                url=getResources().getString(R.string.checktransaction);
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
        Log.e(tag, "Service Received Start Command");

        int loggedCount=db.getLoggedUserCount();
        if(loggedCount<=0){
            Log.e(tag, "No Logged User Available");
            Calendar cal = Calendar.getInstance();
            Intent alarmIntent = new Intent(context, AppMainService.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //clean alarm cache for previous pending intent
            alarm.cancel(pintent);
            stopService();
        }else{
            if(userId==0){
                Log.e(tag, "No Logged User with an ID 0");
                Calendar cal = Calendar.getInstance();
                Intent alarmIntent = new Intent(context, AppMainService.class);
                PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                //clean alarm cache for previous pending intent
                alarm.cancel(pintent);

                stopService();
            }

            List<AsyncTransaction> asyncTransactions= db.getAllAsyncTransactions(userId);
            if(!asyncTransactions.isEmpty()){
                for(AsyncTransaction asyncTransaction : asyncTransactions){
                    Log.d(tag,"Async Data To Check On Server \n"+new MapperClass().mapping(asyncTransaction));
                    try {
                        if(asyncTransaction.getSum() <= 40){
                            asyncTransaction.setSum(asyncTransaction.getSum()+1);
                            db.updateAsyncTransaction(asyncTransaction);
                            new CheckTrans().execute(asyncTransaction);
                        }else{
                            db.deleteAsyncTransaction(asyncTransaction.getDeviceTransactionId());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else{
                stopService();
            }

//            List<SellingTransaction> sellingTransactions=db.getAllUnsuccessfulTransaction(userId);
//        if(!sellingTransactions.isEmpty()){
//            for(SellingTransaction sts: sellingTransactions){
//
//                    AsyncTransaction at=db.getSingleAsyncPerTransacton(sts.getDeviceTransactionId());
//                Log.d(tag,"Async Data \n"+new MapperClass().mapping(at));
//                if(at != null){
//                    if(at.getSum()<=30){
//                        Log.d(tag,"Async Data To Check On Server \n"+new MapperClass().mapping(at));
//                        at.setSum(at.getSum()+1);
//                        db.updateAsyncTransaction(at);
//                        new CheckTrans().execute(new MapperClass().mapping(at));
//                    }else{
//                        Log.d(tag,"Async Data Deleted \n"+new MapperClass().mapping(at));
//                        db.deleteAsyncTransaction(sts.getDeviceTransactionId());
//                        sts.setStatus(500);
//                        db.updateTransaction(sts);
//                    }
//                }else
//                    stopService();
//            }

//            for(AsyncTransaction at:ats){
//                MapperClass mc=new MapperClass();
//                String asyncData=mc.mapping(at);
//                if(!TextUtils.isEmpty(asyncData)&&(!TextUtils.isEmpty(url)) ){
//                    CheckTrans trans = new CheckTrans();
//                    if(at.getSum()<=1200) {//revert it to near 20
////                        SellingTransaction st=db.getSingleTransaction(at.getDeviceTransactionId());
////                        if(st.getStatus()==500 || st.getStatus()==100){
////                            db.deleteAsyncTransaction(at.getDeviceTransactionId());
////                        }
////                        else
//                            trans.execute(asyncData);
//                    }
////                    else{
////                        db.deleteAsyncTransaction(at.getDeviceTransactionId());
////
////                        //transaction TimeOut
////                        SellingTransaction st=db.getSingleTransaction(at.getDeviceTransactionId());
////                        if(st.getStatus()==301 || st.getStatus()==302)
////                            st.setStatus(500);
////                        db.updateTransaction(st);
////                    }
//                }
//            }
//        }else{
//            stopService();
//        }
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

    //___________________Check Transaction__________________________\\
    private class CheckTrans extends AsyncTask<AsyncTransaction, String, String> {

        AsyncTransaction asyncTransaction;
        @Override
        protected String doInBackground(AsyncTransaction... params) {
            Log.d(tag,"Transaction checking starts, background activity");
            asyncTransaction = params[0];
            String transData = new MapperClass().mapping(params[0]);
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
                StringBuilder response = new StringBuilder();

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }
                in1.close();
                con.disconnect();
                return response.toString();

            } catch (Exception e){
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
                    AsyncResponce ar =mapper.readValue(result,AsyncResponce.class);
                    Log.d(tag,"mapped Object is: "+ar.getClass().getSimpleName());

                    //do something with AsyncResponse
                    final AsyncTransaction at=ar.getAsyncTransaction();
                    if(at==null){
                        Log.e(tag,"Null result from server");
                        //post then a transaction on the server
                        try{
                            PostPendingTransactionModule postPendingTransactionModule = new PostPendingTransactionModule(context, db.getSingleTransaction(asyncTransaction.getDeviceTransactionId()));
                            postPendingTransactionModule.startPosting();
                        }catch (Exception e){
                            Log.e(tag, "Error: "+e.getMessage());
                        }
                    }else{
                    final SellingTransaction st=db.getSingleTransaction(at.getDeviceTransactionId());
                    //a transaction has been rejected
                        PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
                        if(pm.getName().toLowerCase().equalsIgnoreCase("cash") || pm.getName().toLowerCase().equalsIgnoreCase("debt") || pm.getName().toLowerCase().contains(" CARD")) {
                            if (((st.getStatus() == 101 || st.getStatus() == 302) || (st.getStatus() == 501 || st.getStatus() == 500)) && ar.getStastusCode() == 100) {
                                //update the database
                                st.setStatus(ar.getStastusCode());
                                db.updateTransaction(st);
                                db.deleteAsyncTransaction(at.getDeviceTransactionId());

                                if(st.getStatus() != 100 || st.getStatus() != 101){
                                Nozzle nozzle = db.getSingleNozzle(st.getNozzleId());
                                nozzle.setNozzleIndex(nozzle.getNozzleIndex() + st.getQuantity());


                                //Updating Nozzle indexes
                                db.updateNozzle(nozzle);
                                }
                                //print the receipt
                                generateReceipt(st);
                                Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
                                context.sendBroadcast(i);

                            } else {

                                if (ar.getStastusCode() == 500 || ar.getStastusCode() == 100) {
                                    st.setStatus(ar.getStastusCode());
                                    db.updateTransaction(st);
                                    db.deleteAsyncTransaction(at.getDeviceTransactionId());
                                    Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
                                    context.sendBroadcast(i);
                                }
                            }

                        }else{
                            if (((st.getStatus() == 302) || (st.getStatus() == 501 || st.getStatus() == 500)) && ar.getStastusCode() == 100){
                                generateReceipt(st);
                                if(st.getStatus() != 100 || st.getStatus() != 101){
                                    Nozzle nozzle = db.getSingleNozzle(st.getNozzleId());
                                    nozzle.setNozzleIndex(nozzle.getNozzleIndex() + st.getQuantity());
                                    //Updating Nozzle indexes
                                    db.updateNozzle(nozzle);
                                }
                                Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
                                context.sendBroadcast(i);
                            }
                        }

//                    if(ar.getStastusCode()==500 && st.getStatus()==500){
//                        st.setStatus(500);
//                        db.updateTransaction(st);
//                        //delete it from the pending task
//                        db.deleteAsyncTransaction(at.getDeviceTransactionId());
//                    }else if(ar.getStastusCode()==301){
//                        st.setStatus(301);
//                        db.updateTransaction(st);
//                        AsyncTransaction async=db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
//                        async.setSum(async.getSum()+1);
//                        Log.d(tag,"the pending transaction: "+st.getDeviceTransactionId()+" has checksum: "+async.getSum());
//                        db.updateAsyncTransaction(async);
//                    }else if(ar.getStastusCode()==100){
//                        if(st.getStatus()==302 || st.getStatus()==501|| st.getStatus()==301){
//                            st.setStatus(100);
//                            db.deleteAsyncTransaction(at.getDeviceTransactionId());
//                            db.updateTransaction(st);
//
//                            Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
//                            nozzle.setNozzleIndex(nozzle.getNozzleIndex()+st.getQuantity());
//
//                            //Updating Nozzle indexes
//                            db.updateNozzle(nozzle);
//                            //__________________Print if the status was generated to generate receipt_____________________\\
//
//                            Runnable runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.v(tag,"Running a printing thread");
//                                    try{
//                                            TransactionPrint tp=new TransactionPrint();
//
//                                            tp.setAmount(st.getAmount());
//                                            tp.setQuantity(st.getQuantity());
//                                            tp.setBranchName(db.getSingleUser(userId).getBranch_name());
//                                            tp.setDeviceId(db.getSingleDevice().getDeviceNo());
//                                            tp.setUserName(db.getSingleUser(userId).getName());
//                                            tp.setDeviceTransactionId(String.valueOf(at.getDeviceTransactionId()));
//                                            tp.setDeviceTransactionTime(st.getDeviceTransactionTime());
//                                            tp.setNozzleName(db.getSingleNozzle(st.getNozzleId()).getNozzleName());
//                                            tp.setPaymentMode(db.getSinglePaymentMode(st.getPaymentModeId()).getName());
//
//                                            if(st.getPlateNumber()!=null)
//                                                tp.setPlateNumber(st.getPlateNumber());
//                                            else
//                                                tp.setPlateNumber("N/A");
//
//                                            tp.setProductName(db.getSingleNozzle(st.getNozzleId()).getProductName());
//                                            tp.setPumpName(db.getSinglePump(st.getPumpId()).getPumpName());
//
//                                            if(st.getTelephone()!=null)
//                                                tp.setTelephone(st.getTelephone());
//                                            else
//                                                tp.setTelephone("N/A");
//
//                                            if(st.getTin()!=null)
//                                                tp.setTin(st.getTin());
//                                            else
//                                                tp.setTin("N/A");
//
//                                            if(st.getVoucherNumber()!=null)
//                                                tp.setVoucherNumber(st.getVoucherNumber());
//                                            else
//                                                tp.setVoucherNumber("N/A");
//
//                                            if(st.getName()!=null)
//                                                tp.setCompanyName(st.getName());
//                                            else
//                                                tp.setCompanyName("N/A");
//
////                                            if(st.getStatus()==100 || st.getStatus()==101){
//                                                tp.setPaymentStatus("Success");
//                                                //launch printing procedure
//                                                PrintHandler ph=new PrintHandler(context,tp);
//                                                String print=ph.transPrint();
//                                                if(!print.equalsIgnoreCase("Success")){
//                                                    Log.e(tag,print);
//                                                }
////                                            }else{
////                                                //Update the status to generate the the print out finally
////                                                if(st.getStatus()!=500){
////                                                    st.setStatus(st.getStatus()+1);
////
////                                                    long dbId=db.updateTransaction(st);
////                                                    if(dbId<=0){
////                                                        Log.e(tag,"Failed to generate receipt");
////                                                    }
////                                                }
////                                            }
//
//                                    }catch (Exception e){
//                                        Log.e(tag,e.getMessage());
//                                        e.printStackTrace();
//                                    }
//                                }
//                            };
//                            new Thread(runnable).start();
//
//                            Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh_main");
//                            sendBroadcast(i);
//                            //________________________________________\\
//                        }else{
//                            st.setStatus(100);
//                            db.deleteAsyncTransaction(at.getDeviceTransactionId());
//                            db.updateTransaction(st);
//
//                            Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
//                            nozzle.setNozzleIndex(nozzle.getNozzleIndex() + st.getQuantity());
//
//                            //Updating Nozzle indexes
//                           db.updateNozzle(nozzle);
//                               Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh_main");
//                               sendBroadcast(i);
//
//                        }
//                    }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(tag,"Exit With Error:"+e.getMessage());
                    System.out.println("Exit with Error: "+e.getMessage());
                }

            }
        }

        protected void generateReceipt(final SellingTransaction st){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Log.v(tag,"Running a printing thread");
                    try{
                        TransactionPrint tp=new TransactionPrint();

                        tp.setAmount(st.getAmount());
                        tp.setQuantity(st.getQuantity());
                        tp.setBranchName(db.getSingleUser(userId).getBranch_name());
                        tp.setDeviceId(db.getSingleDevice().getDeviceNo());
                        tp.setUserName(db.getSingleUser(userId).getName());
                        tp.setDeviceTransactionId(String.valueOf(st.getDeviceTransactionId()));
                        tp.setDeviceTransactionTime(st.getDeviceTransactionTime());
                        tp.setNozzleName(db.getSingleNozzle(st.getNozzleId()).getNozzleName());
                        tp.setPaymentMode(db.getSinglePaymentMode(st.getPaymentModeId()).getName());

                        if(st.getPlateNumber()!=null)
                            tp.setPlateNumber(st.getPlateNumber());
                        else
                            tp.setPlateNumber("N/A");

                        tp.setProductName(db.getSingleNozzle(st.getNozzleId()).getProductName());
                        tp.setPumpName(db.getSinglePump(st.getPumpId()).getPumpName());

                        if(st.getTelephone()!=null)
                            tp.setTelephone(st.getTelephone());
                        else
                            tp.setTelephone("N/A");

                        if(st.getTin()!=null)
                            tp.setTin(st.getTin());
                        else
                            tp.setTin("N/A");

                        if(st.getVoucherNumber()!=null)
                            tp.setVoucherNumber(st.getVoucherNumber());
                        else
                            tp.setVoucherNumber("N/A");

                        if(st.getName()!=null)
                            tp.setCompanyName(st.getName());
                        else
                            tp.setCompanyName("N/A");

                        tp.setPaymentStatus("Success");

                        st.setStatus(100);
                        long dbId = db.updateTransaction(st);
                        //launch printing procedure
                        PrintHandler ph=new PrintHandler(context,tp);
                        String print=ph.printOut();//transPrint(tp);
                        if(!print.equalsIgnoreCase("Success")){
                            Log.e(tag,print);
                        }

                    }catch (Exception e){
                        Log.e(tag,e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();

            Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh_main");
            sendBroadcast(i);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}
