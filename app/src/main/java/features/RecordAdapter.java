package features;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aub.oltranz.mysppayfuel.R;

import java.util.List;

import databaseBean.DBHelper;
import entities.Nozzle;
import entities.SellingTransaction;
import modules.TransactionPrintModule;

/**
 * Created by Owner on 6/10/2016.
 */
public class RecordAdapter extends ArrayAdapter<SellingTransaction> implements TransactionPrintModule.TransactionPrintInteraction {
    String tag="PayFuel: "+getClass().getSimpleName();
    private final Activity context;
    private final int userId;
    private DBHelper db;
    private final List<SellingTransaction> sts;
    private RecordAdapterInteraction mListener;

    public RecordAdapter(RecordAdapterInteraction mListener, Activity context, int userId, List<SellingTransaction> sts) {
        super(context, R.layout.record_style, sts);
        Log.d(tag, "Construct Transaction List Adapter");

        this.context=context;
        this.mListener=mListener;
        this.userId=userId;
        this.sts=sts;
        db=new DBHelper(context);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Log.d(tag, "Transaction Row " + position + " Default Values Handle");
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            LayoutInflater inflater = (context).getLayoutInflater();
            row = inflater.inflate(R.layout.record_style, parent, false);
            holder = new ViewHolder();

            holder.transId=(TextView) row.findViewById(R.id.transid);
            holder.prodInfo=(TextView) row.findViewById(R.id.prodinfo);
            holder.payInfo=(TextView) row.findViewById(R.id.payinfo);
            holder.refresh = (ImageView) row.findViewById(R.id.refresh);
            holder.print=(Button) row.findViewById(R.id.print);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final SellingTransaction st= sts.get(position);

        holder.transId.setText(String.valueOf(st.getDeviceTransactionId()));
        String prodName="product";
        Nozzle nozzle=db.getSingleNozzle(st.getNozzleId());
        if(nozzle != null && nozzle.getProductName() != null)
            prodName=db.getSingleNozzle(st.getNozzleId()).getProductName();

        holder.prodInfo.setText(db.getSingleNozzle(st.getNozzleId()).getNozzleName()+" /"+prodName+" /"+st.getQuantity()+"L /"+st.getPlateNumber());
      //  prodInfo.setText(prodName+" /"+qty+"L /"+plate);
        if(st.getStatus()==100 || st.getStatus()==101){
            holder.payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Succeeded");
            holder.payInfo.setTextColor(ContextCompat.getColor(context, R.color.positive));
        }
        else if(st.getStatus()==301 || st.getStatus()==302){
            holder.payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Pending");
            holder.payInfo.setTextColor(ContextCompat.getColor(context, R.color.darkgray));

            holder.print.setEnabled(false);
            holder.print.setClickable(false);
            holder.print.setBackground(ContextCompat.getDrawable(context,R.drawable.button_shape_negative));
            holder.print.setTextColor(ContextCompat.getColor(context, R.color.nearblack));
        }else{
            holder.payInfo.setText(st.getDeviceTransactionTime()+" /"+db.getSinglePaymentMode(st.getPaymentModeId()).getName()+" /"+st.getAmount()+"Rwf /Failed");
            holder.payInfo.setTextColor(ContextCompat.getColor(context, R.color.error));

            holder.print.setEnabled(false);
            holder.print.setClickable(false);
            holder.print.setBackground(ContextCompat.getDrawable(context, R.drawable.button_shape_negative));
            holder.print.setTextColor(ContextCompat.getColor(context, R.color.nearblack));
        }

        if(st.getStatus()==500){
            holder.print.setEnabled(false);
            holder.print.setClickable(false);
            holder.print.setBackground(ContextCompat.getDrawable(context, R.drawable.button_shape_negative));
            holder.print.setTextColor(ContextCompat.getColor(context, R.color.nearblack));
        }

        holder.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshTransaction(st);
            }
        });

        holder.print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printAction(st);
            }
        });
        return row;
    }

    private void refreshTransaction(SellingTransaction sellingTransaction){
        if(sellingTransaction != null)
            mListener.onRecordInteractionRefresh(true, sellingTransaction);
        else
            uiFeedBack("Empty Transaction");
    }

    private void printAction(SellingTransaction st){

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

    private void uiFeedBack(String message){
        Toast.makeText(context, message , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printResult(String printingMessage) {
        uiFeedBack(printingMessage);
    }


    private static class ViewHolder {
        TextView transId;
        TextView prodInfo;
        TextView payInfo;
        ImageView refresh;
        Button print;
    }

    public interface RecordAdapterInteraction {
        void onRecordInteractionRefresh(boolean refresh, SellingTransaction sellingTransaction);
    }
}
