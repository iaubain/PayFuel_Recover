package utilities;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import modules.PostTransaction;
import modules.TransactionPrintModule;

public class PeriodicTransactionService extends IntentService implements PostTransaction.PostTransactionInteraction,
        TransactionPrintModule.TransactionPrintInteraction {
    private String tag=getClass().getSimpleName();
    public static final String ACTION="PostTransaction";
    public static final String USER_ID_PARAM="userId";
    private DBHelper db;
    private int userId;
    public PeriodicTransactionService() {
        super("PeriodicTransactionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(tag, "Service running");
        Bundle bundle = intent.getExtras();
        if(bundle == null)
            return;

        if(db == null)
            db = new DBHelper(getApplicationContext());

        if(bundle.getInt(USER_ID_PARAM) != 0)
            this.userId = bundle.getInt(USER_ID_PARAM);
        else
            return;

        if(intent.getAction().equals(ACTION)){
            List<AsyncTransaction> asyncTransactionList = db.getAllAsyncTransactions(userId);
            if(!asyncTransactionList.isEmpty()){
                for(AsyncTransaction asyncTransaction : asyncTransactionList){
                    startPosting(asyncTransaction);
                }
            }
        }
    }

    private void startPosting(AsyncTransaction asyncTransaction){
        try{
            SellingTransaction sellingTransaction = db.getSingleTransaction(asyncTransaction.getDeviceTransactionId());
            if(sellingTransaction != null){
                if(sellingTransaction.getQuantity() != null && sellingTransaction.getQuantity() != 0){
                    PostTransaction postTransaction = new PostTransaction(PeriodicTransactionService.this, sellingTransaction);
                    postTransaction.startPosting();
                }else{
                    db.deleteAsyncTransaction(sellingTransaction.getDeviceTransactionId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTransactionPost(boolean status, int serverStatus, SellingTransaction sellingTransaction) {
        if(status){
            SellingTransaction st = db.getSingleTransaction(sellingTransaction.getDeviceTransactionId());
//            if(st.getStatus() == 100){
//                incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
//            }

            if(serverStatus == 100){
                if(st.getStatus() == 301){
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
//                    incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
                }else if(st.getStatus() == 302){
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
                    generateReceipt(st);
//                    incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
                }else if(st.getStatus() == 101 || st.getStatus() == 100){
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
                    //generateReceipt(st);
//                    incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
                }else if(st.getStatus() == 500 || st.getStatus() == 500){
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
                    generateReceipt(st);
//                    incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
                }
                db.deleteAsyncTransaction(sellingTransaction.getDeviceTransactionId());
            }else if(serverStatus == 301){
                if(st.getStatus() != 301 && st.getStatus() != 302) {
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
                }
            }else if(serverStatus == 500){
                PaymentMode paymentMode=db.getSinglePaymentMode(sellingTransaction.getPaymentModeId());
                if(paymentMode != null){
                if(paymentMode.getName().toLowerCase().contains("tigo") ||
                        paymentMode.getName().toLowerCase().contains("mtn") ||
                        paymentMode.getName().toLowerCase().contains("airtel")){
                    st.setStatus(serverStatus);
                    updateLocalTransaction(st);
                    db.deleteAsyncTransaction(sellingTransaction.getDeviceTransactionId());
                }else{
                    db.deleteAsyncTransaction(sellingTransaction.getDeviceTransactionId());
                }
                }

                incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
            }else{
                Log.i(tag, "Server status: "+ serverStatus);
                Toast.makeText(getApplicationContext(), "CONNECTIVITY STATUS: "+ serverStatus, Toast.LENGTH_LONG).show();
            }
        }else{
            switch(serverStatus){
                case PostTransaction.CONNECTIVITY_ERROR :
                    Toast.makeText(getApplicationContext(), "CONNECTIVITY ERROR", Toast.LENGTH_LONG).show();
                    break;
                case PostTransaction.LOCAL_ERROR :
                    Toast.makeText(getApplicationContext(), "LOCAL TRANSACTION ERROR", Toast.LENGTH_LONG).show();
                    break;
                case PostTransaction.SERVER_ERROR :
                    Toast.makeText(getApplicationContext(), "SERVER ERROR", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void updateLocalTransaction(SellingTransaction sellingTransaction){
        try{
            db.updateTransaction(sellingTransaction);

            PaymentMode paymentMode = db.getSinglePaymentMode(sellingTransaction.getPaymentModeId());
            if(paymentMode.getName().toLowerCase().contains("tigo") || paymentMode.getName().toLowerCase().contains("mtn") || paymentMode.getName().toLowerCase().contains("airtel")){
                AsyncTransaction asyncTransaction = db.getSingleAsyncPerTransacton(sellingTransaction.getDeviceTransactionId());
                if(asyncTransaction != null){
                    if(asyncTransaction.getSum() >= 40){
                        sellingTransaction.setStatus(500);
                        db.updateTransaction(sellingTransaction);
                        db.deleteAsyncTransaction(asyncTransaction.getDeviceTransactionId());
                    }else{
                        asyncTransaction.setSum(asyncTransaction.getSum()+1);
                        db.updateAsyncTransaction(asyncTransaction);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR: "+e.getCause(), Toast.LENGTH_LONG).show();
        }
    }

    private void incrementIndex(Nozzle nozzle, Double reduceValue){
        Log.d("Nozzle","Updating nozzle: "+nozzle.getNozzleId()+"'s Indexes");
        if(nozzle.getNozzleIndex()>reduceValue){
            Double newIndex=nozzle.getNozzleIndex()-reduceValue;
            nozzle.setNozzleIndex(newIndex);
        }else{
            nozzle.setNozzleIndex(0.0);
        }
        long dbId=db.updateNozzle(nozzle);
        Log.v("Nozzle","Nozzle "+dbId+" Updated");
        Log.v("Nozzle", "Synchronisation finished, Sending a refresh Broadcast Command");
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg",
                "refresh_processTransaction");
        getApplicationContext().sendBroadcast(i);
    }

    private void generateReceipt(SellingTransaction sellingTransaction){
        try{
            TransactionPrintModule transactionPrintModule = new TransactionPrintModule(this, getApplicationContext(), userId, db, sellingTransaction);
            transactionPrintModule.generateReceipt();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Printing Error: "+e.getCause(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void printResult(String printingMessage) {
        Toast.makeText(getApplicationContext(), printingMessage, Toast.LENGTH_SHORT).show();
    }
}
