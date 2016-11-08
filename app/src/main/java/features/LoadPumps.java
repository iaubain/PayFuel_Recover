package features;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aub.oltranz.payfuel.R;

import java.util.Iterator;
import java.util.List;

import appBean.LoadPumpsResponse;
import databaseBean.DBHelper;
import entities.Nozzle;
import entities.Pump;
import models.Tanks;
import models.UrlNozzles;
import models.UrlPumps;

/**
 * Created by Owner on 5/3/2016.
 */
public class LoadPumps implements HandleUrlInterface {
    String tag="PayFuel: "+getClass().getSimpleName();
    String message;
    Context context;
    int userId;
    HandleUrl hu;
    DBHelper db;
    boolean pumpLoaded=false;
    public boolean fetchPump(Context context, int userId){
        Log.d(tag,"Initiating the link to fetch pumps");
        this.context=context;
        this.userId=userId;
        initDB();
        hu=new HandleUrl(this,context,context.getResources().getString(R.string.pumpurl)+userId,context.getResources().getString(R.string.get),null);
        return result();
    }

    @Override
    public void resultObject(Object object) {
        Log.d(tag,"Result from server received");
        try{
        if(object==null){
            //when data are not found on the server
            pumpLoaded=false;
        }else if(!object.getClass().getSimpleName().equalsIgnoreCase("LoadPumpsResponse")){
            //when ambiguous server result is received
            pumpLoaded=false;
        }else{
            //when we have a good response form the server
            LoadPumpsResponse lpr=(LoadPumpsResponse) object;

            if(lpr.getStatusCode()!=100){
                //Issue with pumpList
                pumpLoaded=false;
            }else{
                db.truncatePumps();
                db.truncateNozzles();
                List<UrlPumps> urlPumpsList=lpr.getPumps();

                    for(UrlPumps up:urlPumpsList){
                        if(!isPumpAvailable(up.getPumpId())){
                            //if pump is not found
                            Pump pump=new Pump();
//                            pump.setStatus(up.getStatus());
//                            pump.setBranchId(up.getBranchId());
                            pump.setPumpName(up.getPumpName());
                            pump.setPumpId(up.getPumpId());

                            int pumpDbId= (int) db.createPump(pump);
                            if(pumpDbId>0){
                                Log.d(tag,"Pump created: "+pumpDbId);
                                List<UrlNozzles> nozzlesList=up.getNozzles();
                                Iterator iterator1=nozzlesList.iterator();
                                while (iterator1.hasNext()){
                                    UrlNozzles urlNozzles=new UrlNozzles();
                                    urlNozzles=(UrlNozzles) iterator1.next();
                                    if(!isNozzleAvailable(urlNozzles.getNozzleId())){
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
                        }else{
                            //pump found
                            Log.e(tag,"Pump already there: " + up.getPumpId());
                            db.deletePump(up.getPumpId());
                            db.deleteNozzleByPump(up.getPumpId());
                            //create pump
                            Pump pump=new Pump();
                            pump.setStatus(up.getStatus());
                            pump.setBranchId(up.getBranchId());
                            pump.setPumpName(up.getPumpName());
                            pump.setPumpId(up.getPumpId());
                            int pumpDbId= (int) db.createPump(pump);
                            if(pumpDbId>0){
                                Log.d(tag,"Pump created: "+pumpDbId);
                                List<UrlNozzles> nozzlesList=up.getNozzles();
                                Iterator iterator1=nozzlesList.iterator();
                                for(UrlNozzles urlNozzles:nozzlesList){
                                    if(!isNozzleAvailable(urlNozzles.getNozzleId())){
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
                                        Log.d(tag,"Nozzle created: "+nozzleDbId);
                                    }
                                }
                            }
                        }
                    }

                pumpLoaded=true;
            }

        }
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void feedBack(String message) {
        this.message=message;
        if(!TextUtils.isEmpty(message)){
            pumpLoaded=false;
        }
    }

    public boolean result(){
        return pumpLoaded;
    }

    public boolean isPumpAvailable(int pumpId){
        Pump pump=new Pump();
        pump=db.getSinglePump(pumpId);
        return pump != null;
    }

    public boolean isNozzleAvailable(int nozzleId){
        Nozzle nozzle=new Nozzle();
        nozzle=db.getSingleNozzle(nozzleId);
        return nozzle != null;
    }

    public void initDB(){
        Log.d(tag,"Initiating Data Base instance");
        db=new DBHelper(context);
    }
}
