package com.aub.oltranz.payfuel;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import appBean.LogoutResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.DeviceIdentity;
import entities.Logged_in_user;
import entities.PaymentMode;
import entities.SellingTransaction;
import features.CheckTransaction;
import features.HandleUrl;
import features.HandleUrlInterface;
import features.LogoutService;
import features.PreferenceManager;
import models.LogoutData;
import models.MapperClass;
import modules.ClearPending;
import utilities.MyAlarmManager;
import utilities.PeriodicTransactionService;

public class SellingTabHost extends TabActivity implements TabHost.OnTabChangeListener, HandleUrlInterface {
    String tag="PayFuel: "+getClass().getSimpleName();

    TextView name;
    TabHost tHost;
    Context context;

    DBHelper db;
    HandleUrl handleUrl;
    MapperClass mc;

    int userId;
    boolean doubleBackToExitPressedOnce = false;

    Intent intent;
    Bundle savedBundle;
    Bundle bundle;
    StrictMode.ThreadPolicy policy;
    IntentFilter intentFilterilter;
    BroadcastReceiver broadcastReceiver;
    int numOfPendingTransaction;

    boolean sync=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            getActionBar().hide();
        }catch (Exception e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_selling_tab_host);

        savedBundle =getIntent().getExtras();

        //initialize Activity components
        initAppComponents();

        //Check user validity
        userValidity();

        PreferenceManager prefs=new PreferenceManager(this);
        prefs.createPreference(userId);

