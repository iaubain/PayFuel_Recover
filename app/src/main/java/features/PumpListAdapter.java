package features;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import databaseBean.DBHelper;
import entities.Nozzle;
import entities.Pump;
import entities.WorkStatus;

/**
 * Created by Owner on 5/4/2016.
 */

public class PumpListAdapter extends ArrayAdapter<Pump> {

    private final Activity context;
    private final List<Pump> pumps;
    private final int userId;
    String tag="PayFuel: "+getClass().getSimpleName();
    DBHelper db;


    public PumpListAdapter(Activity context,int userId, List<Pump> pumps) {
        super(context, R.layout.pump_list_style, pumps);
        Log.d(tag,"Construct Pump List Adapter");
        this.context=context;
        this.userId=userId;
        this.pumps=pumps;
        db=new DBHelper(context);

    }

    public View getView(int position,View view,ViewGroup parent) {
        Log.d(tag,"Pump List Row Default Values Handle");
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.pump_list_style, null, true);

        TextView pumpName = (TextView) rowView.findViewById(R.id.pumpname);
        ImageView pumpImg = (ImageView) rowView.findViewById(R.id.icon);
        TextView pumpLabel = (TextView) rowView.findViewById(R.id.indicator);

        //get pump object
        Pump pump=pumps.get(position);

        pumpName.setText(pump.getPumpName());

        List<WorkStatus>  statuses=new ArrayList<WorkStatus>();
        statuses=db.getAllStatusPerPump(userId,Long.parseLong(String.valueOf(pump.getPumpId())));
        Iterator iterator=statuses.iterator();
        List<Nozzle> nozzles=db.getAllNozzlePerPump(Long.parseLong(String.valueOf(pump.getPumpId())));

        int nozzleDenialCheck = 0;
        int nozzleAcceptCheck = 0;
        int nozzleTaken = 0;
        for(Nozzle nozzle: nozzles){
            if(nozzle.getStatusCode()==8)
                nozzleTaken+=1;
        }

        while (iterator.hasNext()){
            WorkStatus ws=new WorkStatus();
            ws=(WorkStatus) iterator.next();
            if(ws.getStatusCode()==1){
//                pumpLabel.setText("Nozzle(s) Accepted");
//                pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
//                pumpImg.setImageResource(R.drawable.pump_green);
                nozzleAcceptCheck += 1;
            }else if(ws.getStatusCode()==0){
//                pumpImg.setImageResource(R.drawable.pump_red);
//                pumpLabel.setText("Nozzle(s) rejected");
//                pumpLabel.setTextColor(context.getResources().getColor(R.color.error));
//
                nozzleDenialCheck += 1;
                break;
            }

        }

        if(nozzleDenialCheck > 0){
            pumpImg.setImageResource(R.drawable.pump_red);
            pumpLabel.setText("Rejected");
            pumpName.setTextColor(context.getResources().getColor(R.color.error));
            pumpLabel.setTextColor(context.getResources().getColor(R.color.error));
        } else if(nozzleAcceptCheck > 0){
            pumpLabel.setText("Accepted");
            pumpName.setTextColor(context.getResources().getColor(R.color.green));
            pumpLabel.setTextColor(context.getResources().getColor(R.color.green));
            pumpImg.setImageResource(R.drawable.pump_green);
        }else if(nozzleTaken>0){
            pumpLabel.setText("Taken");
            pumpName.setTextColor(context.getResources().getColor(R.color.rdcolor));
            pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
            pumpImg.setImageResource(R.drawable.pump_gray);
        }else{
            pumpLabel.setText("Available");
            pumpName.setTextColor(context.getResources().getColor(R.color.rdcolor));
            pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
            //pumpLabel.setTextColor(context.getResources().getColor(R.color.rdoff));
            pumpImg.setImageResource(R.drawable.pump_blue);
        }
        return rowView;
    }
}