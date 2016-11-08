package features;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

import java.util.ArrayList;
import java.util.List;

import appBean.PumpAndNozzle;
import appBean.PumpAndNozzleList;
import databaseBean.DBHelper;
import entities.Nozzle;
import entities.WorkStatus;

/**
 * Created by Owner on 5/4/2016.
 */

public class NozzleListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> nozzleNameList;
//    private final List<String> imgIdList;
//    private final List<String> indexList;
//    private final List<String> productList;
//    private final List<String> nozzleId;
    private final List<Nozzle> nozzles;
    private final int userId;
    private final int pumpId;
    private final View pumpView;
    String tag="PayFuel: "+getClass().getSimpleName();
    DBHelper db;
    PumpAndNozzleList choosenPumpAndNozzle;
    NozzleDenied nd;
    List<PumpAndNozzle> selectedList;



    public NozzleListAdapter(Activity context,int userId, int pumpId, View pumpView, List<Nozzle> nozzles, List<String> nozzleNameList/*, List<String> imgIdList, List<String> indexList,List<String> productList, List<String> nozzleId*/) {
        super(context, R.layout.nozzle_list_style,nozzleNameList);
        Log.d(tag,"Construct Nozzle List Adapter of pump: ");
        this.context=context;
        this.userId=userId;
        this.pumpId=pumpId;
        this.nozzles=nozzles;
        this.pumpView=pumpView;
        this.nozzleNameList = nozzleNameList;
//        this.indexList=indexList;
//        this.productList=productList;
//        this.imgIdList = imgIdList;
//        this.nozzleId=nozzleId;
        db=new DBHelper(context);
        nd=new NozzleDenied();
        choosenPumpAndNozzle=new PumpAndNozzleList();
        selectedList=new ArrayList<PumpAndNozzle>();
    }

    public View getView(final int position,View view,ViewGroup parent) {
        Log.d(tag,"Pump List Row Default Values Handle");



        nd.setIsNozzleDenied(false);

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.nozzle_list_style, null, true);

        TextView nozzleName = (TextView) rowView.findViewById(R.id.nozzlename);
        final ImageView nozzleIcon = (ImageView) rowView.findViewById(R.id.nozzleicon);
        TextView index = (TextView) rowView.findViewById(R.id.index);
        index.setTypeface(Typeface.DEFAULT,Typeface.BOLD);

        TextView product = (TextView) rowView.findViewById(R.id.nozzleproduct);
        final TextView label = (TextView) rowView.findViewById(R.id.nozzleindicator);

        final TextView pumpLabel=(TextView) pumpView.findViewById(R.id.indicator);
        final ImageView pumpImg=(ImageView) pumpView.findViewById(R.id.icon);

        final Button refuse=(Button) rowView.findViewById(R.id.refuse);
        final Button accept=(Button) rowView.findViewById(R.id.accept);

        final Nozzle nozzle=nozzles.get(position);
        final boolean[] refuseCheck = {false},acceptCheck={false};

        //When a nozzle was already taken
        if(nozzle.getStatusCode()==8){
            nozzleIcon.setImageResource(R.drawable.nozzleinactive);
            nozzleName.setText("Taken by:" + nozzle.getUserName());
            accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_on));
            accept.setTextColor(context.getResources().getColor(R.color.white));
//            accept.setText("Ѵ");
            accept.setEnabled(false);
            accept.setClickable(false);

            refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_on));
            refuse.setTextColor(context.getResources().getColor(R.color.white));
