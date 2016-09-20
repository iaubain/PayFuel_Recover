package features;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

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
        pName.setText(pm.getName());


        return gridViewElement;
    }
}
