package modules;

import android.util.Log;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.LoadPumpsResponse;
import appBean.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utilities.DataFactory;

import static modules.PostTransaction.CONNECTIVITY_ERROR;
import static modules.PostTransaction.LOCAL_ERROR;
import static modules.PostTransaction.SERVER_ERROR;

/**
 * Created by Hp on 12/22/2016.
 */

public class RequestPumps {
    private RequestPumpsInteraction mListener;
    private int userId;

    public RequestPumps(RequestPumpsInteraction mListener, int userId) {
        this.mListener = mListener;
        this.userId = userId;
    }

    public void makeRequest(){
        if(userId <= 0){
            mListener.onRequestPumpInteraction(406, "Invalid user", null);
            return;
        }
        try{
            Log.d("Server request", "user id "+ userId);
            ClientServices clientServices = ServerClient.getClient().create(ClientServices.class);
            Call<LoadPumpsResponse> callService = clientServices.getPumps(userId);
            callService.enqueue(new Callback<LoadPumpsResponse>() {
                @Override
                public void onResponse(Call<LoadPumpsResponse> call, Response<LoadPumpsResponse>
                        response) {

                    //HTTP status code
                    int statusCode = response.code();
                    if(statusCode == 500){
                        mListener.onRequestPumpInteraction(SERVER_ERROR, response.message(), null);
                        return;
                    }
                    if(statusCode != 200){
                        mListener.onRequestPumpInteraction(CONNECTIVITY_ERROR, response.message(), null);
                        return;
                    }
                    Log.d("Server response", new DataFactory().objectToString(response.body()));
                    mListener.onRequestPumpInteraction(response.body().getStatusCode(), response.body().getMessage(), response.body());
                }

                @Override
                public void onFailure(Call<LoadPumpsResponse> call, Throwable t) {
                    // Log error here since request failed
                    mListener.onRequestPumpInteraction(CONNECTIVITY_ERROR, t.getMessage(), null);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            mListener.onRequestPumpInteraction(LOCAL_ERROR, e.getMessage(), null);
        }
    }

    public interface RequestPumpsInteraction{
        void onRequestPumpInteraction(int statsCode, String message, LoadPumpsResponse loadPumpsResponse);
    }
}
