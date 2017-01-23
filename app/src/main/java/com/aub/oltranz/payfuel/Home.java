package com.aub.oltranz.payfuel;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import appBean.Login;
import appBean.LoginResponse;
import databaseBean.DBHelper;
import entities.DeviceIdentity;
import entities.Logged_in_user;
import features.CheckTransaction;
import features.ForceLogout;
import features.HandleUrl;
import features.HandleUrlInterface;
import features.LoadPaymentMode;
import features.LoadPumps;
import features.ServiceCheck;
import features.ThreadControl;
import models.MapperClass;
import modules.LoginManager;
import modules.ResourceManager;

public class Home extends AppCompatActivity implements LoginManager.LoginManagerInteraction, ResourceManager.ResourceManagerInteraction {
    String tag = "PayFuel: " + getClass().getSimpleName();

    TextView tv;
    EditText pin;
    ImageView regLink, admin;
    Button login;
    Context context;

    ProgressDialog barProgressDialog;
    Handler updateBarHandler;

    MapperClass mapperClass;
    DBHelper db;
    HandleUrl hu;
    LoadPaymentMode lpm;

    Intent intent;
    StrictMode.ThreadPolicy policy;
    ThreadControl tc;
    Dialog progress;
    ProgressDialog progressBar;
    Typeface font;

