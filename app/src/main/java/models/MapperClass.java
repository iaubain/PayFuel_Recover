package models;

import android.util.Log;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import appBean.ChoosenPumpResponse;
import appBean.DeviceRegistrationResponse;
import appBean.LoadPumpsResponse;
import appBean.Login;
import appBean.LoginResponse;
import appBean.LogoutResponse;
import appBean.PaymentModeResponse;
import appBean.TransactionResponse;

/**
 * Created by Owner on 4/27/2016.
 */
public class MapperClass {
    String tag="PayFuel: "+getClass().getSimpleName();
    Object object;
    String jsonData;
    String data;
    ObjectMapper mapper;

    public String mapping(Object object){
        Log.d(tag,"mapping object starts...");
        mapper= new ObjectMapper();

        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            jsonData=mapper.writeValueAsString(object);
            Log.d(tag,"mapping object ended with a result: "+jsonData);
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exit with Error: "+e.getMessage());
        }
        return null;
    }
//Assign string to object
    public Object jsonToObject(String urlResult){
        Log.d(tag,"MapperClass Response being redirected");
        //Check Null response form server
        if(urlResult==null){
            Object object=null;
            return object;
        }
        //Device Registration
        if(urlResult.contains("\"DeviceRegistrationResponse\"")){
            Log.d(tag,"Response redirected to DeviceRegistrationResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                DeviceRegistrationResponse drr =mapper.readValue(urlResult,DeviceRegistrationResponse.class);
                Log.d(tag,"mapped Object is: "+drr.getClass().getSimpleName());
                return drr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Login
        else if(urlResult.contains("\"LoginOpModel\"")){
            Log.d(tag,"Response redirected to LoginResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                LoginResponse lr =mapper.readValue(urlResult,LoginResponse.class);
                Log.d(tag,"mapped Object is: "+lr.getClass().getSimpleName());
                return lr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Loading Pumps
        else if(urlResult.contains("\"PumpDetailsModel\"")){
            Log.d(tag,"Response redirected to LoadPumpsResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                LoadPumpsResponse lpr =mapper.readValue(urlResult,LoadPumpsResponse.class);
                Log.d(tag,"mapped Object is: "+lpr.getClass().getSimpleName());
                return lpr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Loading payment mode
        else if(urlResult.contains("\"PaymentMode\"")){
            Log.d(tag,"Response redirected to PaymentModeResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                PaymentModeResponse pmr =mapper.readValue(urlResult,PaymentModeResponse.class);
                Log.d(tag,"mapped Object is: "+pmr.getClass().getSimpleName());
                return pmr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Send selected Pumps and Nozzles
        else if(urlResult.contains("\"AssignedPumpModel\"")){
            Log.d(tag,"Response redirected to ChoosenPumpResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                ChoosenPumpResponse cpr =mapper.readValue(urlResult,ChoosenPumpResponse.class);
                Log.d(tag,"mapped Object is: "+cpr.getClass().getSimpleName());
                return cpr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Send Logout Data
        else if(urlResult.contains("\"LogoutOpModel\"")){
            Log.d(tag,"Response redirected to LogoutResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                LogoutResponse lr =mapper.readValue(urlResult,LogoutResponse.class);
                Log.d(tag,"mapped Object is: "+lr.getClass().getSimpleName());
                return lr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        //Send Transaction Data
        else if(urlResult.contains("\"SaleDetailsModel\"")){
            Log.d(tag,"Response redirected to TransactionResponse");
            mapper= new ObjectMapper();

            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                TransactionResponse tr =mapper.readValue(urlResult,TransactionResponse.class);
                Log.d(tag,"mapped Object is: "+tr.getClass().getSimpleName());
                return tr;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag,"Exit With Error:"+e.getMessage());
                System.out.println("Exit with Error: "+e.getMessage());
            }
        }

        else {

            Log.e(tag,"No matching bean Found for: "+ urlResult);
            System.out.println("No matching bean Found for: "+ urlResult);
        }

        return null;
    }

}
