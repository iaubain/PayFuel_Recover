package com.aub.oltranz.payfuel.myadmin;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.aub.oltranz.payfuel.R;
import com.aub.oltranz.payfuel.SellingTabHost;

public class SpAdmin extends FragmentActivity implements AdminLogin.OnAdminLoginListener {

    private String tag="PayFuel: "+getClass().getSimpleName();
    private int userId;
    private int branchId;
    private int adminId;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();

        setContentView(R.layout.activity_my_admin);
        Log.d(tag,"SP Admin Activity Created");

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            tv=(TextView) findViewById(R.id.adminMonitor);
            // Create a new Fragment to be placed in the activity layout
            AdminLogin adminLogin=new AdminLogin();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            Bundle savedBundle=getIntent().getExtras();
            userId=savedBundle.getInt("userId");
            branchId=savedBundle.getInt("branchId");
            adminLogin.newInstance(userId, branchId);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, adminLogin).commit();
        }

    }

    @Override
    public void onAdminLogin(int adminId, String info) {
        Log.d(tag, "Detected LoggedIn Admin: " + adminId + " " + info);
        if(!info.equalsIgnoreCase("success"))
            uiFeedBack(info);
        else
            this.adminId=adminId;

        Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
    }

    public void logout(View v){
        Intent intent = new Intent(this, SellingTabHost.class);
        Bundle bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        finish();
        startActivity(intent);
    }

    @Override
    public void onAdminLoginError(String message) {
        Log.e(tag,"OnAdminError: "+message);
        Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        uiFeedBack(message);
    }

    public void uiFeedBack(String message){
        Log.d(tag, "UIFeedBack: " + message);
        tv.setText(message);
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
}