    int userId, branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_home);

        //initialize activity UI
        initAppUI();
        //initialize activity Components
        initAppComponents();
        //device Registration
        deviceRegistration();

    }

    //initialize app UI
    public void initAppUI() {
        Log.d(tag, "Initializing app UI");
        context = this;
        font=Typeface.createFromAsset(getAssets(), "font/ubuntu.ttf");

        tv = (TextView) findViewById(R.id.popupTv);
        tv.setTypeface(font);

        pin = (EditText) findViewById(R.id.pin);
        pin.setTypeface(font);

        login = (Button) findViewById(R.id.login);
        login.setTypeface(font, Typeface.BOLD);

        regLink = (ImageView) findViewById(R.id.regLink);
        admin = (ImageView) findViewById(R.id.adminLink);
    }

    //initialize app components
    public void initAppComponents() {
        Log.d(tag, "Initializing app Components");
        mapperClass = new MapperClass();
        db = new DBHelper(context);
        tc = new ThreadControl();
        updateBarHandler = new Handler();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        barProgressDialog = new ProgressDialog(Home.this);
    }

    //Handle longin button
    public void loginFunction(View v) {
        //db.truncateDevice();
        // db.truncateUser();
        //db.truncateAsyncTransactions();
        Log.d(tag, "Login Process");
        //launchBarDialog(v);
       // showDialog("Logging In. Please wait...");
        String data = pin.getText().toString();

        if (TextUtils.isEmpty(data)) {
            uiFeedBack(getResources().getString(R.string.invaliddata));
        } else {
            //process the login
            DeviceIdentity di = db.getSingleDevice();
            Login login = new Login();
            login.setDeviceId(di.getDeviceNo());
            login.setUserPin(pin.getText().toString());

            //disabling UI
            disableUI();

            //mapping to object to get JsonData
            LoginManager loginManager = new LoginManager(Home.this, login);
            loginManager.loginRequest();
        }
    }

    public void register(View v){
        Log.d(tag,"Registering Device Triggered");
        intent = new Intent(this, RegisterDevice.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    public void myAdmin(View v){
        Log.d(tag,"ENGEN Admin Triggered");
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.olranz.payfuel.myadmin");
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            uiFeedBack("ENGEN Admin App Is Missing...!");
//            // Bring user to the market or let them choose an app?
//            intent = new Intent(Intent.ACTION_VIEW);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setData(Uri.parse("market://details?id=" + "com.package.name"));
//            startActivity(intent);
        }
    }

    //Return a message to the user
    public void uiFeedBack(String message) {
        enableUI();
        try{
            if(!TextUtils.isEmpty(message)){
                tv.setTextColor(getResources().getColor(R.color.error));
                tv.setText(message);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv.setTextColor(getResources().getColor(R.color.darkgray));
                        tv.setText("ENTER YOUR PIN CODE");
                    }
                }, 4000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //check device registration
    public void deviceRegistration() {

        //DeviceIdentity di=new DeviceIdentity();
        //  di=db.getSingleDevice();
        // System.out.println("device name: "+di.getDeviceId());
        try {
            int devCount = devCount = db.getDeviceCount();
            if (devCount <= 0) {
                //when no device found
                intent = new Intent(this, RegisterDevice.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(tag, "Exception Occurred " + e.getMessage());
            if (e.getMessage() == null) {
                //when no device found
                intent = new Intent(this, RegisterDevice.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        }
    }


    //resetting the login when a bad login happens
    public void resetLogin(){
        long log=0;
        //delete Work Status
        db.deleteStatusByUser(userId);
        //delete user

        Logged_in_user user=new Logged_in_user();
        user.setLogged(0);
        log=db.updateUser(user);
        Log.v(tag,"User log status 0: "+log);
        db.deleteUser(userId);
        db.deleteStatusByUser(userId);
    }

    //Disabling all UI element
    public void disableUI() {
        Log.d(tag, "Disable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.loginlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child.isEnabled())
                child.setEnabled(false);
        }
    }

    //Enable all UI element
    public void enableUI() {
        Log.d(tag, "Enable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.loginlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (!child.isEnabled())
                child.setEnabled(true);

            //Reset all Edit text
            View view = layout.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
            }

        }
    }

    //check if the user was already there
    public boolean isUserLogged(List<Logged_in_user> userList, int currentUserId) {

        for(Logged_in_user user:userList){
            if (user.getUser_id() == currentUserId && user.getLogged() == 1)
                return true;
        }

        return false;
    }

    public boolean forceLogoutUser(List<Logged_in_user> userList, int currentUserId) {
        Log.d(tag,"Check user: "+ currentUserId);

        int logoutPositiveCount=0, logoutNegativeCount=0;
        for(Logged_in_user user:userList){
            if(user.getUser_id() != currentUserId && user.getLogged() == 1){
                //logout this user
                DeviceIdentity di=db.getSingleDevice();
                String url=getResources().getString(R.string.logouturl);
                ForceLogout fl=new ForceLogout(this, currentUserId, di.getDeviceNo(), url);

                //return fl.logout();
                if(fl.logout())
                    logoutPositiveCount++;
                else{
                    logoutNegativeCount++;
                }
            }
        }
        if(logoutNegativeCount>0)
            return false;
        else{
            if(logoutPositiveCount>0)
                return true;
            else
                return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(tag, "action:" +"Back Key Pressed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void pause() {

        try {
            Log.e(tag, "Application Paused attempt");
            Thread.sleep(500);
            //tc.pause();
        } catch (Exception e) {
            Log.e(tag, "Application Paused attempt failed");
            e.printStackTrace();
        }
    }

    public boolean loadPayment(Context context, int userId) {
       // updateDialog("Logging In. Loading payment Modes...");
        lpm = new LoadPaymentMode();
        return lpm.fetchPump(context, userId);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(tag, "Application called onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(tag, "Application called onResume");
    }

    //showing the progress dialog
    public void showDialog(String message) {

        if (!barProgressDialog.isShowing()) {

            barProgressDialog.setTitle("Logging in...");
            barProgressDialog.setMessage(message);
            barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.setProgress(0);
            barProgressDialog.setMax(20);
            barProgressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Here you should write your time consuming task...
                        while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                            Thread.sleep(2000);

                            updateBarHandler.post(new Runnable() {

                                public void run() {

                                    barProgressDialog.incrementProgressBy(2);

                                }

                            });

                            if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {

                                barProgressDialog.dismiss();

                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
    }

    //updating text on dialog
    public void updateDialog(String message) {

        if (!barProgressDialog.isShowing()) {
            barProgressDialog.setTitle("Logging in...");
            barProgressDialog.setMessage(message);
            barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.setProgress(0);
            barProgressDialog.setMax(20);
            barProgressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Here you should write your time consuming task...
                        while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                            Thread.sleep(2000);

                            updateBarHandler.post(new Runnable() {

                                public void run() {

                                    barProgressDialog.incrementProgressBy(2);

                                }

                            });

                            if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {

                                barProgressDialog.dismiss();

                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        } else {
            barProgressDialog.setMessage(message);
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //dismiss dialog text
    public void dismissDialog() {
        barProgressDialog.dismiss();
    }

    public void launchBarDialog(View view) {

    }

    @Override
    public void onLoginManagerInteraction(boolean isLoginSuccessFull, String message, Logged_in_user user) {
        if(!isLoginSuccessFull){
            uiFeedBack(message);
            return;
        }
        if(user == null){
            uiFeedBack(message);
            return;
        }

        //check user if was available, load pumps, force logout the
        ResourceManager resourceManager = new ResourceManager(Home.this, Home.this, user);
        resourceManager.organizeResources();
    }

    @Override
    public void onResourceAvailable(boolean isUserAllowed, String message, Logged_in_user user) {
        if(isUserAllowed){
            //direct him to sales activity
        }else{
            //show the message
        }
    }

    @Override
    public void onUiLoading(boolean loading, String message, Logged_in_user user) {
        if(loading){
            //show loading message to progress dialog
        }else{
            //uiFeed the message again
        }
    }

    @Override
    public void onSelectPumps(boolean selectPumps, String message, Logged_in_user user) {
        if(selectPumps){
            //direct the user to select pumps activities
        }else{
            //show the message
        }
    }
}
