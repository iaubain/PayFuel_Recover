package features;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.mysppayfuel.R;

import java.util.List;

import appBean.GridData;
import entities.PaymentMode;

/**
 * Created by Owner on 5/17/2016.
 */
public class PaymentAdapter extends BaseAdapter {
    String tag="PayFuel: "+PaymentAdapter.class.getSimpleName();

    private Context context;
    private final List<PaymentMode> mData;

    public PaymentAdapter(Context context, List<PaymentMode> mData) {
        Log.d(tag,"Initialise griview Content");
        this.context = context;
        this.mData = mData;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Log.d(tag,"Grid View constructing");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridViewElement = new View(context);

        // get layout Style
        gridViewElement = inflater.inflate(R.layout.paymode_style, null);

        // set values
        TextView pId = (TextView) gridViewElement.findViewById(R.id.pid);
        TextView pName = (TextView) gridViewElement.findViewById(R.id.pname);

        PaymentMode pm=new PaymentMode();
        pm=mData.get(position);
        pId.setText(String.valueOf(pm.getPaymentModeId()));
        if(pm.getName().equalsIgnoreCase("cash")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.cash, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("voucher")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.voucher, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("mtn")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.mtnmobilemoney, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("tigo")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.tigocash, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("airtel")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.airtel, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("visa")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.visa, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("master")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.mastercard, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("debt")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.debt, 0, 0);
        }else if(pm.getName().equalsIgnoreCase("engen card") || pm.getName().equalsIgnoreCase("sp card")){
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.engenonecard, 0, 0);
        }else{
            pName.setText(pm.getName());
            pName.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.cash, 0, 0);
        }

        return gridViewElement;
    }
}
