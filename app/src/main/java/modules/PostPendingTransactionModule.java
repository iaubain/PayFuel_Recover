package modules;

import android.content.Context;
import android.util.Log;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.TransactionResponse;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import models.MapperClass;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hp on 11/30/2016.
 */

public class PostPendingTransactionModule {
    private String tag=getClass().getSimpleName();
    private Context context;
    private SellingTransaction sellingTransaction;

    public PostPendingTransactionModule(Context context, SellingTransaction sellingTransaction) {
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
                    if(statusCode != 200){
                        Log.e(tag,"WebServices is experiencing problem Error: Code "+statusCode+", "+response.message());
                        return;
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
}
