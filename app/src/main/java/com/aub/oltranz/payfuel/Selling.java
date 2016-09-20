package com.aub.oltranz.payfuel;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import appBean.GridData;
import databaseBean.DBHelper;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.PaymentMode;
import entities.Pump;
import entities.SellingTransaction;
import entities.WorkStatus;
import features.PaymentAdapter;
import features.PrintHandler;
import features.RecordAdapter;
import features.StatusAdapter;
import models.TransactionPrint;
import progressive.Confirmation;
import progressive.PayDetails;
import progressive.PumpDetails;
import progressive.TransValue;
import progressive.TransactionFeedsInterface;
import progressive.TransactionProcess;

public class Selling extends ActionBarActivity implements AdapterView.OnItemClickListener, TransactionFeedsInterface {

    String tag="PayFuel: "+getClass().getSimpleName();
    int userId;
    int branchId;
    boolean receipt=false;

    TextView tv;
    GridView gv;

    Bundle savedBundle;
    Context context;
    TextWatcher watchAmount, watchQuantity;

    DBHelper db;
    StatusAdapter sAdapter;
    Confirmation confirm;
    PayDetails payDetails;
    PumpDetails pDetails;
    TransValue transValue;
    SellingTransaction sTransaction;

    StrictMode.ThreadPolicy policy;
    Dialog dialog;

    //Transaction valiables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();

        setContentView(R.layout.activity_selling);

        //initialize app Components
        initAppComponents();

