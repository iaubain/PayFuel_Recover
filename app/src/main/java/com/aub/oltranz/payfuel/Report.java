package com.aub.oltranz.payfuel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aub.oltranz.payfuel.myadmin.SpAdmin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import features.PrintReport;
import features.RecordAdapter;
import modules.PostTransaction;

public class Report extends AppCompatActivity implements RecordAdapter.RecordAdapterInteraction, PostTransaction.PostTransactionInteraction {

    String tag="PayFuel: "+getClass().getSimpleName();
    int userId;
    int branchId;

    Button records;

    Bundle savedBundle;
    Context context;
    StrictMode.ThreadPolicy policy;
    Dialog dialog;

    DBHelper db;
    boolean yesterdayReport = false;
    List<SellingTransaction> sts;
    private ListView transView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repport);

        initAppComponents();

        initAppUI();

    }

    public void initAppUI(){
        Log.d(tag, "Initializing Activity UI");

        records=(Button) findViewById(R.id.rec);

        savedBundle =getIntent().getExtras();
        if(savedBundle!=null){
            String extra= savedBundle.getString(getResources().getString(R.string.userid));
            userId=Integer.parseInt(extra);
        }

        context=this;

        try{
            Logged_in_user user=db.getSingleUser(userId);
            branchId=user.getBranch_id();
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
        sts=new ArrayList<SellingTransaction>();
    }

    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog=new Dialog(this);
        db=new DBHelper(this);
    }


    public void records(View v){
        Log.d(tag, "Transaction Logs Room");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.records);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        try{
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Transaction Logs</font>"));

        final TextView tv=(TextView) dialog.findViewById(R.id.popupTv);
        Button exit=(Button) dialog.findViewById(R.id.done);
        transView=(ListView) dialog.findViewById(R.id.records);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        try{
            tv.setText("Total Transactions: " + db.getTransactionCount(userId) + " Successful: " + db.getTransactionCountSucceeded(userId) + " Pending: " + db.getTransactionCountPending(userId) + " Cancelled: " + db.getTransactionCountCancelled(userId));
        }catch (Exception e){
            e.printStackTrace();
            tv.setText(e.getMessage());
        }
        TransactionLogs logs = new TransactionLogs();
        logs.execute(userId);


        dialog.show();
    }

    public void admin(View v){
        Intent intent=new Intent(this, SpAdmin.class);
        Bundle bundle=new Bundle();
        bundle.putInt("userId", userId);
        bundle.putInt("branchId", branchId);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void report(View v){
        Log.d(tag, "On Shift Transactions Report");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.user_report);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        try{
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Transaction Report</font>"));

//        final TextView preview=(TextView) dialog.findViewById(R.id.data);
//        final TextView tv=(TextView) dialog.findViewById(R.id.tv);
//        Button exit=(Button) dialog.findViewById(R.id.exit);
//        Button print=(Button) dialog.findViewById(R.id.print);
        RadioGroup mGroup=(RadioGroup) dialog.findViewById(R.id.mGroup);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if(id == R.id.yesterday){
                    yesterdayReport = true;
                    sts= db.getAllTransactionsPerTime(userId, -1);
                    showReport(dialog, -1, sts);
                }
                if(id == R.id.today){
                    yesterdayReport = false;
                    sts= db.getAllTransactionsPerTime(userId, 0);
                    showReport(dialog, 0, sts);
                }
            }
        });

