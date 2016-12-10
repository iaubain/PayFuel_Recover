package modules;

import android.content.Context;
import android.util.Log;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.TransactionResponse;
import entities.SellingTransaction;
import models.MapperClass;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hp on 11/30/2016.
 */

public class PostClearing {
    private String tag=getClass().getSimpleName();
    private Context context;
    private SellingTransaction sellingTransaction;
    private PostClearingInteraction mListener;

    public PostClearing(PostClearingInteraction mListener, Context context, SellingTransaction sellingTransaction) {
        this.mListener = mListener;
        this.context = context;
        this.sellingTransaction = sellingTransaction;
    }

    public void startPosting(){
        try {
            Log.d("Server request", new MapperClass().mapping(sellingTransaction));
            ClientServices clientServices = ServerClient.getClient().create(ClientServices.class);
            Call<TransactionResponse> callService = clientServices.makeTransactionApi("application/json", sellingTransaction);
            callService.enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {

                    //HTTP status code
                    int statusCode = response.code();
                    if(statusCode == 500){
                        mListener.onPostClearingInteraction(sellingTransaction);
                        return;
                    }
                    if(statusCode != 200){
                        Log.e(tag,"WebServices is experiencing problem Error: Code "+statusCode+", "+response.message());
                        return;
                    }
                    if(response.body().getStatusCode() == 100){
                        mListener.onPostClearingInteraction(sellingTransaction);
                    }
                    Log.d(tag,"Server message: "+ response.body().getMessage());
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    //uiPop("Connectivity Error");
                    Log.e(tag,"WebServices is experiencing problem Error: "+t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(tag,"WebServices is experiencing problem Error: "+e.getMessage());
            //loginListener.onLoginInteraction(500, e.getMessage(), msisdn, null);
        }
    }

    public interface PostClearingInteraction{
        void onPostClearingInteraction(SellingTransaction sellingTransaction);
    }
}
