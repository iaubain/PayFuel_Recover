package modules;

import android.util.Log;
import android.widget.Toast;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.TransactionResponse;
import entities.SellingTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utilities.DataFactory;

/**
 * Created by Hp on 12/9/2016.
 */

public class PostTransaction {
    private SellingTransaction sellingTransaction;
    private  PostTransactionInteraction mListener;
    public static final int CONNECTIVITY_ERROR=-1;
    public static final int SERVER_ERROR=-2;
    public static final int LOCAL_ERROR=-3;

    public PostTransaction(PostTransactionInteraction mListener, SellingTransaction sellingTransaction) {
        this.mListener = mListener;
        this.sellingTransaction = sellingTransaction;
    }

    public void startPosting(){
        if(sellingTransaction == null)
            return;
        try{
            Log.d("Server request", new DataFactory().objectToString(sellingTransaction));
            ClientServices clientServices = ServerClient.getClient().create(ClientServices.class);
            Call<TransactionResponse> callService = clientServices.makeTransactionApi("application/json", sellingTransaction);
            callService.enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse>
                        response) {

                    //HTTP status code
                    int statusCode = response.code();
                    if(statusCode == 500){
                        mListener.onTransactionPost(false, SERVER_ERROR, sellingTransaction);
                        return;
                    }
                    if(statusCode != 200){
                        mListener.onTransactionPost(false, CONNECTIVITY_ERROR, sellingTransaction);
                        return;
                    }

                    TransactionResponse transactionResponse=response.body();
                    if(transactionResponse == null){
                        mListener.onTransactionPost(false, CONNECTIVITY_ERROR, sellingTransaction);
                        return;
                    }
                    Log.d("Server response: ", new DataFactory().objectToString(transactionResponse));
                    mListener.onTransactionPost(true, transactionResponse.getStatusCode(), sellingTransaction);
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    // Log error here since request failed
                    mListener.onTransactionPost(false, CONNECTIVITY_ERROR, sellingTransaction);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            mListener.onTransactionPost(false, LOCAL_ERROR, sellingTransaction);
        }
    }
    public interface PostTransactionInteraction{
        void onTransactionPost(boolean status, int serverStatus, SellingTransaction sellingTransaction);
    }
}
