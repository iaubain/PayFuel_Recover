package com.aub.oltranz.payfuel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import appBean.DeviceRegistrationResponse;
import databaseBean.DBHelper;
import entities.DeviceIdentity;
import features.DeviceUuidFactory;
import features.HandleUrl;
import features.HandleUrlInterface;
import models.DeviceBean;
import models.MapperClass;

public class RegisterDevice extends ActionBarActivity implements HandleUrlInterface{

    String tag="PayFuel: "+getClass().getSimpleName();
    TextView tv;
    EditText userName, password,devName,reDevName;
    ImageView loginLink, admin;
    Button reg;
    String devRegUrl, deviceSerial,deviceName;
    Context context;

    DBHelper db;
    MapperClass mapper;
    HandleUrl hu;
    Typeface font;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_register_device);
        Log.d(tag,"DeviceRegistration Activity Created");
        //initialize UI
        initActUI();
        //initialize Activity components
        initActComponent();
    }

    //Initialise Activity UI
    public void initActUI(){
        Log.d(tag,"Initialize Activity UI");
        context=getApplicationContext();
        font=Typeface.createFromAsset(getAssets(), "font/ubuntu.ttf");
        //labels
        TextView lblMail, lblPw, lblDevNm, lblDevNmRe;
        Button reg;

        reg=(Button) findViewById(R.id.devreg);
        reg.setTypeface(font, Typeface.BOLD);

        lblMail=(TextView) findViewById(R.id.lblUserName);
        lblMail.setTypeface(font);

        lblPw=(TextView) findViewById(R.id.lblPassword);
        lblPw.setTypeface(font);

        lblDevNm=(TextView) findViewById(R.id.lbldevice);
        lblDevNm.setTypeface(font);

        lblDevNmRe=(TextView) findViewById(R.id.lbldeviceretype);
        lblDevNmRe.setTypeface(font);

        tv=(TextView) findViewById(R.id.popupTv);
        tv.setTypeface(font);
        loginLink=(ImageView) findViewById(R.id.loginLink);
        admin =(ImageView) findViewById(R.id.adminLink);
        userName=(EditText) findViewById(R.id.username);
        userName.requestFocus();
        userName.setTypeface(font);
        password=(EditText) findViewById(R.id.pw);
        password.setTypeface(font);
        devName=(EditText) findViewById(R.id.devname);
        devName.setTypeface(font);
        reDevName=(EditText) findViewById(R.id.retypedevname);
        reDevName.setTypeface(font);
    }

    //initialize Activity components
    public void initActComponent(){
        Log.d(tag, "Initialize Activity Components");
        deviceSerial= Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        db=new DBHelper(context);
        mapper=new MapperClass();
    }

    public void login(View v){
        Log.d(tag,"Registering Device Triggered");
        intent = new Intent(this, Home.class);
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
        }
    }

    public void register(View v){
        Log.d(tag, "Register Device Process");
        if((!userName.getText().toString().equalsIgnoreCase(""))&&(!password.getText().toString().equalsIgnoreCase(""))&&(!devName.getText().toString().equalsIgnoreCase(""))){
            if(reDevName.getText().toString().equals(devName.getText().toString())){
                deviceName=devName.getText().toString();

                DeviceBean devBean=new DeviceBean();
                devBean.setDeviceId(devName.getText().toString());
                devBean.setEmail(userName.getText().toString());
                devBean.setPassword(password.getText().toString());

                    DeviceUuidFactory duf=new DeviceUuidFactory(this);
                    try{
                        deviceSerial+="/" +String.valueOf(duf.getDeviceUuid());
                    }catch (Exception e){e.printStackTrace();}
                devBean.setSerialNumber(deviceSerial);


                //disabling the UI
                disableUI();

                String jsonData=mapper.mapping(devBean);
                hu=new HandleUrl(this,context,getResources().getString(R.string.registerdeviceurl),getResources().getString(R.string.post),jsonData);
            }else{
                uiFeedBack(getResources().getString(R.string.invaliddata));
            }
       }else{
            uiFeedBack(getResources().getString(R.string.invaliddata));
        }
    }
    //Return a message to the user
    public void uiFeedBack(String message){
        enableUI();
        try{
            if(!TextUtils.isEmpty(message)){
                tv.setTextColor(getResources().getColor(R.color.error));
                tv.setText(message);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv.setTextColor(getResources().getColor(R.color.rdcolor));
                        tv.setText("Register Your Device");
                    }
                }, 4000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void resultObject(Object object) {
        if(object==null){
            uiFeedBack(getResources().getString(R.string.connectionerror));
        }else {
            Log.d(tag, "Objected to: " + object.getClass().getSimpleName());
            if (object.getClass().getSimpleName().equalsIgnoreCase("DeviceRegistrationResponse")) {
                DeviceRegistrationResponse drr=(DeviceRegistrationResponse) object;
                if(drr.getStatusCode()!=100){
                    //when device registration had a problem
                    uiFeedBack(getResources().getString(R.string.deviceregfail));
                }else{
                    //when the status code is Okay
                    db.truncateDevice();

                    DeviceIdentity di=new DeviceIdentity();
                    di.setSerialNumber(deviceSerial);
                    di.setDeviceNo(deviceName);
                    long dbId=db.createDevice(di);
                    if(dbId>=1){
                        intent=new Intent(this,Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                        startActivity(intent);
                    }else{
                        uiFeedBack(getResources().getString(R.string.invaliddata));
                    }
                }
            }else{
                uiFeedBack(getResources().getString(R.string.ambiguous));
            }
        }
    }

    @Override
    public void feedBack(String message) {
        uiFeedBack(message);
    }

    //Disabling all UI element
    public void disableUI(){
        Log.d(tag,"Disable all UI Elements");
        ScrollView layout = (ScrollView) findViewById(R.id.reglayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(child.isEnabled())
            child.setEnabled(false);
        }
    }

    //Enable all UI element
    public void enableUI(){
        Log.d(tag,"Enable all UI Elements");
        ScrollView layout = (ScrollView) findViewById(R.id.reglayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(!child.isEnabled())
                child.setEnabled(true);

            //Reset all Edit text
            View view = layout.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }
        }
    }
}