//            refuse.setText("X");
            refuse.setEnabled(false);
            refuse.setClickable(false);

        }else{
            nozzleName.setText(nozzle.getNozzleName());
        }

        index.append("" + nozzle.getNozzleIndex());
        product.setText(nozzle.getProductName());

        //_____________________Check the status of nozzle_____________________\\
        int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
        if(statusCount > 0){
            //when some has been selected so far
            WorkStatus ws=db.getSingleStatus(userId,nozzle.getNozzleId());
            if(ws != null){
                //when a nozzle status was found
                if(ws.getStatusCode()==2){
                    //when nozzle was selected
                    label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                    label.setText("Accepted");
                    nozzleIcon.setImageResource(R.drawable.nozzle_green);

                    refuse.setTextColor(context.getResources().getColor(R.color.error));
                    refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_neutral));

                    accept.setTextColor(context.getResources().getColor(R.color.white));
                    accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_on));

                }else if(ws.getStatusCode()==1){
                    //when nozzle was denied
                    label.setTextColor(context.getResources().getColor(R.color.error));
                    label.setText("Denied");
                    nozzleIcon.setImageResource(R.drawable.nozzle_red);

                    refuse.setTextColor(context.getResources().getColor(R.color.white));
                    refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_on));

                    accept.setTextColor(context.getResources().getColor(R.color.positive));
                    accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_neutral));

                }else{
                    //when no status was found
                    label.setText("Available to Choose");
                    nozzleIcon.setImageResource(R.drawable.nozzle_blue);
                }
            }else if (nozzle.getStatusCode() != 8){
                //When no status was not found
                label.setText("Available to Choose");
                nozzleIcon.setImageResource(R.drawable.nozzle_blue);
            }

        }else if (nozzle.getStatusCode() != 8){
            //when nothing was selected before
            label.setText("Available to Choose");
            nozzleIcon.setImageResource(R.drawable.nozzle_blue);
        }


        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(tag, "Refused: " + nozzle.getNozzleId());

                refuse.setTextColor(context.getResources().getColor(R.color.white));
                refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_on));

                accept.setTextColor(context.getResources().getColor(R.color.positive));
                accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_neutral));
                WorkStatus ws;
                int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
                if(!refuseCheck[0]){
                    if(statusCount<=0){
                        ws=new WorkStatus();
                        ws.setMessage("Denied");
                        ws.setStatusCode(1);
                        ws.setNozzleId(nozzle.getNozzleId());
                        ws.setPumpId(pumpId);
                        ws.setUserId(userId);
                        int dbId=(int) db.createStatus(ws);
                        if(dbId > 0){
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Denied");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }else{
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Error Occurred");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }
                    }else if(statusCount > 0){
                        ws=new WorkStatus();
                        ws.setMessage("Denied");
                        ws.setStatusCode(1);
                        ws.setNozzleId(nozzle.getNozzleId());
                        int dbId= db.updateStatus(ws);
                        if(dbId > 0){
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Denied");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }else{
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Error Occurred");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Status Error");
                        nozzleIcon.setImageResource(R.drawable.nozzle_red);
                    }
                    refuseCheck[0] =true;
                }else{
                    ws=new WorkStatus();
                    ws.setMessage("Available");
                    ws.setStatusCode(0);
                    ws.setNozzleId(nozzle.getNozzleId());
                    int dbId= db.updateStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                        label.setText("Available to Choose");
                        nozzleIcon.setImageResource(R.drawable.nozzle_blue);
                        refuse.setTextColor(context.getResources().getColor(R.color.error));
                        refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_neutral));
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Occurred");
                        nozzleIcon.setImageResource(R.drawable.nozzle_red);
                    }

                    refuseCheck[0] =false;
                }
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(tag, "Accepted: " + nozzle.getNozzleId());
                refuse.setTextColor(context.getResources().getColor(R.color.error));
                refuse.setBackground(context.getResources().getDrawable(R.drawable.ic_deny_neutral));

                accept.setTextColor(context.getResources().getColor(R.color.white));
                accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_on));
                WorkStatus ws;
                int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
                if(!acceptCheck[0]){
                    if(statusCount<=0){
                        ws=new WorkStatus();
                        ws.setMessage("Accepted");
                        ws.setStatusCode(2);
                        ws.setNozzleId(nozzle.getNozzleId());
                        ws.setPumpId(pumpId);
                        ws.setUserId(userId);
                        int dbId=(int) db.createStatus(ws);
                        if(dbId > 0){
                            label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                            label.setText("Accepted");
                            nozzleIcon.setImageResource(R.drawable.nozzle_green);
                        }else{
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Error Choosing");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }
                    }else if(statusCount > 0){
                        ws=new WorkStatus();
                        ws.setMessage("Accepted");
                        ws.setStatusCode(2);
                        ws.setNozzleId(nozzle.getNozzleId());
                        int dbId= db.updateStatus(ws);
                        if(dbId > 0){
                            label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                            label.setText("Accepted");
                            nozzleIcon.setImageResource(R.drawable.nozzle_green);
                        }else{
                            label.setTextColor(context.getResources().getColor(R.color.error));
                            label.setText("Error Choosing");
                            nozzleIcon.setImageResource(R.drawable.nozzle_red);
                        }
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Status Error");
                        nozzleIcon.setImageResource(R.drawable.nozzle_red);
                    }
                    acceptCheck[0] =true;
                }else {
                    ws=new WorkStatus();
                    ws.setMessage("Available");
                    ws.setStatusCode(0);
                    ws.setNozzleId(nozzle.getNozzleId());
                    int dbId= db.updateStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                        label.setText("Available to Choose");
                        nozzleIcon.setImageResource(R.drawable.nozzle_blue);
                        accept.setTextColor(context.getResources().getColor(R.color.positive));
                        accept.setBackground(context.getResources().getDrawable(R.drawable.ic_accept_neutral));
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Occurred");
                        nozzleIcon.setImageResource(R.drawable.nozzle_red);
                    }

                    acceptCheck[0] =false;
                }

                }
        });

        return rowView;

    }
}