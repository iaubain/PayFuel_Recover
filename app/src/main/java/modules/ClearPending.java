package modules;

import android.content.Context;

import java.util.List;

import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.SellingTransaction;

/**
 * Created by Hp on 11/30/2016.
 */

public class ClearPending implements PostClearing.PostClearingInteraction {
    private Context context;
    private List<AsyncTransaction> asyncTransactions;
    private DBHelper db;

    public ClearPending(Context context, List<AsyncTransaction> asyncTransactions, DBHelper db) {
        this.context = context;
        if(!asyncTransactions.isEmpty())
            this.asyncTransactions = asyncTransactions;
        else
            return;
        this.db = db;
    }

    public void startClearing(){
        for(AsyncTransaction asyncTransaction : asyncTransactions){
            try{
                SellingTransaction sellingTransaction = db.getSingleTransaction(asyncTransaction.getDeviceTransactionId());
                PostClearing postClearing = new PostClearing(this, context, sellingTransaction);
                postClearing.startPosting();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPostClearingInteraction(SellingTransaction sellingTransaction) {
        if(sellingTransaction != null){
            try{
                db.deleteAsyncTransaction(sellingTransaction.getDeviceTransactionId());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