//        exit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        print.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.v(tag,"Running a printing thread");
//                        try{
//
//                            PrintReport pr=new PrintReport(context,db.getSingleUser(userId).getBranch_name(),preview.getText().toString());
//                            String print=pr.reportPrint();
//                            if(!print.equalsIgnoreCase("Success")){
//                                tv.setText(print);
//                            }
//
//                        }catch (Exception e){
//                            tv.setText("Error Occured");
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                new Thread(runnable).start();
//
//            }
//        });
//
//        try{
//            preview.setText("Total Transactions: " + db.getTransactionCount(userId) + " Successful: " + db.getTransactionCountSucceeded(userId) + " Pending: " + db.getTransactionCountPending(userId) + " Cancelled: " + db.getTransactionCountCancelled(userId));
//        }catch (Exception e){
//            e.printStackTrace();
//            preview.setText(e.getMessage());
//        }
//
//        HashMap<String, Double> nozzleData = new HashMap<String, Double>();
//        HashMap<String, Double> paymentData = new HashMap<String, Double>();
//        HashMap<String, Double> productData = new HashMap<String, Double>();
//        Double bigSum= Double.valueOf(0);
//
//        if(sts.isEmpty()){
//            preview.setText("No Data Yet");
//            print.setClickable(false);
//        }else{
//
//            for(SellingTransaction st: sts){
//                double tempQty;
//                double tempAmnt;
//                double tempProd;
//
//                Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
//                String nozzleName=nozzle.getNozzleName();
//
//                String productName=nozzle.getProductName();
//
//                PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
//                String paymentName=pm.getName();
//
//                //Nozzles and their sales quantity
//                if(nozzleData.containsKey(nozzleName)) {
//                    tempQty = nozzleData.get(nozzleName)+st.getQuantity();
//                    nozzleData.put(nozzleName,tempQty);
//                }
//                else {
//                    nozzleData.put(nozzleName, st.getQuantity());
//                }
//
//                //Product and their sales quantity
//                if(productData.containsKey(productName)) {
//                    tempProd = productData.get(productName)+st.getQuantity();
//                    productData.put(productName,tempProd);
//                }
//                else {
//                    productData.put(productName, st.getQuantity());
//                }
//
//                //Payment mode and their sales Amount
//                if(paymentData.containsKey(paymentName)) {
//                    tempAmnt = paymentData.get(paymentName)+st.getAmount();
//                    paymentData.put(paymentName,tempAmnt);
//                }
//                else {
//                    paymentData.put(paymentName, st.getAmount());
//                }
//                bigSum+=st.getAmount();
//            }
//
//            //Displays data on TextView tv monitor
//            preview.setText("User: "+db.getSingleUser(userId).getName() + "\n\n");
//            for (HashMap.Entry<String, Double> nData : nozzleData.entrySet()) {
//                preview.append(nData.getKey() + ": " + nData.getValue() + " Liters\n");
//            }
//            preview.append("\n");
//
//            for (HashMap.Entry<String, Double> pData : productData.entrySet()) {
//                preview.append(pData.getKey() + ": " + pData.getValue() + " Liters\n");
//            }
//            preview.append("\n");
//
//            for (HashMap.Entry<String, Double> payData : paymentData.entrySet()) {
//                preview.append(payData.getKey() + ": " + payData.getValue() + " Rwf\n");
//            }
//            preview.append("\n");
//            preview.append("Total Income:" + bigSum + " Rwf\n\n");
//            final Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH)+1;
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            String now=year+"-"+month+"-"+day;
//            if(month<10)
//                now=year+"-0"+month+"-"+day;
//
//            if(day<10)
//                now=year+"-"+month+"-0"+day;
//
//            preview.append(now + "\n");
//            preview.append("Signature \n");
//        }

        dialog.show();
    }