        //initialize app UI
        initAppUI();
    }

    public void initAppUI(){
        Log.d(tag, "Initializing Activity UI");

        tv=(TextView) findViewById(R.id.tv);
        gv=(GridView) findViewById(R.id.choosenlist);

        savedBundle =getIntent().getExtras();
        if(savedBundle!=null){
        String extra= savedBundle.getString(getResources().getString(R.string.userid));
        userId=Integer.parseInt(extra);
        }

        context=this;

        try{
            Logged_in_user user=db.getSingleUser(userId);
            branchId=user.getBranch_id();

            getPumpList(workStatusList(userId));
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh(){
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
        sendBroadcast(i);
    }

    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        db=new DBHelper(this);
        dialog=new Dialog(this);
    }

    public List<WorkStatus> workStatusList(int userId){
        Log.d(tag, "Getting Selected Pumps and their Nozzles");
        List<WorkStatus> statuses=db.getAllStatus(userId);
        return statuses;
    }

    public void getPumpList(List<WorkStatus> statuses){
        Log.d(tag, "Populating pump and their nozzle from workstatus");
        List<String> tempPumpId=new ArrayList<String>();

        GridData gridData;

        List<GridData> gridDataList=new ArrayList<GridData>();
        try{
            Iterator iterator=statuses.iterator();
            while (iterator.hasNext()){
                WorkStatus ws=new WorkStatus();
                ws=(WorkStatus) iterator.next();

                //check if the nozzle id is there or is not coming more than one time
                if(tempPumpId.isEmpty()||tempPumpId==null || (!tempPumpId.contains(String.valueOf(ws.getNozzleId()))) ){
                    tempPumpId.add(String.valueOf(ws.getNozzleId()));

                    if(ws.getStatusCode()==2){
                        Nozzle nozzle=db.getSingleNozzle(ws.getNozzleId());
                        Pump pump=db.getSinglePump(ws.getPumpId());

                        gridData=new GridData();

                        gridData.setPumpId(pump.getPumpId());
                        gridData.setPumpName(pump.getPumpName());
                        gridData.setNozzleId(nozzle.getNozzleId());
                        gridData.setNozzleName(nozzle.getNozzleName());
                        gridData.setPrice(nozzle.getUnitPrice());
                        gridData.setProduct(nozzle.getProductName());
                        gridData.setProductId(nozzle.getProductId());
                        gridData.setIndex(String.valueOf(nozzle.getNozzleIndex()));

                        gridDataList.add(gridData);
                    }
                }
            }
            sAdapter=new StatusAdapter(this,gridDataList);
            gv.setAdapter(sAdapter);
            gv.setOnItemClickListener(this);
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.v(tag, "You clicked Pump: " + ((TextView) view.findViewById(R.id.pumpid)).getText() + " And Nozzle: " + ((TextView) view.findViewById(R.id.nozzleid)).getText());

        //initiating progressive transaction Values
        initiateValue();

        pDetails.setIndex(((TextView) view.findViewById(R.id.index)).getText().toString());
        pDetails.setNozzleId(Integer.parseInt(((TextView) view.findViewById(R.id.nozzleid)).getText().toString()));
        pDetails.setUserId(userId);
        pDetails.setBranchId(branchId);
        pDetails.setPrice(Double.parseDouble(((TextView) view.findViewById(R.id.price)).getText().toString()));
        pDetails.setProductId(Integer.parseInt(((TextView) view.findViewById(R.id.productid)).getText().toString()));
        pDetails.setPumpId(Integer.parseInt(((TextView) view.findViewById(R.id.pumpid)).getText().toString()));

        setAmntOrQty(pDetails);
    }

    public  void setAmntOrQty(final PumpDetails pDetails){
        Log.d(tag, "Setting the amount and quantity");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog.setContentView(R.layout.set_amnt_qty);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Amount and Quantity</font>"));

        final Button pay=(Button) dialog.findViewById(R.id.pay);
        final Button cancel=(Button) dialog.findViewById(R.id.cancel);

        final TextView tv=(TextView) dialog.findViewById(R.id.tv);
        final TextView what=(TextView) dialog.findViewById(R.id.what);

        final EditText amnt=(EditText) dialog.findViewById(R.id.amnt);
        final EditText qty=(EditText) dialog.findViewById(R.id.qty);
        final EditText plateNumber=(EditText) dialog.findViewById(R.id.platenumber);
        plateNumber.setAllCaps(true);
        plateNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        final EditText tin=(EditText) dialog.findViewById(R.id.tin);
        final EditText companyName=(EditText) dialog.findViewById(R.id.name);

        //_______________Setting text Watcher_______________\\

        final double unityPrice=pDetails.getPrice();

        //Setting Quantity when Amount is Changed
        watchAmount=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                qty.removeTextChangedListener(watchQuantity);
                qty.setText("");
                double amount=0;

                //check length of text box
                int textLength=amnt.getText().toString().length();
                if(textLength>=7 && textLength%2==0){
                    float textSize=amnt.getTextSize();
                    if(textSize>=12)
                        amnt.setTextSize(textSize+2);
                }

                //check length of text box
                if(amnt.getText().toString().length()<=6)
                    amnt.setTextSize(20);
                else if(amnt.getText().toString().length()==8)
                    amnt.setTextSize(18);
                else if(amnt.getText().toString().length()>=10)
                    amnt.setTextSize(16);
                else if(amnt.getText().toString().length()>=12)
                    amnt.setTextSize(14);
                else if(amnt.getText().toString().length()>=14)
                    amnt.setTextSize(12);

                try{


                    amount=Double.parseDouble(amnt.getText().toString());

                    if ((unityPrice != 0) && (amount>0)) {
                        double quantity = Double.parseDouble(amnt.getText().toString()) / unityPrice;
                        NumberFormat numberFormat=NumberFormat.getInstance();
                        numberFormat.setMaximumFractionDigits(2);

                        //purifying double value
                        String doubleString=String.valueOf(numberFormat.format(quantity));
                        qty.setText(String.valueOf(doubleString.replaceAll(",", "")));

                    } else if(unityPrice <= 0)
                        tv.setText("Revise reinitialise the app");
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                qty.addTextChangedListener(watchQuantity);
            }
        };
        amnt.addTextChangedListener(watchAmount);

        //Setting Amount when Quantity is Changed
        watchQuantity=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                amnt.removeTextChangedListener(watchAmount);
                amnt.setText("");
                double quantity=0;

                //check length of text box
                if(qty.getText().toString().length()<=6)
                    qty.setTextSize(20);
                else if(qty.getText().toString().length()==8)
                    qty.setTextSize(18);
                else if(qty.getText().toString().length()>=10)
                    qty.setTextSize(16);
                else if(qty.getText().toString().length()>=12)
                    qty.setTextSize(14);
                else if(qty.getText().toString().length()>=14)
                    qty.setTextSize(12);

                try{

                    quantity=Double.parseDouble(qty.getText().toString());

                    if ((unityPrice != 0) && (quantity>0)) {//remove 0 and set >=1
                        double amount = Double.parseDouble(qty.getText().toString()) * unityPrice;
                        NumberFormat numberFormat=NumberFormat.getInstance();
                        numberFormat.setMaximumFractionDigits(2);


                        //purifying double value
                        String doubleString=String.valueOf(numberFormat.format(amount));
                        amnt.setText(String.valueOf(doubleString.replaceAll(",", "")));
                    } else if(unityPrice <= 0)
                        tv.setText("Reinitialise the app");
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                amnt.addTextChangedListener(watchAmount);
            }
        };
        qty.addTextChangedListener(watchQuantity);


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check compulsory fields AMOUNT and QUANTITY
                if(!TextUtils.isEmpty(amnt.getText().toString())&&(!TextUtils.isEmpty(qty.getText().toString()))){
                    transValue.setAmnt(Double.parseDouble(amnt.getText().toString()));
                    transValue.setQty(Double.parseDouble(qty.getText().toString()));

                    //verify the plate number
                    if(!TextUtils.isEmpty(plateNumber.getText().toString())){
                        transValue.setPlateNumber(plateNumber.getText().toString());
                    }else{
                        transValue.setPlateNumber("N/A");
                    }

                    //verify the tin
                    if(tin.isShown() && (!TextUtils.isEmpty(tin.getText().toString()))){
                        transValue.setTin(tin.getText().toString());
                    }else{
                        transValue.setTin("N/A");
                    }

                    //verify the Company Name
                    if(companyName.isShown() && (!TextUtils.isEmpty(companyName.getText().toString()))){
                        transValue.setName(companyName.getText().toString());
                    }else{
                        transValue.setName("N/A");
                    }

                    //pushing the result to the next pop up
                    setPaymentMode(pDetails, transValue);

                }else{
                    //mandatory fields has the empty value
                    tv.setText(getResources().getString(R.string.invaliddata));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetValue();
                dialog.dismiss();
            }
        });

        what.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!tin.isShown())
                    tin.setVisibility(View.VISIBLE);
                else
                    tin.setVisibility(View.GONE);

                if (!companyName.isShown())
                    companyName.setVisibility(View.VISIBLE);
                else
                    companyName.setVisibility(View.GONE);

                if (what.getText().toString().equalsIgnoreCase("Company?"))
                    what.setText("^hide^");
                else
                    what.setText("Company?");
            }
        });

        dialog.show();
    }

    public void setPaymentMode(final PumpDetails pumpDetails, final TransValue transValue){
        Log.d(tag, "Setting payment mode");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog.setContentView(R.layout.payment_modes);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Payment Modes</font>"));

        final Button cancel=(Button) dialog.findViewById(R.id.cancel);
        final GridView paymentGrid=(GridView) dialog.findViewById(R.id.paymentlist);
        final TextView tv=(TextView) dialog.findViewById(R.id.tv);

        List<PaymentMode> paymentModeList=db.getAllPaymentMode();
        if(!paymentModeList.isEmpty()){
            paymentGrid.setAdapter(new PaymentAdapter(this,paymentModeList));
            paymentGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.v(tag, "You selected Payment mode: " + ((TextView) view.findViewById(R.id.pname)).getText() + " with id: " + ((TextView) view.findViewById(R.id.pid)).getText());
                    payDetails.setPayId(Integer.parseInt(((TextView) view.findViewById(R.id.pid)).getText().toString()));
                    //check type of payment to put extra value

                    //push value to next Popup CONFIRM
                    if(((TextView) view.findViewById(R.id.pname)).getText().toString().equalsIgnoreCase("cash")||((TextView) view.findViewById(R.id.pname)).getText().toString().equalsIgnoreCase("debt"))
                        setConfirm(pumpDetails, transValue, payDetails);
                    else
                        setExtra(pumpDetails, transValue, payDetails);
                }
            });

        }else{
            tv.setText(getResources().getString(R.string.emptypaymode));
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payDetails=new PayDetails();
                setAmntOrQty(pumpDetails);
            }
        });
        dialog.show();
    }

    public void setExtra(final PumpDetails pumpDetails, final TransValue transValue, final PayDetails payDetails){
        Log.d(tag, "Setting Extra value for Transaction");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }

        PaymentMode paymentMode=db.getSinglePaymentMode(payDetails.getPayId());

        //
        if(paymentMode.getName().equalsIgnoreCase("MTN") || paymentMode.getName().equalsIgnoreCase("AIRTEL") || paymentMode.getName().equalsIgnoreCase("TIGO")){
            dialog.setContentView(R.layout.telephone_layout);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
            dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Fill the Number</font>"));
            final TextView tv=(TextView) dialog.findViewById(R.id.tv);
            final EditText tel=(EditText) dialog.findViewById(R.id.tel);
            Button done=(Button) dialog.findViewById(R.id.done);
            Button cancel=(Button) dialog.findViewById(R.id.cancel);
            if(paymentMode.getName().equalsIgnoreCase("MTN")){
                tel.setText("");
                tel.append("078");
            }
            if(paymentMode.getName().equalsIgnoreCase("TIGO")){
                tel.setText("");
                tel.append("072");
            }
            if(paymentMode.getName().equalsIgnoreCase("AIRTEL")){
                tel.setText("");
                tel.append("073");
            }

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tel.getText().toString().length()<10){
                        tv.setText("Invalid number");
                        String telNum=tel.getText().toString();
                    }else if(tel.getText().toString().length()>=10){
                        String telNum=tel.getText().toString();
                        telNum=telNum.replace("+","");

                        String prefix=telNum.substring(0,3);

                        if((prefix.equalsIgnoreCase("+250")||telNum.equalsIgnoreCase("2507")||(prefix.contains("+"))||prefix.contains("25")) && telNum.length()>=10){
                            payDetails.setTel(telNum);
                        } else{
                                payDetails.setTel("25"+telNum);
                        }

                        setConfirm(pumpDetails, transValue, payDetails);
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPaymentMode(pumpDetails, transValue);
                }
            });
        }else if(paymentMode.getName().equalsIgnoreCase("voucher")){
            dialog.setContentView(R.layout.extra_layout);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
            dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Fill The Card Number</font>"));
            final TextView tv=(TextView) dialog.findViewById(R.id.tv);
            final EditText tel=(EditText) dialog.findViewById(R.id.tel);
            final EditText plateNumber=(EditText) dialog.findViewById(R.id.platenumber);
            plateNumber.setAllCaps(true);
            plateNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            Button done=(Button) dialog.findViewById(R.id.done);
            Button cancel=(Button) dialog.findViewById(R.id.cancel);

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tel.getText().toString().length()<=0 || plateNumber.getText().toString().length()<=0){
                        //tv.setText("Invalid Card number");
                        //Remove the N/A values once on real envirnment
                        payDetails.setVoucher("123");
                        transValue.setPlateNumber("N/A");
                        setConfirm(pumpDetails, transValue, payDetails);
                    }else{
                        payDetails.setVoucher(tel.getText().toString());
                        transValue.setPlateNumber(plateNumber.getText().toString());
                        setConfirm(pumpDetails, transValue, payDetails);
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPaymentMode(pumpDetails,transValue);
                }
            });
        }else{
            setConfirm(pumpDetails, transValue, payDetails);
        }


        dialog.show();
    }

    public void setConfirm(final PumpDetails pDetails, final TransValue tValue, final PayDetails payD){
        Log.d(tag, "Confirming Transaction");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog.setContentView(R.layout.confirm_layout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Confirm Transaction</font>"));

        final Button cancel=(Button) dialog.findViewById(R.id.cancel);
        final Button accept=(Button) dialog.findViewById(R.id.done);
        final ListView translist=(ListView) dialog.findViewById(R.id.transdetails);
        final TextView tv=(TextView) dialog.findViewById(R.id.tv);
        final int[] clickCount = {0};

        List<String> transactionData=new ArrayList<String>();
//        final PumpDetails pDetails, final TransValue transValue, final PayDetails payD
        final Pump pump=db.getSinglePump(pDetails.getPumpId());
        final Nozzle nozzle=db.getSingleNozzle(pDetails.getNozzleId());
        final PaymentMode pm=db.getSinglePaymentMode(payD.getPayId());
        Logged_in_user user=db.getSingleUser(pDetails.getUserId());


        transactionData.add("Pump Name: "+pump.getPumpName());
        transactionData.add("Nozzle Name:"+ nozzle.getNozzleName());
        transactionData.add("Product: "+nozzle.getProductName());
        transactionData.add("");
        transactionData.add("Amount: "+tValue.getAmnt());
        transactionData.add("Quantity: "+tValue.getQty());
        transactionData.add("");
        transactionData.add("Plate Number: " + tValue.getPlateNumber());
        transactionData.add("Company Name: " + tValue.getName());
        transactionData.add("Tin: " + tValue.getTin());
        transactionData.add("");
        transactionData.add("Payment Mode: " + pm.getDescr());
        transactionData.add("Authentication Code: #####");
        transactionData.add("Authorisation Number: #####");
        transactionData.add("");
        transactionData.add("Served by: " + user.getName());
        transactionData.add("Petrol Station: " + user.getBranch_name());
        ArrayAdapter<String> transListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, transactionData);
        translist.setAdapter(transListAdapter);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payDetails = new PayDetails();
                setPaymentMode(pDetails, tValue);
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.v(tag,"Loading Transaction Logs");
                        try{

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(clickCount[0] <=0){
                                        sTransaction.setUserId(userId);
                                        sTransaction.setBranchId(branchId);
                                        sTransaction.setDeviceNo(db.getSingleDevice().getDeviceNo());
                                        sTransaction.setProductId(nozzle.getProductId());
                                        sTransaction.setPaymentModeId(pm.getPaymentModeId());
                                        sTransaction.setNozzleId(nozzle.getNozzleId());
                                        sTransaction.setPumpId(pump.getPumpId());
                                        sTransaction.setAmount(tValue.getAmnt());
                                        sTransaction.setQuantity(tValue.getQty());
                                        sTransaction.setPlateNumber(tValue.getPlateNumber());
                                        sTransaction.setTelephone(payD.getTel());
                                        sTransaction.setName(tValue.getName());
                                        sTransaction.setTin(tValue.getTin());
                                        sTransaction.setVoucherNumber(payD.getVoucher());
                                        sTransaction.setAuthenticationCode(payD.getAuthentCode());
                                        sTransaction.setAuthorisationCode(payD.getAuthorCode());

                                        setReceipt(startSellingProcess(sTransaction));

                                        clickCount[0] +=1;
                                    }else{
                                        //do something when tries to click more than one time
                                        Log.e(tag,"Clicking more than one time same button");
                                    }
                                }
                            });

                        }catch (Exception e){
                            tv.setText("Error Occured");
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();

            }
        });

        dialog.show();
    }

    public void setReceipt(final long transactionId){
        Log.d(tag, "Setting receipt generator for transaction: "+transactionId);
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog.setContentView(R.layout.receipt_layout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Receipt Generator</font>"));

        final Button yes=(Button) dialog.findViewById(R.id.yes);
        final Button no=(Button) dialog.findViewById(R.id.no);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yes.setEnabled(false);
                yes.setClickable(false);
                no.setEnabled(false);
                no.setClickable(false);

                receipt=true;
                resetValue();
                final SellingTransaction st=db.getSingleTransaction(transactionId);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.v(tag,"Running a printing thread");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if(st.getStatus()==100 || st.getStatus()==101){

                                        TransactionPrint tp=new TransactionPrint();

                                        tp.setAmount(st.getAmount());
                                        tp.setQuantity(st.getQuantity());
                                        tp.setBranchName(db.getSingleUser(userId).getBranch_name());
                                        tp.setDeviceId(db.getSingleDevice().getDeviceNo());
                                        tp.setUserName(db.getSingleUser(userId).getName());
                                        tp.setDeviceTransactionId(String.valueOf(transactionId));
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
                                            String print=ph.transPrint();
                                            if(!print.equalsIgnoreCase("Success")){
                                                uiFeedBack(print);
                                            }
                                        }else{
                                            //Update the status to generate the the print out finally
                                            if(st.getStatus()!=500){
                                                st.setStatus(st.getStatus()+1);

                                                long dbId=db.updateTransaction(st);
                                                if(dbId<=0){
                                                    uiFeedBack("Failed to generate receipt");
                                                }
                                            }
                                        }
                                    }else if(st.getStatus()==301){
                                        st.setStatus(302);
                                        long dbId=db.updateTransaction(st);
                                        if(dbId <=0)
                                            uiFeedBack("Failed to generate receipt: "+transactionId);
                                    }
                                }catch (Exception e){
                                    uiFeedBack(e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                new Thread(runnable).start();


                //initialize activity UI
                //initAppUI();

                refresh();
                uiTransactionData(st);
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receipt = false;
                resetValue();

                SellingTransaction st=db.getSingleTransaction(transactionId);
                //initialize activity UI
                //initAppUI();
                refresh();
                uiTransactionData(st);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

   public void uiTransactionData(SellingTransaction st){
       if(st.getStatus()==100 || st.getStatus()==101)
           uiFeedBack("Successful Transaction: "+st.getDeviceTransactionId());
       else if(st.getStatus()==301 || st.getStatus()==302)
           uiFeedBack("Pending Transaction: " + st.getDeviceTransactionId());
       else if(st.getStatus()==500 || st.getStatus()==501)
           uiFeedBack("Cancelled Transaction: " + st.getDeviceTransactionId());
   }

    public void resetValue(){
        Log.d(tag,"Resetting Progressive Transaction Objects");
        confirm=null;
        payDetails=null;
        pDetails=null;
        transValue =null;
        sTransaction=null;
    }

    public void initiateValue(){
        Log.d(tag,"Initiating Progressive Transaction Objects");
        confirm=new Confirmation();
        payDetails=new PayDetails();
        pDetails=new PumpDetails();
        transValue =new TransValue();
        sTransaction=new SellingTransaction();
    }

    /**
     * uiFeedBack: User Interface Feedback
     * @param message
     */
    public void uiFeedBack(String message){
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tv.setText("");
//            }
//        }, 3000);
//        if(dialog.isShowing())
//            dialog.dismiss();
        if(message==null || TextUtils.isEmpty(message)){
            tv.setText(getResources().getString(R.string.nulluifeedback));
        }else{
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            tv.setText(message);
        }
    }

    public long startSellingProcess(SellingTransaction st){
        TransactionProcess tp=new TransactionProcess(this);
        long transactionId=tp.transactionDatas(this,st);
        return transactionId;
    }

    @Override
    public void feedsMessage(String message) {
        Log.d(tag, "Transaction Error: "+message);
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        uiFeedBack(message);
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
}
