package progressive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aub.oltranz.payfuel.R;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import appBean.TransactionResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import features.HandleUrl;
import features.HandleUrlInterface;
import features.PrintHandler;
import models.MapperClass;
import models.TransactionPrint;

/**
 * Created by Owner on 5/23/2016.
 */
public class TransactionProcess implements HandleUrlInterface {
    public static final AtomicLong lastTime = new AtomicLong();
    //Id generation of transactions
    static AtomicInteger nextId = new AtomicInteger();
    String tag="PayFuel: "+getClass().getSimpleName();
    long transactionId;
    SellingTransaction st;
    int userId;
    TransactionFeedsInterface tfi;
    DBHelper db;
    HandleUrl handUrl;
    MapperClass mc;
    private int id;
    private Activity context;

    public TransactionProcess(TransactionFeedsInterface tfi) {
        id = nextId.incrementAndGet();
        this.tfi=tfi;
        Log.d(tag,"Transaction initiated with id: "+id);
    }


    public long transactionDatas(Activity context, final SellingTransaction st){
        this.context=context;
        this.st=st;
        this.userId=st.getUserId();
        this.transactionId=idGenerator(id);
        db=new DBHelper(context);
        Log.d(tag, "Transaction ID created:" + transactionId);

        st.setDeviceTransactionId(transactionId);
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.v(tag,"Running a printing thread");
                try{
                    createTransaction(st);
                }catch (Exception e){
                    tfi.feedsMessage(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.v(tag,"Running a printing thread");
//                try{
//                    createTransaction(st);
//                }catch (Exception e){
//                    tfi.feedsMessage(e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        };
//        new Thread(runnable).start();

        return transactionId;
    }

    private long idGenerator(int id){
        Log.d(tag,"Generating transaction Id of transactionObjectId:"+id);
        long now = uniqueNow();
        String ids=""+now+userId+id;
        long finalId=Long.parseLong(ids);
        return finalId;
    }

    private long uniqueNow() {
        long now = System.currentTimeMillis();
        while(true) {
            long lastTime = TransactionProcess.lastTime.get();
            if (lastTime >= now)
                now = lastTime+1;
            if (TransactionProcess.lastTime.compareAndSet(lastTime, now)){
                Log.d(tag,"Unique transaction time: "+now);
                return now;
            }
        }
    }

    @Override
    public void resultObject(Object object) {
        Log.d(tag,"Getting result from the server");
        try{
            if(object != null){
                if(object.getClass().getSimpleName().equalsIgnoreCase("TransactionResponse")){
                    TransactionResponse transactionResponse=(TransactionResponse) object;
                    final SellingTransaction st=transactionResponse.getSellingTransaction();

                    //_________Draft______\\
                    if(transactionResponse.getStatusCode() == 500){
                        st.setStatus(500);
                        long dbId=db.updateTransaction(st);
                    }else if(transactionResponse.getStatusCode()==100){
                        SellingTransaction stLocal=db.getSingleTransaction(st.getDeviceTransactionId());

                        //Increment nozzles' index
                        Nozzle nozzle=new Nozzle();
                        nozzle=db.getSingleNozzle(st.getNozzleId());
                        incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());

                        if(stLocal.getStatus()==302){
                            //if the receipt generation is set to generate
                            //__________________Print if the status was generated to generate receipt_____________________\\

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.v(tag,"Running a printing thread");
                                    try{

                                        st.setStatus(100);
                                        db.updateTransaction(st);

                                        TransactionPrint tp=new TransactionPrint();

                                        tp.setAmount(st.getAmount());
                                        tp.setQuantity(st.getQuantity());
                                        tp.setBranchName(db.getSingleUser(userId).getBranch_name());
                                        tp.setDeviceId(db.getSingleDevice().getDeviceNo());
                                        tp.setUserName(db.getSingleUser(userId).getName());
                                        tp.setDeviceTransactionId(String.valueOf(st.getDeviceTransactionId()));
                                        tp.setDeviceTransactionTime(st.getDeviceTransactionTime());
                                        tp.setNozzleName(db.getSingleNozzle(st.getNozzleId()).getNozzleName());
                                        tp.setPaymentMode(db.getSinglePaymentMode(st.getPaymentModeId()).getName());

                                        if(st.getPlateNumber()!=null)
                                            tp.setPlateNumber(st.getPlateNumber());
                                        else
                                            tp.setPlateNumber("N/A");

                                        tp.setProductName(db.getSingleNozzle(st.getNozzleId()).getProductName());
                                        tp.setPumpName(db.getSinglePump(st.getPumpId()).getPumpName());

                                        if(st.getTelephone()!=null)
                                            tp.setTelephone(st.getTelephone());
                                        else
                                            tp.setTelephone("N/A");

                                        if(st.getTin()!=null)
                                            tp.setTin(st.getTin());
                                        else
                                            tp.setTin("N/A");

                                        if(st.getVoucherNumber()!=null)
                                            tp.setVoucherNumber(st.getVoucherNumber());
                                        else
                                            tp.setVoucherNumber("N/A");

                                        if(st.getName()!=null)
                                            tp.setCompanyName(st.getName());
                                        else
                                            tp.setCompanyName("N/A");

                                        if(st.getStatus()==100 || st.getStatus()==101){
                                            tp.setPaymentStatus("Success");
                                            //launch printing procedure
                                            PrintHandler ph=new PrintHandler(context,tp);
                                            String print=ph.transPrint(tp);
                                            if(!print.equalsIgnoreCase("Success")){
                                                Log.e(tag,print);
                                            }
                                        }else{
                                            //Update the status to generate the the print out finally
                                            st.setStatus(st.getStatus());

                                            long dbId=db.updateTransaction(st);
                                            if(dbId<=0){
                                                Log.e(tag,"Failed to generate receipt");
                                            }
                                        }

                                    }catch (Exception e){
                                        Log.e(tag,e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            };
                            new Thread(runnable).start();
                        }

                        st.setStatus(100);
                        db.updateTransaction(st);

                        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
                        context.sendBroadcast(i);
                    }else if(transactionResponse.getStatusCode()==301){
                        st.setStatus(301);
                        db.updateTransaction(st);
                    }
                    //________End Draft_____\\

//                if(tr.getStatusCode()==301){
//                    Log.d(tag,"A pending transaction: "+tr.getSellingTransaction().getDeviceTransactionId());
//                    SellingTransaction st=tr.getSellingTransaction();
//                    AsyncTransaction at=new AsyncTransaction();
//                    at.setSum(0);
//                    at.setDeviceId(st.getDeviceId());
//                    at.setUserId(st.getUserId());
//                    at.setBranchId(st.getBranchId());
//                    at.setDeviceTransactionId(st.getDeviceTransactionId());
//
//                    long dbId=db.createAsyncTransaction(at);
//                    if(dbId<=0){
//                        tfi.feedsMessage("Pending transaction Failed to be Recorded:"+st.getDeviceTransactionId());
//                    }
//                }else if(tr.getStatusCode()==100){
//                    SellingTransaction st=tr.getSellingTransaction();
//                    PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
//                    if(!pm.getName().equalsIgnoreCase("cash") && !pm.getName().equalsIgnoreCase("debt")){
//                        Nozzle nozzle=new Nozzle();
//                        nozzle=db.getSingleNozzle(st.getNozzleId());
//                        incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());
//                    }
//                }else if(tr.getStatusCode()==500){
//                    SellingTransaction st=tr.getSellingTransaction();
//                    long dbId=db.updateTransaction(st);
//                }
                }
            }else{
                tfi.feedsMessage("Connectivity error");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void feedBack(String message) {
        Log.d(tag,"Server error");
        tfi.feedsMessage(message);
    }

    public void createTransaction(SellingTransaction st){
        Log.d(tag, "Creating a transaction of payment type: " + db.getSinglePaymentMode(st.getPaymentModeId()).getName());

        /*
        * Transaction status: 100(Cash-Success) 301(Pending) 500(canceled)
        * Transaction Receipt generator: 101(Cash-Success-Receipt) 301(Pending-Receipt) 501(Canceled)
        * */

//        PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());

//        //if payment mode equals cash
//        if(pm.getName().equalsIgnoreCase("cash") || pm.getName().equalsIgnoreCase("debt") || pm.getName().equalsIgnoreCase("SP CARD")){
//            st.setStatus(100);
//            long transactonId=db.createTransaction(st);
//            if(transactionId<=0){
//                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
//                return;
//            }
//
//            Nozzle nozzle=new Nozzle();
//            nozzle=db.getSingleNozzle(st.getNozzleId());
//            incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());
//            st=db.getSingleTransaction(st.getDeviceTransactionId());
//        }else{
//            st.setStatus(301);
//            long transactonId=db.createTransaction(st);
//            if(transactionId<=0){
//                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
//                return;
//            }
//
//            st=db.getSingleTransaction(st.getDeviceTransactionId());
//        }

//
//        st.setStatus(301);

        PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
//        if(pm.getName().equalsIgnoreCase("cash") || pm.getName().equalsIgnoreCase("debt") || pm.getName().contains(" CARD")){
//            st.setStatus(100);
//            long transactonId=db.createTransaction(st);
//            if(transactionId < 0 || transactonId < 0){
//                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
//                return;
//            }
//        }else{

            if(pm.getName().equalsIgnoreCase("cash") || pm.getName().equalsIgnoreCase("debt") || pm.getName().contains(" CARD"))
                st.setStatus(100);
            else
                st.setStatus(301);

            long transactonId=db.createTransaction(st);
            AsyncTransaction at=new AsyncTransaction();
            at.setSum(0);
            at.setDeviceId(st.getDeviceNo());
            at.setUserId(st.getUserId());
            at.setBranchId(st.getBranchId());
            at.setDeviceTransactionId(st.getDeviceTransactionId());

            long asyncDBId=db.createAsyncTransaction(at);

            if((transactionId < 0 || asyncDBId < 0)||(transactonId < 0)){
                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
                return;
            }
//        }

//            Nozzle nozzle=new Nozzle();
//            nozzle=db.getSingleNozzle(st.getNozzleId());
//            incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());
            st=db.getSingleTransaction(st.getDeviceTransactionId());


        //sending a transaction Online
        mc=new MapperClass();
        handUrl=new HandleUrl(this,context,context.getResources().getString(R.string.transactionurl),context.getResources().getString(R.string.post),mc.mapping(st));
    }

    public void incrementIndex(int nozzleId, Double currentIndex, Double valueToAdd){
        Log.d(tag,"Updating nozzle: "+nozzleId+"'s Indexes");
        Nozzle nozzle=db.getSingleNozzle(nozzleId);
        Double newIndex=currentIndex+valueToAdd;
        nozzle.setNozzleIndex(newIndex);
        long dbId=db.updateNozzle(nozzle);
        Log.v(tag,"Nozzle "+dbId+" Updated");
        Log.v(tag, "Synchronisation finished, Sending a refresh Broadcast Command");
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh_processTransaction");
        context.sendBroadcast(i);
    }
}
