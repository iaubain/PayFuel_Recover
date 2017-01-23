package modules;

import android.util.Log;

import apiclient.ClientServices;
import apiclient.ServerClient;
import appBean.Login;
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

public class PostCredential {
    Login login;
    PostCredentialInteraction mListener;

    public PostCredential(PostCredentialInteraction mListener, Login login) {
        this.login = login;
        this.mListener = mListener;
    }

    public void startPost(){
        if(login == null){
            mListener.onPostCredentialInteraction(406, "Invalid login credential", null);
            return;
        }
        try{
            Log.d("Server request", new DataFactory().objectToString(login));
            ClientServices clientServices = ServerClient.getClient().create(ClientServices.class);
            Call<LoginResponse> callService = clientServices.login(login);
            callService.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse>
                        response) {

                    //HTTP status code
                    int statusCode = response.code();
                    if(statusCode == 500){
                        mListener.onPostCredentialInteraction(SERVER_ERROR, response.message(), null);
                        return;
                    }
                    if(statusCode != 200){
                        mListener.onPostCredentialInteraction(CONNECTIVITY_ERROR, response.message(), null);
                        return;
                    }
                    Log.d("Server response", new DataFactory().objectToString(response.body()));
                    mListener.onPostCredentialInteraction(response.body().getStatusCode(), response.body().getMessage(), response.body());
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    // Log error here since request failed
                    mListener.onPostCredentialInteraction(CONNECTIVITY_ERROR, t.getMessage(), null);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            mListener.onPostCredentialInteraction(LOCAL_ERROR, e.getMessage(), null);
        }
    }

    public interface PostCredentialInteraction{
        void onPostCredentialInteraction(int statusCode, String message, LoginResponse loginResponse);
    }
}
