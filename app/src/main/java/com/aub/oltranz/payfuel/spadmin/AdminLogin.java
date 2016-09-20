package com.aub.oltranz.payfuel.spadmin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;
import com.aub.oltranz.payfuel.spadmin.beans.AdminLoginBean;

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
import appBean.LoginResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.SellingTransaction;
import features.PrintHandler;
import models.MapperClass;
import models.TransactionPrint;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAdminLoginListener} interface
 * to handle interaction events.
 * Use the {@link AdminLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminLogin extends Fragment {
    private String tag = "PayFuel: "+getClass().getSimpleName();
    String lbl=tag;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String userIdParam = "userId";
    private static final String branchIdParam = "branchId";


    private int userId;
    private int branchId;
    private OnAdminLoginListener loginListener;
    private DBHelper db;
    private String url;

    public AdminLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @param branchId Parameter 2.
     * @return A new instance of fragment AdminLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminLogin newInstance(int userId, int branchId) {
        Log.d("Instance","newInstance of AdminLogin with user id: "+userId+" and branch id: "+branchId);
        AdminLogin fragment = new AdminLogin();
        Bundle args = new Bundle();
        args.putInt(userIdParam, userId);
        args.putInt(branchIdParam, branchId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag,"Fragment created");
        if (getArguments() != null) {
            userId = getArguments().getInt(userIdParam);
            branchId = getArguments().getInt(branchIdParam);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag,"Layout View is Being inflated");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(tag,"View are finally inflated");
        final TextView tv=(TextView) view.findViewById(R.id.tv);
        final EditText email=(EditText) view.findViewById(R.id.emailId);
        final EditText pw=(EditText) view.findViewById(R.id.pw);
        Button btnLogin=(Button) view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(pw.getText().toString())){
                    AdminLoginBean alb=new AdminLoginBean();
                    alb.setEmail(email.getText().toString());
                    alb.setPassword(pw.getText().toString());

                    String loginData=new MapperClass().mapping(alb);
                    url=getResources().getString(R.string.adminlogin);
                    AdminProcessLogin apl=new AdminProcessLogin();
                    apl.execute(loginData);
                }else{
                    tv.setText("Invalid data");
                }
            }
        });
    }

    public void loginClickListener(View v){

    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int adminId) {
        if (loginListener != null) {
            loginListener.onAdminLogin(adminId,"success");
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        Log.d(tag, "Fragment attaching");
        if (context instanceof OnAdminLoginListener) {
            loginListener = (OnAdminLoginListener) context;
            db=new DBHelper(context);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdminLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(tag,"Fragment detaching");
        loginListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAdminLoginListener {
        // TODO: Update argument type and name
        void onAdminLogin(int adminId, String info);
        void onAdminLoginError(String message);
    }


    //___________________Admin Login__________________________\\
    private class AdminProcessLogin extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag, "Admin Login starts, background activity");
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
                return e.getMessage();
            } catch (ProtocolException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }

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
                Log.e(tag,"Error Occurred, During The Admin Login: Empty or null Web Result");
                loginListener.onAdminLoginError("Empty or null Web Result");
            }else{
                Log.d(tag,"Data from Server: \n"+result);
                ObjectMapper mapper= new ObjectMapper();

                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                try {
                    LoginResponse lr =mapper.readValue(result,LoginResponse.class);
                    Log.d(tag,"mapped Object is: "+lr.getClass().getSimpleName());

                    //sending login Response
                    loginObject(lr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(tag,"Exit With Error:"+e.getMessage());
                    System.out.println("Exit with Error: "+e.getMessage());
                    loginListener.onAdminLoginError(e.getMessage());
                }

            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    public void loginObject(LoginResponse lr){
        if (lr.getStatusCode() != 100) {
            //when login response got a problem
            loginListener.onAdminLoginError(lr.getMessage());
        } else {
            Logged_in_user user=lr.getLogged_in_user();
            loginListener.onAdminLogin(user.getUser_id(),"success");
        }
    }
}