//        Calendar calCheck = Calendar.getInstance();
//        Intent alarmIntentCheck = new Intent(context, CheckTransaction.class);
//        PendingIntent pintentCheck = PendingIntent.getService(context, 0, alarmIntentCheck, 0);
//        AlarmManager alarmCheck = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        //clean alarm cache for previous pending intent
//        alarmCheck.cancel(pintentCheck);
//        // schedule for every 5 min 5 * 60 * 1000
//        alarmCheck.setInexactRepeating(AlarmManager.RTC_WAKEUP, calCheck.getTimeInMillis(), 5 * 60 * 1000, pintentCheck);

        try {

            intentFilterilter=new IntentFilter("com.aub.oltranz.payfuel.MAIN_SERVICE");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Handle the received Intent message
                    String msg = intent.getStringExtra("msg");
                    if(msg.equalsIgnoreCase("refresh_main")){
                        refresh();
                        Log.v(tag,"Refresh command from Main service");
                    }
                    if(msg.equalsIgnoreCase("refresh_check")){
                        Log.v(tag,"Refresh command from Check service");
                        refresh();
                    }
                    if(msg.equalsIgnoreCase("refresh_processTransaction")){
                        Log.v(tag,"Refresh command from Transaction Process");
                        refresh();
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilterilter);

        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        scheduleAlarm();
    }

    public void userValidity(){
        Log.d(tag, "checking user validity");
        int userCount=db.getUserCount();
        if(userCount>0){
            Log.d(tag,"Checking user validity succeeded");
            //when some user found


            String extra= savedBundle.getString(getResources().getString(R.string.userid));
            //  getIntent().getExtras().getString(getResources().getString(R.string.userid));
            userId=Integer.parseInt(extra);
            Logged_in_user user=db.getSingleUser(userId);
            if(user==null){
                Log.e(tag,"Checking user validity failed");
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }else if(user.getLogged()!=1){
                Log.e(tag,"Checking user validity failed");
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }else if(user.getLogged()==1){
                Log.d(tag,"Checking user validity succeeded");

                //initialize activity UI
                initAppUI();

                name.append(" "+user.getName());
            }
        }else{
            Log.d(tag,"Checking user validity failed");
            intent=new Intent(context,Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            finish();
            startActivity(intent);
        }
    }

    public void initAppUI() {
        Log.d(tag, "Initializing Activity UI");

        String extra= savedBundle.getString(getResources().getString(R.string.userid));
        int uId=Integer.parseInt(extra);

        bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.userid), String.valueOf(uId));

        name=(TextView) findViewById(R.id.lblname);
        tHost = getTabHost();
        TabHost.TabSpec tSpec;
        Intent intent;

        intent = new Intent().setClass(this, Selling.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        tSpec = tHost.newTabSpec("sell").setIndicator("Sell Portal") .setContent(intent);
        tHost.addTab(tSpec);

        intent = new Intent().setClass(this, Report.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        tSpec = tHost.newTabSpec("report").setIndicator("Report Portal") .setContent(intent);
        tHost.addTab(tSpec);


        TextView tv;
        for(int i=0;i<tHost.getTabWidget().getTabCount();i++){
            if(i==0){
                tHost.setCurrentTab(i);
                //tHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tabselectedcolor);
            }
            tHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_selector);
            tv=(TextView) tHost.getTabWidget().getChildTabViewAt(i).findViewById(android.R.id.title);
            tv.setAllCaps(false);
        }

        tHost.setOnTabChangedListener(this);

//        Calendar cal = Calendar.getInstance();
//        Intent alarmIntent = new Intent(context, AppMainService.class);
//        PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
//        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        //clean alarm cache for previous pending intent
//        alarmCheck.cancel(pintent);

    }

    // Setup a recurring alarm every 4 sec
    public void scheduleAlarm() {

        Calendar cal = Calendar.getInstance();
        Intent alarmIntent = new Intent(context, PeriodicTransactionService.class);
        alarmIntent.setAction(PeriodicTransactionService.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(PeriodicTransactionService.USER_ID_PARAM, userId);
        alarmIntent.putExtras(bundle);
        PendingIntent pintent = PendingIntent.getService(context,
                MyAlarmManager.REQUEST_CODE,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 4 * 1000, pintent);




//        Intent intent = new Intent(this, PeriodicTransactionService.class);
//        intent.setAction(PeriodicTransactionService.ACTION);
//        Bundle bundle = new Bundle();
//        bundle.putInt(PeriodicTransactionService.USER_ID_PARAM, userId);
//        intent.putExtras(bundle);
//        PendingIntent pIntent = PendingIntent.getBroadcast(context,
//                MyAlarmManager.REQUEST_CODE,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        long firstMillis = System.currentTimeMillis(); // alarm is set right away
//        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 1000*4, pIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyAlarmManager.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmManager.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public void refresh(){
        Log.d(tag, "Refreshing the tabs");

        int currentTabId = tHost.getCurrentTab();
        tHost.clearAllTabs();
        initAppUI();
        tHost.setCurrentTab(currentTabId);
    }

    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");
        context=this;
        db=new DBHelper(context);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void logout(View v){
        if(!hasPendingTransactions())
            uiPopLogOut();
        else{
            uiPopUp("There still "+numOfPendingTransaction+" transaction(s) on queue, please wait until are uploaded");
        }
    }

    private void uiPopLogOut(){
        Log.v(tag, "Logging out...");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                // User clicked OK button
                //            DeviceIdentity di=db.getSingleDevice();
//            LogoutData ld=new LogoutData();
//            try {
//                ld.setDevId(di.getDeviceNo());
//                ld.setUserId(userId);
//                mc=new MapperClass();
//
//                handleUrl=new HandleUrl(this,this,getResources().getString(R.string.logouturl),getResources().getString(R.string.post),mc.mapping(ld));
//            }catch (Exception e){
//                uiFeedBack(e.getMessage());
//            }


                //unregisterReceiver(broadcastReceiver);

                Intent logoutIntent=new Intent(getApplicationContext(), LogoutService.class);
                Bundle logotBundle=new Bundle();
                logotBundle.putInt("userId",userId);
                DeviceIdentity di=db.getSingleDevice();
                logotBundle.putString("deviceNo",di.getDeviceNo());

                logoutIntent.putExtras(logotBundle);

                startService(logoutIntent);

                //cancel alarm
                cancelAlarm();

                //invalidate broadcast listener
                try{
                    unregisterReceiver(broadcastReceiver);
                }catch (Exception e){
                    e.printStackTrace();
                }

                intent=new Intent(getApplicationContext(),Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

//
//        if (doubleBackToExitPressedOnce) {
////            DeviceIdentity di=db.getSingleDevice();
////            LogoutData ld=new LogoutData();
////            try {
////                ld.setDevId(di.getDeviceNo());
////                ld.setUserId(userId);
////                mc=new MapperClass();
////
////                handleUrl=new HandleUrl(this,this,getResources().getString(R.string.logouturl),getResources().getString(R.string.post),mc.mapping(ld));
////            }catch (Exception e){
////                uiFeedBack(e.getMessage());
////            }
//
//
//            //unregisterReceiver(broadcastReceiver);
//
//            Intent logoutIntent=new Intent(this, LogoutService.class);
//            Bundle logotBundle=new Bundle();
//            logotBundle.putInt("userId",userId);
//            DeviceIdentity di=db.getSingleDevice();
//            logotBundle.putString("deviceNo",di.getDeviceNo());
//
//            logoutIntent.putExtras(logotBundle);
//
//            this.startService(logoutIntent);
//
//
//            Calendar cal = Calendar.getInstance();
//            Intent alarmIntent = new Intent(context, CheckTransaction.class);
//            PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
//            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            //clean alarm cache for previous pending intent
//            alarm.cancel(pintent);
//
//            intent=new Intent(this,Home.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            finish();
//            startActivity(intent);
//
//            return;
//        }
//
//        this.doubleBackToExitPressedOnce = true;
//        Toast.makeText(this, "Please click Logout again to exit", Toast.LENGTH_SHORT).show();
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                doubleBackToExitPressedOnce = false;
//            }
//        }, 2000);
    }

    private boolean hasPendingTransactions(){
        List<SellingTransaction> pendingSellingTransaction=db.getAllUnsuccessfulTransaction(userId);
        if(!pendingSellingTransaction.isEmpty()){
            for(SellingTransaction sellingTransaction : pendingSellingTransaction){
                AsyncTransaction asyncTransaction = db.getSingleAsyncPerTransacton(sellingTransaction.getDeviceTransactionId());
                if(asyncTransaction == null){
                    try{
                        AsyncTransaction at=new AsyncTransaction();
                        at.setSum(0);
                        at.setDeviceId(sellingTransaction.getDeviceNo());
                        at.setUserId(sellingTransaction.getUserId());
                        at.setBranchId(sellingTransaction.getBranchId());
                        at.setDeviceTransactionId(sellingTransaction.getDeviceTransactionId());

                        long asyncDBId=db.createAsyncTransaction(at);
                    }catch (Exception e){e.printStackTrace();}
                }
            }
        }
        List<AsyncTransaction> asyncTransactionList=db.getAllAsyncTransactions(userId);
        try{
            numOfPendingTransaction = asyncTransactionList.size();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return !asyncTransactionList.isEmpty();
    }

    private void uiPopUp(String message){
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                    .setTitle(R.string.dialog_title);
            // Add the buttons
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    try{
//                        List<AsyncTransaction> asyncTransactionsTemp = new ArrayList<AsyncTransaction>();
//                        List<AsyncTransaction> asyncTransactionList = db.getAllAsyncTransactions(userId);
//                        for(AsyncTransaction asyncTransaction : asyncTransactionList){
//                            SellingTransaction sellingTransaction = db.getSingleTransaction(asyncTransaction.getDeviceTransactionId());
//                            PaymentMode paymentMode = db.getSinglePaymentMode(sellingTransaction.getPaymentModeId());
//
//                            if(paymentMode.getName().toLowerCase().contains("tigo") || paymentMode.getName().toLowerCase().contains("mtn") || paymentMode.getName().toLowerCase().contains("airtel")){
//                                if(asyncTransaction.getSum() <= 40){
//                                    db.deleteAsyncTransaction(asyncTransaction.getDeviceTransactionId());
//                                    if(sellingTransaction.getStatus() != 100){
//                                        sellingTransaction.setStatus(500);
//                                        db.updateTransaction(sellingTransaction);
//                                    }
//                                }else
//                                    asyncTransactionsTemp.add(asyncTransaction);
//                            }else
//                                asyncTransactionsTemp.add(asyncTransaction);
//                        }
//
                        Intent i = new Intent(context, PeriodicTransactionService.class);
                        i.setAction(PeriodicTransactionService.ACTION);
                        Bundle bundle1 = new Bundle();
                        bundle1.putInt(PeriodicTransactionService.USER_ID_PARAM, userId);
                        i.putExtras(bundle1);
                        context.startService(i);

//                        if(!asyncTransactionsTemp.isEmpty()){
//                            ClearPending clearPending = new ClearPending(SellingTabHost.this, db.getAllAsyncTransactions(userId), db);
//                            clearPending.startClearing();
//                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();


            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTabChanged(String tabTag) {
        Log.d(tag, "Current tab tag: " + tHost.getCurrentTabTag());
    }

    @Override
    public void resultObject(Object object) {
    }

    @Override
    public void feedBack(String message) {
        uiFeedBack(message);
    }

    public void uiFeedBack(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(SellingTabHost.this).create();
        alertDialog.setTitle("Attention");
        if(!TextUtils.isEmpty(message)) {
            alertDialog.setMessage(message);
        }else{
            alertDialog.setMessage(getResources().getString(R.string.faillurenotification));
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(tag, "action:" + "Menu Key Pressed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(tag, "action:" +"Back Key Pressed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(tag, "Application called onPause");
        try{

            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(tag, "Application called onResume");
        try {
            registerReceiver(broadcastReceiver, intentFilterilter);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.v(tag, "Application called onStop");
//        if(broadcastReceiver !=null)
//        unregisterReceiver(broadcastReceiver);
//    }
}