private void showReport(final Dialog dialog, int repportDate, List<SellingTransaction> sts){
    final TextView preview=(TextView) dialog.findViewById(R.id.data);
    final TextView tv=(TextView) dialog.findViewById(R.id.popupTv);
    Button exit=(Button) dialog.findViewById(R.id.exit);
    Button print=(Button) dialog.findViewById(R.id.print);

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    Calendar cal = Calendar.getInstance();

    exit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    });

    print.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Log.v(tag,"Running a printing thread");
                    try{

                        PrintReport pr=new PrintReport(context,db.getSingleUser(userId).getBranch_name(),preview.getText().toString());
                        String print=pr.reportPrint();
                        if(!print.equalsIgnoreCase("Success")){
                            tv.setText(print);
                        }

                    }catch (Exception e){
                        tv.setText("Error Occured");
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();

        }
    });

    try{
        preview.setText("Total Transactions: " + db.getTransactionCount(userId) + " Successful: " + db.getTransactionCountSucceeded(userId) + " Pending: " + db.getTransactionCountPending(userId) + " Cancelled: " + db.getTransactionCountCancelled(userId));
    }catch (Exception e){
        e.printStackTrace();
        preview.setText(e.getMessage());
    }

    HashMap<String, Double> nozzleData = new HashMap<String, Double>();
    HashMap<String, Double> paymentData = new HashMap<String, Double>();
    HashMap<String, Double> productData = new HashMap<String, Double>();
    Double bigSum= Double.valueOf(0);

    if(sts.isEmpty()){
        preview.setText("No Data Yet");
        print.setClickable(false);
    }else{

        for(SellingTransaction st: sts){
            double tempQty;
            double tempAmnt;
            double tempProd;

            Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
            String nozzleName=nozzle.getNozzleName();

            String productName=nozzle.getProductName();

            PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
            String paymentName=pm.getName();

            //Nozzles and their sales quantity
            if(nozzleData.containsKey(nozzleName)) {
                tempQty = nozzleData.get(nozzleName)+st.getQuantity();
                nozzleData.put(nozzleName,tempQty);
            }
            else {
                nozzleData.put(nozzleName, st.getQuantity());
            }

            //Product and their sales quantity
            if(productData.containsKey(productName)) {
                tempProd = productData.get(productName)+st.getQuantity();
                productData.put(productName,tempProd);
            }
            else {
                productData.put(productName, st.getQuantity());
            }

            //Payment mode and their sales Amount
            if(paymentData.containsKey(paymentName)) {
                tempAmnt = paymentData.get(paymentName)+st.getAmount();
                paymentData.put(paymentName,tempAmnt);
            }
            else {
                paymentData.put(paymentName, st.getAmount());
            }
            bigSum+=st.getAmount();
        }

        //Displays data on TextView tv monitor
        preview.setText("User: "+db.getSingleUser(userId).getName() + "\n\n");
        for (HashMap.Entry<String, Double> nData : nozzleData.entrySet()) {
            preview.append(nData.getKey() + ": " + nData.getValue() + " Liters\n");
        }
        preview.append("\n");

        for (HashMap.Entry<String, Double> pData : productData.entrySet()) {
            preview.append(pData.getKey() + ": " + pData.getValue() + " Liters\n");
        }
        preview.append("\n");

        for (HashMap.Entry<String, Double> payData : paymentData.entrySet()) {
            preview.append(payData.getKey() + ": " + payData.getValue() + " Rwf\n");
        }
        preview.append("\n");
        preview.append("Total Income:" + bigSum + " Rwf\n\n");
        String now = null;
        if(repportDate == -1){
            cal.add(Calendar.DATE, -1);
            now=dateFormat.format(cal.getTime());
        } else if(repportDate == 0){
            now=dateFormat.format(cal.getTime());
        }else{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH)+1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            now=year+"-"+month+"-"+day;
            if(month<10)
                now=year+"-0"+month+"-"+day;

            if(day<10)
                now=year+"-"+month+"-0"+day;
        }

        preview.append(now + "\n");
        preview.append("Signature \n");
    }
}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(tag, "action:" + "Menu Key Pressed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(tag, "action:" +"Back Key Pressed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRecordInteractionRefresh(boolean refresh, SellingTransaction sellingTransaction) {
        if(dialog != null)
            if(dialog.isShowing())
                dialog.dismiss();
        if(refresh && sellingTransaction != null){
            //refresh a transaction
            PostTransaction postTransaction = new PostTransaction(Report.this, sellingTransaction);
            postTransaction.startPosting();
        }
    }

    @Override
    public void onTransactionPost(boolean status, int serverStatus, SellingTransaction sellingTransaction) {
        if(status){
            SellingTransaction st = db.getSingleTransaction(sellingTransaction.getDeviceTransactionId());
            if(serverStatus == 100 && st.getStatus() != 100){
                incrementIndex(db.getSingleNozzle(st.getNozzleId()), st.getQuantity());
                AsyncTransaction asyncTransaction = db.getSingleAsyncPerTransacton(st.getDeviceTransactionId());
                if(asyncTransaction != null)
                    db.deleteAsyncTransaction(st.getDeviceTransactionId());
            }
            if(st.getStatus() != 100){
                st.setStatus(serverStatus);
                updateLocalTransaction(st);
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

    public void incrementIndex(Nozzle nozzle, Double addValue){
        Log.d("Nozzle","Updating nozzle: "+nozzle.getNozzleId()+"'s Indexes");
        Double newIndex=nozzle.getNozzleIndex()+addValue;
        nozzle.setNozzleIndex(newIndex);
        long dbId=db.updateNozzle(nozzle);
        Log.v("Nozzle","Nozzle "+dbId+" Updated");
        Log.v("Nozzle", "Synchronisation finished, Sending a refresh Broadcast Command");
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg",
                "refresh_processTransaction");
        getApplicationContext().sendBroadcast(i);
    }


    private class TransactionLogs extends AsyncTask<Integer, Object, List<SellingTransaction>> {

        List<SellingTransaction> transactionList;
        int localUserId;
        DBHelper db = new DBHelper(context);
        @Override
        protected List<SellingTransaction> doInBackground(Integer... params) {
            localUserId = params[0];
            transactionList = new ArrayList<>();
            try{
                return transactionList =  db.getAllTransactionsPerUser(localUserId);
            }catch (Exception e){
                e.printStackTrace();
                return transactionList;
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(List<SellingTransaction> result) {
            if(result.isEmpty()){
                Toast.makeText(Report.this, "No transaction found", Toast.LENGTH_SHORT).show();
                return;
            }
            RecordAdapter ra = new RecordAdapter(Report.this, (Activity) context, userId, result);
            transView.setAdapter(ra);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Object... text) {

        }
    }

}
