package com.aub.oltranz.payfuel;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
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
import features.StatusAdapter;
import modules.TransactionPreparation;
import modules.TransactionPrintModule;
import progressive.Confirmation;
import progressive.PayDetails;
import progressive.PumpDetails;
import progressive.TransValue;
import progressive.TransactionFeedsInterface;
import progressive.TransactionProcess;
import utilities.MyAlarmManager;
import utilities.PeriodicTransactionService;

public class Selling extends ActionBarActivity implements AdapterView.OnItemClickListener,
        TransactionFeedsInterface,
        TransactionPreparation.TransactionPreparationInteraction,
        TransactionPrintModule.TransactionPrintInteraction {

    String tag = "PayFuel: " + getClass().getSimpleName();
    int userId;
    int branchId;
    boolean receipt = false;

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
    List<String> tempPumpId = new ArrayList<>();

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

    public void initAppUI() {
        Log.d(tag, "Initializing Activity UI");

        tv = (TextView) findViewById(R.id.popupTv);
        gv = (GridView) findViewById(R.id.choosenlist);

        savedBundle = getIntent().getExtras();
        if (savedBundle != null) {
            String extra = savedBundle.getString(getResources().getString(R.string.userid));
            userId = Integer.parseInt(extra);
        }

        context = this;

        try {
            Logged_in_user user = db.getSingleUser(userId);
            branchId = user.getBranch_id();

            getPumpList(workStatusList(userId));
        } catch (Exception e) {
            Log.e(tag, "Error Occurred. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh() {
        Intent i = new Intent("com.aub.oltranz.payfuel.MAIN_SERVICE").putExtra("msg", "refresh");
        sendBroadcast(i);
    }

    public void initAppComponents() {
        Log.d(tag, "Initializing Activity Components");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        db = new DBHelper(this);
        dialog = new Dialog(this);
    }

    public List<WorkStatus> workStatusList(int userId) {
        Log.d(tag, "Getting Selected Pumps and their Nozzles");
        List<WorkStatus> statuses = db.getAllStatus(userId);
        return statuses;
    }

    public void getPumpList(List<WorkStatus> statuses) {
        Log.d(tag, "Populating pump and their nozzle from workstatus");


        GridData gridData;

        List<GridData> gridDataList = new ArrayList<GridData>();
        try {

            for(WorkStatus ws : statuses){
                if (!tempPumpId.contains(String.valueOf(ws.getNozzleId()))) {
                    tempPumpId.add(String.valueOf(ws.getNozzleId()));
                    if (ws.getStatusCode() == 2) {
                        Nozzle nozzle = db.getSingleNozzle(ws.getNozzleId());
                        Pump pump = db.getSinglePump(ws.getPumpId());

                        gridData = new GridData();

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
//            Iterator iterator = statuses.iterator();
//            while (iterator.hasNext()) {
//                WorkStatus ws = new WorkStatus();
//                ws = (WorkStatus) iterator.next();
//
//                //check if the nozzle id is there or is not coming more than one time
//                if (tempPumpId.isEmpty() || (!tempPumpId.contains(String.valueOf(ws.getNozzleId())))) {
//                    tempPumpId.add(String.valueOf(ws.getNozzleId()));
//
//                    if (ws.getStatusCode() == 2) {
//                        Nozzle nozzle = db.getSingleNozzle(ws.getNozzleId());
//                        Pump pump = db.getSinglePump(ws.getPumpId());
//
//                        gridData = new GridData();
//
//                        gridData.setPumpId(pump.getPumpId());
//                        gridData.setPumpName(pump.getPumpName());
//                        gridData.setNozzleId(nozzle.getNozzleId());
//                        gridData.setNozzleName(nozzle.getNozzleName());
//                        gridData.setPrice(nozzle.getUnitPrice());
//                        gridData.setProduct(nozzle.getProductName());
//                        gridData.setProductId(nozzle.getProductId());
//                        gridData.setIndex(String.valueOf(nozzle.getNozzleIndex()));
//
//                        gridDataList.add(gridData);
//                    }
//                }
//            }
            sAdapter = new StatusAdapter(this, gridDataList);
            gv.setAdapter(sAdapter);
            gv.setOnItemClickListener(this);
        } catch (Exception e) {
            Log.e(tag, "Error Occurred. " + e.getMessage());
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

    public void setAmntOrQty(final PumpDetails pDetails) {
        Log.d(tag, "Setting the amount and quantity");
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = new Dialog(this);
        }
        dialog.setContentView(R.layout.set_amnt_qty);
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
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Amount and Quantity</font>"));

        final Button pay = (Button) dialog.findViewById(R.id.pay);
        final Button cancel = (Button) dialog.findViewById(R.id.cancel);

        final TextView tv = (TextView) dialog.findViewById(R.id.popupTv);
        tv.setText("");
        Spannable text;
        text = new SpannableString("Warning");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.error)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("/");

        text = new SpannableString("Success ");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("message appear here!");

        final TextView what = (TextView) dialog.findViewById(R.id.what);

        final EditText amnt = (EditText) dialog.findViewById(R.id.amnt);
        final TextView qty = (TextView) dialog.findViewById(R.id.qty);
        final EditText plateNumber = (EditText) dialog.findViewById(R.id.platenumber);
        plateNumber.setAllCaps(true);
        plateNumber.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        final EditText tin = (EditText) dialog.findViewById(R.id.tin);
        final EditText companyName = (EditText) dialog.findViewById(R.id.name);

        //_______________Setting text Watcher_______________\\

        final double unityPrice = pDetails.getPrice();

        qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amnt.setText("");
                setQtyBox(unityPrice);
            }
        });


        //Setting Quantity when Amount is Changed
        watchAmount = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //qty.removeTextChangedListener(watchQuantity);
                qty.setText("");
                double amount = 0;

                //check length of text box
//                int textLength = amnt.getText().toString().length();
//                if (textLength >= 7 && textLength % 2 == 0) {
//                    float textSize = amnt.getTextSize();
//                    if (textSize >= 12)
//                        amnt.setTextSize(textSize + 2);
//                }

                //check length of text box
                if (amnt.getText().toString().length() <= 6)
                    amnt.setTextSize(23);
                else if (amnt.getText().toString().length() == 8)
                    amnt.setTextSize(20);
                else if (amnt.getText().toString().length() >= 10)
                    amnt.setTextSize(17);
                else if (amnt.getText().toString().length() >= 12)
                    amnt.setTextSize(14);
                else if (amnt.getText().toString().length() >= 14)
                    amnt.setTextSize(11);

                try {


                    amount = Double.parseDouble(amnt.getText().toString());

                    if ((unityPrice != 0) && (amount > 0)) {
                        double quantity = Double.parseDouble(amnt.getText().toString()) / unityPrice;
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        numberFormat.setMaximumFractionDigits(2);

                        //purifying double value
                        String doubleString = String.valueOf(numberFormat.format(quantity));
                        qty.setText(String.valueOf(doubleString.replaceAll(",", "")));

                    } else if (unityPrice <= 0)
                        tv.setText("Revise reinitialise the app");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //qty.addTextChangedListener(watchQuantity);
            }
        };
        amnt.addTextChangedListener(watchAmount);

//        //Setting Amount when Quantity is Changed
//        watchQuantity = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                amnt.removeTextChangedListener(watchAmount);
//                amnt.setText("");
//                double quantity = 0;
//
//                //check length of text box
//                if (qty.getText().toString().length() <= 6)
//                    qty.setTextSize(15);
//                else if (qty.getText().toString().length() == 8)
//                    qty.setTextSize(13);
//                else if (qty.getText().toString().length() >= 10)
//                    qty.setTextSize(11);
//                else if (qty.getText().toString().length() >= 12)
//                    qty.setTextSize(9);
//                else if (qty.getText().toString().length() >= 14)
//                    qty.setTextSize(7);
//
//                try {
//
//                    quantity = Double.parseDouble(qty.getText().toString());
//
//                    if ((unityPrice != 0) && (quantity > 0)) {//remove 0 and set >=1
//                        double amount = Double.parseDouble(qty.getText().toString()) * unityPrice;
//                        NumberFormat numberFormat = NumberFormat.getInstance();
//                        numberFormat.setMaximumFractionDigits(2);
//
//
//                        //purifying double value
//                        String doubleString = String.valueOf(numberFormat.format(amount));
//                        amnt.setText(String.valueOf(doubleString.replaceAll(",", "")));
//                    } else if (unityPrice <= 0)
//                        tv.setText("Reinitialise the app");
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                amnt.addTextChangedListener(watchAmount);
//            }
//        };
//        qty.addTextChangedListener(watchQuantity);


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check compulsory fields AMOUNT and QUANTITY
                if (!TextUtils.isEmpty(amnt.getText().toString()) && (!TextUtils.isEmpty(qty.getText().toString()))) {
                    transValue.setAmnt(Double.parseDouble(amnt.getText().toString()));
                    transValue.setQty(Double.parseDouble(qty.getText().toString()));

                    //verify the plate number
                    if (!TextUtils.isEmpty(plateNumber.getText().toString())) {
                        transValue.setPlateNumber(plateNumber.getText().toString());
                    } else {
                        transValue.setPlateNumber("N/A");
                    }

                    //verify the tin
                    if (tin.isShown() && (!TextUtils.isEmpty(tin.getText().toString()))) {
                        transValue.setTin(tin.getText().toString());
                    } else {
                        transValue.setTin("N/A");
                    }

                    //verify the Company Name
                    if (companyName.isShown() && (!TextUtils.isEmpty(companyName.getText().toString()))) {
                        transValue.setName(companyName.getText().toString());
                    } else {
                        transValue.setName("N/A");
                    }

                    //pushing the result to the next pop up
                    setPaymentMode(pDetails, transValue);

                } else {
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

                if (!plateNumber.isShown())
                    plateNumber.setVisibility(View.VISIBLE);
                else
                    plateNumber.setVisibility(View.GONE);

                if (what.getText().toString().equalsIgnoreCase("Click here to more info if available!"))
                    what.setText("Click here to view less info!");
                else
                    what.setText("Click here to more info if available!");
            }
        });

        dialog.show();
    }

    private void setQtyBox(final double unityPrice){
        try {
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.quantity_ui_popup, null);
            final EditText userQty = (EditText) promptsView.findViewById(R.id.quantity);
            final TextView popTv=(TextView) promptsView.findViewById(R.id.qtyTv);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.quantityPop)
                    .setView(promptsView);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int id) {
                    if(!TextUtils.isEmpty(userQty.getText().toString().trim())){
                        if(dialog != null)
                            if(dialog.isShowing()){
                                Double quantity;
                                final EditText amnt = (EditText) dialog.findViewById(R.id.amnt);
                                final TextView qty = (TextView) dialog.findViewById(R.id.qty);
                                qty.setText(userQty.getText().toString());

                                if (qty.getText().toString().length() <= 6)
                                    qty.setTextSize(15);
                                else if (qty.getText().toString().length() == 8)
                                    qty.setTextSize(13);
                                else if (qty.getText().toString().length() >= 10)
                                    qty.setTextSize(11);
                                else if (qty.getText().toString().length() >= 12)
                                    qty.setTextSize(9);
                                else if (qty.getText().toString().length() >= 14)
                                    qty.setTextSize(7);

                                try {

                                    quantity = Double.parseDouble(qty.getText().toString());

                                    if ((unityPrice != 0) && (quantity > 0) && (quantity<=2000)) {//remove 0 and set >=1
                                        double amount = Double.parseDouble(qty.getText().toString()) * unityPrice;
                                        NumberFormat numberFormat = NumberFormat.getInstance();
                                        numberFormat.setMaximumFractionDigits(2);


                                        //purifying double value
                                        String doubleString = String.valueOf(numberFormat.format(amount));
                                        amnt.setText(String.valueOf(doubleString.replaceAll(",", "")));

                                        //check length of text box
                                        if (amnt.getText().toString().length() <= 6)
                                            amnt.setTextSize(23);
                                        else if (amnt.getText().toString().length() == 8)
                                            amnt.setTextSize(20);
                                        else if (amnt.getText().toString().length() >= 10)
                                            amnt.setTextSize(17);
                                        else if (amnt.getText().toString().length() >= 12)
                                            amnt.setTextSize(14);
                                        else if (amnt.getText().toString().length() >= 14)
                                            amnt.setTextSize(11);

                                        dialogInterface.dismiss();
                                    } else if (unityPrice <= 0){
                                        qty.setText("");
                                        popTv.setText("Reinitialise the app");

                                    } else if(quantity<=2000){
                                        qty.setText("");
                                        popTv.setText("Quantity exceeded the limit");
                                    }else{
                                        qty.setText("");
                                        popTv.setText("Consider restart the app");
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Toast.makeText(Selling.this, "Internal Application error", Toast.LENGTH_SHORT).show();
                            }
                    } else{
                        popTv.setText("Invalid quantity");
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setPaymentMode(final PumpDetails pumpDetails, final TransValue transValue) {
        Log.d(tag, "Setting payment mode");
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = new Dialog(this);
        }
        dialog.setContentView(R.layout.payment_modes);
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
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Payment Modes</font>"));

        final Button cancel = (Button) dialog.findViewById(R.id.cancel);
        final GridView paymentGrid = (GridView) dialog.findViewById(R.id.paymentlist);
        final TextView tv = (TextView) dialog.findViewById(R.id.popupTv);
        tv.setText("");
        Spannable text;
        text = new SpannableString("Warning");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.error)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("/");

        text = new SpannableString("Success ");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("message appear here!");

        List<PaymentMode> paymentModeList = db.getAllPaymentMode();
        if (!paymentModeList.isEmpty()) {
            paymentGrid.setAdapter(new PaymentAdapter(this, paymentModeList));
            paymentGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.v(tag, "You selected Payment mode: " + ((TextView) view.findViewById(R.id.pname)).getText() + " with id: " + ((TextView) view.findViewById(R.id.pid)).getText());
                    payDetails.setPayId(Integer.parseInt(((TextView) view.findViewById(R.id.pid)).getText().toString()));
                    //check type of payment to put extra value

                    //push value to next Popup CONFIRM
                    if (((TextView) view.findViewById(R.id.pname)).getText().toString().equalsIgnoreCase("cash") || ((TextView) view.findViewById(R.id.pname)).getText().toString().equalsIgnoreCase("debt"))
                        setConfirm(pumpDetails, transValue, payDetails);
                    else
                        setExtra(pumpDetails, transValue, payDetails);
                }
            });

        } else {
            tv.setText(getResources().getString(R.string.emptypaymode));
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payDetails = new PayDetails();
                setAmntOrQty(pumpDetails);
            }
        });
        dialog.show();
    }

    public void setExtra(final PumpDetails pumpDetails, final TransValue transValue, final PayDetails payDetails) {
        Log.d(tag, "Setting Extra value for Transaction");
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = new Dialog(this);
        }

        PaymentMode paymentMode = db.getSinglePaymentMode(payDetails.getPayId());

        //
        if (paymentMode.getName().equalsIgnoreCase("MTN") || paymentMode.getName().equalsIgnoreCase("AIRTEL") || paymentMode.getName().equalsIgnoreCase("TIGO")) {
            View content = getLayoutInflater().inflate(R.layout.telephone_layout, null);
            dialog.setContentView(content);
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
            dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Fill the Number</font>"));
            final TextView tv = (TextView) dialog.findViewById(R.id.popupTv);
            tv.setText("");
            Spannable text;
            text = new SpannableString("Warning");
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.error)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(text);

            tv.append("/");

            text = new SpannableString("Success ");
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(text);

            tv.append("message appear here!");
            final EditText tel = (EditText) dialog.findViewById(R.id.tel);
            Button done = (Button) dialog.findViewById(R.id.done);
            Button cancel = (Button) dialog.findViewById(R.id.cancel);
            if (paymentMode.getName().equalsIgnoreCase("MTN")) {
                tel.setText("");
                tel.append("078");
            }
            if (paymentMode.getName().equalsIgnoreCase("TIGO")) {
                tel.setText("");
                tel.append("072");
            }
            if (paymentMode.getName().equalsIgnoreCase("AIRTEL")) {
                tel.setText("");
                tel.append("073");
            }

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (tel.getText().toString().trim().length() < 10) {
                        tv.setText("Invalid number");
                        String telNum = tel.getText().toString().trim();
                    } else if (tel.getText().toString().length() >= 10) {
                        String telNum = tel.getText().toString().trim();
                        telNum = telNum.replace("+", "");

                        String prefix = telNum.substring(0, 3);

                        if ((prefix.equalsIgnoreCase("+250") || telNum.equalsIgnoreCase("2507") || (prefix.contains("+")) || prefix.contains("25")) && telNum.length() >= 10) {
                            payDetails.setTel(telNum);
                        } else {
                            payDetails.setTel("25" + telNum);
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
        } else if (paymentMode.getName().equalsIgnoreCase("voucher")) {
            dialog.setContentView(R.layout.extra_layout);
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
            dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Fill The Card Number</font>"));
            final TextView tv = (TextView) dialog.findViewById(R.id.popupTv);
            final EditText tel = (EditText) dialog.findViewById(R.id.tel);
            final EditText plateNumber = (EditText) dialog.findViewById(R.id.platenumber);
            plateNumber.setAllCaps(true);
            plateNumber.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            Button done = (Button) dialog.findViewById(R.id.done);
            Button cancel = (Button) dialog.findViewById(R.id.cancel);

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(tel.getText().toString())) {
                        payDetails.setVoucher(tel.getText().toString().trim());
                    }else{
                        payDetails.setVoucher("123");
                    }

                    if(!TextUtils.isEmpty(plateNumber.getText().toString())){
                        transValue.setPlateNumber(plateNumber.getText().toString().trim());
                    }else{
                        transValue.setPlateNumber("N/A");
                    }

                    setConfirm(pumpDetails, transValue, payDetails);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPaymentMode(pumpDetails, transValue);
                }
            });
        } else {
            setConfirm(pumpDetails, transValue, payDetails);
        }


        dialog.show();
    }

    public void setConfirm(final PumpDetails pDetails, final TransValue tValue, final PayDetails payD) {
        Log.d(tag, "Confirming Transaction");
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = new Dialog(this);
        }
        dialog.setContentView(R.layout.confirm_layout);
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
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Confirm Transaction</font>"));

        final Button cancel = (Button) dialog.findViewById(R.id.cancel);
        final Button accept = (Button) dialog.findViewById(R.id.done);
        //final ListView translist = (ListView) dialog.findViewById(R.id.transdetails);
        final TextView translist = (TextView) dialog.findViewById(R.id.transdetails);
        Spannable text;
        final TextView tv = (TextView) dialog.findViewById(R.id.popupTv);
        tv.setText("");
        text = new SpannableString("Warning");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.error)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("/");

        text = new SpannableString("Success ");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.append(text);

        tv.append("message appear here!");
        final int[] clickCount = {0};

        List<String> transactionData = new ArrayList<String>();
