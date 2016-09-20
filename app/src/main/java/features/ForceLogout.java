package features;

import android.app.Activity;
import android.content.Context;
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

import appBean.LogoutResponse;
import databaseBean.DBHelper;
import entities.Logged_in_user;
import models.LogoutData;
import models.MapperClass;

/**
 * Created by Owner on 7/4/2016.
 */
public class ForceLogout {
    String tag="PayFuel: "+getClass().getSimpleName();
    final private Activity context;
    final private int userId;
    final private String deviceNo;
    final private String url;
    private String logoutData;
    private DBHelper db;
    private boolean check;

    public ForceLogout(final Activity context, int userId, String deviceNo, String url) {
        Log.d(tag, "Initiating values for forcing logout of user: "+userId);
        this.context=context;
        this.userId=userId;
        this.deviceNo=deviceNo;
        this.url=url;
        db=new DBHelper(context);
        check=false;
    }

    public boolean logout(){
        Log.d(tag,"Logging out");

        LogoutData ld=new LogoutData();
        ld.setDevId(deviceNo);
        ld.setUserId(userId);
        logoutData=new MapperClass().mapping(ld);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.v(tag,"Running a loggingout thread thread");
                try{
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String result=processLogout();
                            if(result==null){
                                Log.e(tag,"Error Occurred, During Checking Transaction Status");
                                check=false;
                            }else {
                                ObjectMapper mapper = new ObjectMapper();

                                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                try {
                                    LogoutResponse lr = mapper.readValue(result, LogoutResponse.class);
                                    Log.d(tag, "mapped Object is: " + lr.getClass().getSimpleName());

                                    if (lr.getStatusCode() != 100)
                                        check = false;
                                    else {
                                        long log = 0;
                                        //delete Work Status
                                        db.deleteStatusByUser(userId);

                                        //delete user
                                        // db.truncateTransactions();
                                        Logged_in_user user = new Logged_in_user();
                                        user.setLogged(0);
                                        log = db.updateUser(user);
                                        Log.v(tag, "User log status 0: " + log);
                                        db.deleteUser(userId);
                                        db.deleteStatusByUser(userId);

                                        //removing shared preferences
                                        PreferenceManager prefs = new PreferenceManager(context);
                                        if (!prefs.deletePrefs()) {
                                            Log.e(tag, "Deleting Shared preference failed");
                                            //uiFeedBack("Deleting Shared preference failed");
                                        }

                                        check = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    check = false;
                                    //uiFeedBack(e.getMessage());
                                }

                                //registerPump();
                            }
                        }
                    });

                }catch (Exception e){
                   // uiFeedBack(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();

        return check;
    }

    public void uiFeedBack(String message){

    }

    public String processLogout(){
        Log.d(tag,"Pushing and Pull data from Online");
        try {
            //_____________Opening connection and post data____________//
            URL oURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) oURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", "Application/json; charset=UTF-8");


            con.setDoOutput(true);
            con.setDoInput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

            wr.writeBytes(logoutData);
            wr.flush();
            wr.close();
            System.out.println("Data to post :" + logoutData);
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
}
