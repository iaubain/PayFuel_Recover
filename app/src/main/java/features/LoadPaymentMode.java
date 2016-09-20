package features;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aub.oltranz.payfuel.R;

import java.util.Iterator;
import java.util.List;

import appBean.PaymentModeResponse;
import databaseBean.DBHelper;
import entities.PaymentMode;
import entities.Pump;

/**
 * Created by Owner on 5/3/2016.
 */
public class LoadPaymentMode implements HandleUrlInterface {
    String tag="PayFuel: "+getClass().getSimpleName();
    String message;
    Context context;
    int userId;
    HandleUrl hu;
    DBHelper db;
    boolean paymentLoaded = false;
    public boolean fetchPump(Context context, int userId){
        Log.d(tag, "Initiating the link to fetch payment mode");
        this.context=context;
        this.userId=userId;
        initDB();
        hu=new HandleUrl(this,context,context.getResources().getString(R.string.paymenturl)+userId,context.getResources().getString(R.string.get),null);
        return result();
    }

    @Override
    public void resultObject(Object object) {
        Log.d(tag,"Result from server received");
        if(object==null){
            //when data are not found on the server
            paymentLoaded=false;
        }else if(!object.getClass().getSimpleName().equalsIgnoreCase("PaymentModeResponse")){
            //when ambiguous server result is received
            paymentLoaded=false;
        }else{
            //when we have a good response form the server
            PaymentModeResponse pmr=(PaymentModeResponse) object;
            if(pmr.getStatusCode()!=100){
                paymentLoaded=false;
            }else{
                db.truncatePaymentMode();
                List<PaymentMode> pms=pmr.getPaymentModeList();
                Iterator iterator=pms.iterator();
                while (iterator.hasNext()){
                    PaymentMode pm=new PaymentMode();
                    pm=(PaymentMode) iterator.next();
                    if(isPaymentModeAvailable(pm.getPaymentModeId())){
                        //payment mode already there
                        Log.e(tag,"Payment Mode already there: "+pm.getPaymentModeId());
                        db.deletePaymentMode(pm.getPaymentModeId());
                        int paymentDbId=(int) db.createPayment(pm);
                        Log.d(tag,"Payment Mode created: "+paymentDbId);
                    }else{
                        int paymentDbId=(int) db.createPayment(pm);
                        Log.d(tag,"Payment Mode created: "+paymentDbId);
                    }
                }

                paymentLoaded=true;
            }
        }
    }

    @Override
    public void feedBack(String message) {
        this.message=message;
        if(!TextUtils.isEmpty(message)){
            paymentLoaded =false;
        }
    }
    public boolean result(){
        return paymentLoaded;
    }
    public void initDB(){
        Log.d(tag,"Initiating Data Base instance");
        db=new DBHelper(context);
    }

    public boolean isPaymentModeAvailable(int pId){
        Log.d(tag,"Check local payment mode if "+pId+" is available");
        PaymentMode pm=new PaymentMode();
        pm=db.getSinglePaymentMode(pId);
        return pm != null;
    }
}
