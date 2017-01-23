package features;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aub.oltranz.payfuel.R;
import com.aub.oltranz.payfuel.Report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import appBean.LoadPumpsResponse;
import databaseBean.DBHelper;
import entities.Nozzle;
import entities.Pump;
import entities.SellingTransaction;
import models.Tanks;
import models.UrlNozzles;
import models.UrlPumps;
import modules.RequestPumps;

/**
 * Created by Owner on 5/3/2016.
 */
public class LoadPumps implements RequestPumps.RequestPumpsInteraction {
    String tag="PayFuel: "+getClass().getSimpleName();
    String message;
    Context context;
    int userId;
    HandleUrl hu;
    DBHelper db;
    private LoadPumpsInteraction mListener;
    public LoadPumps(LoadPumpsInteraction mListener, Context context, int userId){
        Log.d(tag,"Initiating the link to fetch pumps");
        this.mListener = mListener;
        this.context=context;
        this.userId=userId;
        initDB();
    }

    public void startLoading(){
        RequestPumps requestPumps = new RequestPumps(LoadPumps.this, userId);
        requestPumps.makeRequest();
    }

    private void initDB(){
        Log.d(tag,"Initiating Data Base instance");
        db=new DBHelper(context);
    }

    @Override
    public void onRequestPumpInteraction(int statusCode, String message, LoadPumpsResponse loadPumpsResponse) {
        if(statusCode != 200 && statusCode != 100){
            mListener.onLoadPumpsInteraction(false, message);
            return;
        }

        if(loadPumpsResponse.getStatusCode() == 100 && loadPumpsResponse.getPumps().isEmpty()){
            mListener.onLoadPumpsInteraction(false, "Server says"+ loadPumpsResponse.getMessage()+ " with empty pumps");
            return;
        }

        //persist pumps to local Database
        try{
            PersistPumps persistPumps = new PersistPumps();
            List<UrlPumps> urlPumpsList = loadPumpsResponse.getPumps();
            persistPumps.execute(urlPumpsList);
        }catch (Exception e){
            e.printStackTrace();
            mListener.onLoadPumpsInteraction(false, e.getLocalizedMessage());
        }
    }

    private class PersistPumps extends AsyncTask<List<UrlPumps>, String, Boolean> {

        List<UrlPumps> requestedPumps;
        int localUserId;
        DBHelper db = new DBHelper(context);
        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<UrlPumps>... params) {
            requestedPumps = params[0];
            try{
                db.truncatePumps();
                db.truncateNozzles();
                for(UrlPumps up : requestedPumps){
                    Pump pump=new Pump();
                    pump.setPumpName(up.getPumpName());
                    pump.setPumpId(up.getPumpId());

                    int pumpDbId= (int) db.createPump(pump);
                    if(pumpDbId>0){
                        Log.d(tag,"Pump created: "+pumpDbId);
                        List<UrlNozzles> nozzlesList=up.getNozzles();
                        for(UrlNozzles urlNozzles : nozzlesList){
                            Nozzle nozzle=new Nozzle();
                            nozzle.setPumpId(pumpDbId);
                            nozzle.setUnitPrice(urlNozzles.getUnitPrice());
                            nozzle.setNozzleName(urlNozzles.getNozzleName());
                            nozzle.setNozzleIndex(urlNozzles.getNozzleIndex());
                            nozzle.setNozzleId(urlNozzles.getNozzleId());
                            nozzle.setProductId(urlNozzles.getProductId());
                            nozzle.setStatusCode(urlNozzles.getStatus());
                            nozzle.setProductName(urlNozzles.getProductName());
                            nozzle.setUserName(urlNozzles.getUserName());

                            int nozzleDbId= (int) db.createNozzle(nozzle);
                            Log.d(tag,"Nozzle created: "+nozzleDbId+" with Status: "+ nozzle.getStatusCode());
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
           return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                mListener.onLoadPumpsInteraction(false, "Failed to load pumps and nozzles");
                Toast.makeText(context, "Failed to load pumps and nozzles", Toast.LENGTH_SHORT).show();
                return;
            }

            mListener.onLoadPumpsInteraction(true, "Successfully loaded pumps and nozzles");
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }

        private boolean isPumpAvailable(int pumpId){
            Pump pump=db.getSinglePump(pumpId);
            return pump != null;
        }

        private boolean isNozzleAvailable(int nozzleId){
            Nozzle nozzle=db.getSingleNozzle(nozzleId);
            return nozzle != null;
        }
    }

    public interface LoadPumpsInteraction{
        void onLoadPumpsInteraction(boolean arePumpsLoaded, String message);
    }
}
