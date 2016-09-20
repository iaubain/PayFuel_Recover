package features;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aub.oltranz.payfuel.R;

import java.util.List;

import databaseBean.DBHelper;
import entities.Nozzle;
import entities.SellingTransaction;
import models.TransactionPrint;

/**
 * Created by Owner on 6/10/2016.
 */
public class RecordAdapter extends ArrayAdapter<SellingTransaction> {
    String tag="PayFuel: "+getClass().getSimpleName();
    private final Activity context;
    private final int userId;
    DBHelper db;
    private final List<SellingTransaction> sts;

    public RecordAdapter(Activity context, int userId, List<SellingTransaction> sts) {
        super(context, R.layout.record_style, sts);
        Log.d(tag, "Construct Transaction List Adapter");

        this.context=context;
        this.userId=userId;
        this.sts=sts;
        db=new DBHelper(context);
    }

    public View getView(int position,View view,ViewGroup parent) {
        Log.d(tag, "Transaction Row " + position + " Default Values Handle");
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.record_style, null, true);

        final SellingTransaction st= sts.get(position);

        TextView transId=(TextView) rowView.findViewById(R.id.transid);
        TextView prodInfo=(TextView) rowView.findViewById(R.id.prodinfo);
        TextView payInfo=(TextView) rowView.findViewById(R.id.payinfo);

        final Button print=(Button) rowView.findViewById(R.id.print);

        transId.setText(String.valueOf(st.getDeviceTransactionId()));
        String prodName="product";
        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
        if(nozzle != null && nozzle.getProductName() != null)
            prodName=db.getSingleNozzle(st.getNozzleId()).getProductName();

        prodInfo.setText(db.getSingleNozzle(st.getNozzleId()).getNozzleName()+" /"+prodName+" /"+st.getQuantity()+"L /"+st.getPlateNumber());
      //  prodInfo.setText(prodName+" /"+qty+"L /"+plate);
        if(st.getStatus()==100 || st.getStatus()==101){
            payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Succeeded");
            payInfo.setTextColor(context.getResources().getColor(R.color.positive));
        }
        else if(st.getStatus()==301){
            payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Pending");
            payInfo.setTextColor(context.getResources().getColor(R.color.tab_highlight));

            print.setEnabled(false);
            print.setClickable(false);
            print.setBackground(context.getResources().getDrawable(R.drawable.button_shape_negative));
            print.setTextColor(context.getResources().getColor(R.color.nearblack));
        }else{
            payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Failed");
            payInfo.setTextColor(context.getResources().getColor(R.color.error));

            print.setEnabled(false);
            print.setClickable(false);
            print.setBackground(context.getResources().getDrawable(R.drawable.button_shape_negative));
            print.setTextColor(context.getResources().getColor(R.color.nearblack));
        }

        if(st.getStatus()==500){
            print.setEnabled(false);
            print.setClickable(false);
            print.setBackground(context.getResources().getDrawable(R.drawable.button_shape_negative));
            print.setTextColor(context.getResources().getColor(R.color.nearblack));
        }

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printAction(st.getDeviceTransactionId());
            }
        });
        return rowView;
    }

    public void printAction(final long traId){
        final SellingTransaction st=db.getSingleTransaction(traId);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.v(tag,"Running a printing thread");
                try{
                    if(st.getStatus()==100 || st.getStatus()==101){

                        TransactionPrint tp=new TransactionPrint();

                        tp.setAmount(st.getAmount());
                        tp.setQuantity(st.getQuantity());
                        tp.setBranchName(db.getSingleUser(userId).getBranch_name());
                        tp.setDeviceId(db.getSingleDevice().getDeviceNo());
                        tp.setUserName(db.getSingleUser(userId).getName());
                        tp.setDeviceTransactionId(String.valueOf(traId));
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
                                uiFeedBack("Failed to generate receipt: "+st.getStatus());
                            }
                        }
                    }else if(st.getStatus()==301){
                        st.setStatus(302);
                        long dbId=db.updateTransaction(st);
                        if(dbId <=0)
                            uiFeedBack("Failed to generate receipt: "+traId);
                    }
                }catch (Exception e){
                    uiFeedBack(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    public void uiFeedBack(String message){
        Toast.makeText(context, message , Toast.LENGTH_SHORT).show();
    }
}