//        final PumpDetails pDetails, final TransValue transValue, final PayDetails payD
        final Pump pump = db.getSinglePump(pDetails.getPumpId());
        final Nozzle nozzle = db.getSingleNozzle(pDetails.getNozzleId());
        final PaymentMode pm = db.getSinglePaymentMode(payD.getPayId());
        Logged_in_user user = db.getSingleUser(pDetails.getUserId());

        translist.setText("Amount ");
        text = new SpannableString(tValue.getAmnt()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Quantity  ");
        text = new SpannableString("  "+tValue.getQty()+ " \n\n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Payment Mode  ");
        text = new SpannableString("  "+pm.getDescr()+ " \n\n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Product  ");
        text = new SpannableString("  "+nozzle.getProductName()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Pump Name  ");
        text = new SpannableString("  "+pump.getPumpName()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Nozzle Name  ");
        text = new SpannableString("  "+nozzle.getNozzleName()+ " \n\n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Number Plate  ");
        text = new SpannableString("  "+tValue.getPlateNumber()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Company Name  ");
        text = new SpannableString("  "+tValue.getName()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("TIN  ");
        text = new SpannableString("  "+tValue.getTin()+ " \n\n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Authentication Code  ");
        text = new SpannableString("  #####"+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Authorisation Code  ");
        text = new SpannableString("  #####"+ " \n\n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Served By  ");
        text = new SpannableString("  "+user.getName()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        translist.append("Petrol Station  ");
        text = new SpannableString("  "+user.getBranch_name()+ " \n");
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.positive)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        translist.append(text);

        transactionData.add("Pump Name: " + pump.getPumpName());
        transactionData.add("Nozzle Name:" + nozzle.getNozzleName());
        transactionData.add("Product: " + nozzle.getProductName());
        transactionData.add("");
        transactionData.add("Amount: " + tValue.getAmnt());
        transactionData.add("Quantity: " + tValue.getQty());
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
        //ArrayAdapter<String> transListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, transactionData);
        //translist.setAdapter(transListAdapter);

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

                TransactionPreparation transactionPreparation =new TransactionPreparation(Selling.this, Selling.this, sTransaction, userId, db);
                transactionPreparation.startTransactionPreparation();
            }
        });

        dialog.show();
    }

    public void setReceipt(final SellingTransaction sellingTransaction) {
        Log.d(tag, "Setting receipt generator for transaction: " + sellingTransaction.getDeviceTransactionId());
        if (dialog.isShowing()) {
            dialog.dismiss();
            dialog = new Dialog(this);
        }
        dialog.setContentView(R.layout.receipt_layout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final long transactionId = sellingTransaction.getDeviceTransactionId();

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        try{
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Receipt Generator</font>"));

        final Button yes = (Button) dialog.findViewById(R.id.yes);
        final Button no = (Button) dialog.findViewById(R.id.no);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yes.setEnabled(false);
                yes.setClickable(false);
                no.setEnabled(false);
                no.setClickable(false);
                receipt = true;
                resetValue();
                final SellingTransaction st = db.getSingleTransaction(transactionId);
                if(st.getStatus() == 100){
                    try{
                        TransactionPrintModule transactionPrintModule = new TransactionPrintModule(Selling.this, Selling.this, userId, db, st);
                        transactionPrintModule.generateReceipt();
                    }catch (Exception e){
                        e.printStackTrace();
                        uiFeedBack("Error: "+e.getCause());
                    }
                }else{
                    st.setStatus(st.getStatus()+1);
                    db.updateTransaction(st);
                }
                uiTransactionData(st);
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receipt = false;
                resetValue();

                SellingTransaction st = db.getSingleTransaction(transactionId);
                //initialize activity UI
                uiTransactionData(st);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void uiTransactionData(SellingTransaction st) {
        if (st.getStatus() == 100 || st.getStatus() == 101)
            uiFeedBack("Successful Transaction: " + st.getDeviceTransactionId());
        else if (st.getStatus() == 301 || st.getStatus() == 302)
            uiFeedBack("Pending Transaction: " + st.getDeviceTransactionId());
        else if (st.getStatus() == 500 || st.getStatus() == 501)
            uiFeedBack("Cancelled Transaction: " + st.getDeviceTransactionId());
    }

    public void resetValue() {
        Log.d(tag, "Resetting Progressive Transaction Objects");
        confirm = null;
        payDetails = null;
        pDetails = null;
        transValue = null;
        sTransaction = null;
    }

    public void initiateValue() {
        Log.d(tag, "Initiating Progressive Transaction Objects");
        confirm = new Confirmation();
        payDetails = new PayDetails();
        pDetails = new PumpDetails();
        transValue = new TransValue();
        sTransaction = new SellingTransaction();
    }

    /**
     * uiFeedBack: User Interface Feedback
     *
     * @param message
     */
    public void uiFeedBack(String message) {
        try{
            if(!TextUtils.isEmpty(message)){
                tv.setVisibility(View.VISIBLE);
                tv.setText(message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("");
                        tv.setVisibility(View.INVISIBLE);
                    }
                }, 1000*5);// 5sec
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public long startSellingProcess(SellingTransaction st) {
        TransactionProcess tp = new TransactionProcess(this);
        long transactionId = tp.transactionDatas(this, st);
        return transactionId;
    }

    @Override
    public void feedsMessage(String message) {
        Log.d(tag, "Transaction Error: " + message);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        uiFeedBack(message);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // do nothing
            Log.e(tag, "action:" + "Menu Key Pressed");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do nothing on back key presssed
            Log.e(tag, "action:" + "Back Key Pressed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onTransactionPreparation(boolean status, SellingTransaction sellingTransaction) {
        if(!status){
            //failure during transaction recording
            Log.e(tag,"Failed to record transaction");
        }else{
            setReceipt(sellingTransaction);
        }
    }

    @Override
    public void printResult(String printingMessage) {
        uiFeedBack(printingMessage);
    }

    public interface SellingInteraction{
        void onSellingInteraction(int status, String message, SellingTransaction sellingTransaction);
    }
}
