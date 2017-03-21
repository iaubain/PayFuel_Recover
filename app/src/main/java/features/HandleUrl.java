package features;

import android.content.Context;
import android.util.Log;

import com.aub.oltranz.mysppayfuel.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import models.MapperClass;

/**
 * Created by Owner on 4/27/2016.
 */
public class HandleUrl {
    String tag="PayFuel: "+getClass().getSimpleName();
    HandleUrlInterface handUrl;
    String url;
    String method;
    String data;
    Context context;
    String urlResult;
    MapperClass mc;

    public HandleUrl(HandleUrlInterface handUrl, Context context, final String url, final String method, final String data) {
        Log.d(tag,"Initialize data to and from URL");
        this.handUrl = handUrl;
        this.context = context;
        this.url = url;
        this.method = method;
        this.data = data;

//        //Check wht's on URL
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.v(tag,"Running a URL request thread");
//                try{
//
//
//                    //handle the result from URL
//                    redirector(urlResult);
//                }catch (Exception e){
//                    urlResult = null;
//                    Log.e(tag,"Error occured during the URL request "+e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        };
//        new Thread(runnable).start();

        urlResult=handleConnectivity(url,method,data);

        //handle the result from URL
        redirector(urlResult);
    }

    public String handleConnectivity(String url, String method, String data){
        Log.d(tag,"Connection handling function");
        BufferedReader br;
        InputStreamReader in = null;
        try {
            URL oURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) oURL.openConnection();
            if(method.equalsIgnoreCase(context.getResources().getString(R.string.post))) {
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "Application/json; charset=UTF-8");
                con.setDoOutput(true);
                con.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
                System.out.println("Data to post :" + data);
                in=new InputStreamReader(con.getInputStream());
            }else if (method.equalsIgnoreCase(context.getResources().getString(R.string.get))){
                con.connect();
                in = new InputStreamReader((InputStream) con.getContent());
            }
            br= new BufferedReader(in);
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            con.disconnect();
            return response.toString();
        } catch (Exception e) {
            Log.e(tag,"An Exception Occurred: "+e.getMessage());
            handUrl.feedBack(context.getResources().getString(R.string.connectionerror));
            e.printStackTrace();
            return null;
        }
    }

    public void redirector(String urlResult){
        Log.d(tag,"Server result Redirection\n"+urlResult);
        mc=new MapperClass();
        Object object=mc.jsonToObject(urlResult);
        handUrl.resultObject(object);
    }
}
