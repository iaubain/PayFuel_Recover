package features;

import android.content.Context;
import android.graphics.Typeface;
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
import databaseBean.DBHelper;
import entities.Nozzle;

/**
 * Created by Owner on 5/17/2016.
 */
public class StatusAdapter extends BaseAdapter {
    String tag="PayFuel: "+StatusAdapter.class.getSimpleName();

    private Context context;
    private final List<GridData> mData;
    Typeface font;
    DBHelper db;
    public StatusAdapter(Context context, List<GridData> mData) {
        Log.d(tag,"Initialise griview Content");
        font=Typeface.createFromAsset(context.getAssets(), "font/ubuntu.ttf");
        if(mData.isEmpty() || mData.size()<=0){
            GridData gridData=new GridData();

            gridData.setPumpName("No Pump");
            gridData.setNozzleName("No Nozzle");
            gridData.setIndex("No Index");

            mData.add(gridData);
        }
        this.context = context;
        this.mData = mData;
        db = new DBHelper(context);
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
        gridViewElement = inflater.inflate(R.layout.statuses_style, null);

        // set values
        TextView nozzleId = (TextView) gridViewElement.findViewById(R.id.nozzleid);

        TextView nozzleName = (TextView) gridViewElement.findViewById(R.id.nozzlename);
        nozzleName.setTypeface(font);

        TextView pumpId = (TextView) gridViewElement.findViewById(R.id.pumpid);

        TextView pumpName = (TextView) gridViewElement.findViewById(R.id.pumpname);
        pumpName.setTypeface(font);

        TextView product = (TextView) gridViewElement.findViewById(R.id.nozzleproduct);
        product.setTypeface(font);

        TextView index = (TextView) gridViewElement.findViewById(R.id.index);
        index.setTypeface(font);

        TextView price = (TextView) gridViewElement.findViewById(R.id.price);
        price.setTypeface(font);

        TextView productId = (TextView) gridViewElement.findViewById(R.id.productid);

        ImageView icon=(ImageView) gridViewElement.findViewById(R.id.nozzleicon);

            GridData gridData=new GridData();
            gridData=mData.get(position);
            nozzleId.setText(String.valueOf(gridData.getNozzleId()));
            nozzleName.setText(gridData.getNozzleName());
            product.setText(gridData.getProduct());
            price.setText(String.valueOf(gridData.getPrice()));
            productId.setText(String.valueOf(gridData.getProductId()));
            pumpId.setText(String.valueOf(gridData.getPumpId()));
            pumpName.setText(gridData.getPumpName());
            Nozzle nozzle = db.getSingleNozzle(gridData.getNozzleId());
            index.setText(String.valueOf(nozzle.getNozzleIndex()));


        return gridViewElement;
    }
}
