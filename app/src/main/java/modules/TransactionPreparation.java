package modules;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aub.oltranz.mysppayfuel.R;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.TransactionResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import models.MapperClass;
import progressive.TransactionProcess;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hp on 11/29/2016.
 */

public class TransactionPreparation {
    static AtomicInteger nextId = new AtomicInteger();
    private SellingTransaction sellingTransaction;
    private DBHelper db;
    private int userId;
    private ProgressDialog progressDialog;
    private TransactionPreparationInteraction mListener;
    private long transactionId;
    private int id;

    public TransactionPreparation(TransactionPreparationInteraction mListener, Context context, SellingTransaction sellingTransaction, int userId, DBHelper db) {
        id = nextId.incrementAndGet();
        this.mListener=mListener;
        this.sellingTransaction = sellingTransaction;
        this.userId=userId;
        this.db = db;

        progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    public void startTransactionPreparation(){
        progressDialog.setMessage("Recording...");
        progressDialog.show();

        PaymentMode pm=db.getSinglePaymentMode(sellingTransaction.getPaymentModeId());
        if(pm.getName().equalsIgnoreCase("cash") ||
                pm.getName().equalsIgnoreCase("debt") ||
                pm.getName().equalsIgnoreCase("voucher") ||
                pm.getName().toLowerCase().contains(" card")){
            sellingTransaction.setStatus(100);
        }else{
            sellingTransaction.setStatus(301);
        }

        long check = persistTransaction(sellingTransaction);
        while(check < 0){
            check = persistTransaction(sellingTransaction);
        }

        if(progressDialog != null)
            if(progressDialog.isShowing())
                progressDialog.dismiss();

        SellingTransaction sellingTransaction = db.getSingleTransaction(transactionId);
        mListener.onTransactionPreparation(true, sellingTransaction);
    }

    private long persistTransaction(SellingTransaction sellingTransaction){
        sellingTransaction.setDeviceTransactionId(generateId());
        transactionId = sellingTransaction.getDeviceTransactionId();

        long checkTransaction = db.createTransaction(sellingTransaction);

        if(checkTransaction > 0){
            AsyncTransaction at=new AsyncTransaction();
            at.setSum(0);
            at.setDeviceId(sellingTransaction.getDeviceNo());
            at.setUserId(sellingTransaction.getUserId());
            at.setBranchId(sellingTransaction.getBranchId());
            at.setDeviceTransactionId(sellingTransaction.getDeviceTransactionId());

            long asyncDBId=db.createAsyncTransaction(at);

            if(asyncDBId > 0){
                return asyncDBId;
            } else{
                sellingTransaction.setStatus(301);
                return db.updateTransaction(sellingTransaction);
            }
        }else{
            return checkTransaction;
        }
    }

    private long generateId(){
        return idGenerator(id);
    }

    private long idGenerator(int id){
        long now = uniqueNow();
        String ids=""+now+userId+id;
        return Long.parseLong(ids);
    }

    private long uniqueNow() {
        long now = System.currentTimeMillis();
        while(true) {
            long lastTime = TransactionProcess.lastTime.get();
            if (lastTime >= now)
                now = lastTime+1;
            if (TransactionProcess.lastTime.compareAndSet(lastTime, now)){
                return now;
            }
        }
    }

    public interface TransactionPreparationInteraction {
        void onTransactionPreparation(boolean status, SellingTransaction sellingTransaction);
    }
}
