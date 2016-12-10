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
import com.aub.oltranz.payfuel.Selling;

import java.util.List;

import databaseBean.DBHelper;
import entities.Nozzle;
import entities.SellingTransaction;
import models.TransactionPrint;
import modules.TransactionPrintModule;

/**
 * Created by Owner on 6/10/2016.
 */
public class RecordAdapter extends ArrayAdapter<SellingTransaction> implements TransactionPrintModule.TransactionPrintInteraction {
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
        else if(st.getStatus()==301 || st.getStatus()==302){
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

        if(st.getStatus() == 100){
            try{
                TransactionPrintModule transactionPrintModule = new TransactionPrintModule(RecordAdapter.this, context, userId, db, st);
                transactionPrintModule.generateReceipt();
            }catch (Exception e){
                e.printStackTrace();
                uiFeedBack("Error: "+e.getCause());
            }
        }
    }

    public void uiFeedBack(String message){
        Toast.makeText(context, message , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printResult(String printingMessage) {
        uiFeedBack(printingMessage);
    }
}
