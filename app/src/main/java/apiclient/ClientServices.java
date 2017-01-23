package apiclient;
import appBean.LoadPumpsResponse;
import appBean.Login;
import appBean.LoginResponse;
import appBean.TransactionResponse;
import config.BaseUrl;
import entities.SellingTransaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Created by Owner on 7/9/2016.
 */
public interface ClientServices {
    //    Post a transaction
    @POST(BaseUrl.MAKE_SALES)
    Call<TransactionResponse> makeTransactionApi(@Header("Content-Type")String header, @Body SellingTransaction mySales);

    //Login
    @POST(BaseUrl.LOGIN_URL)
    Call<LoginResponse> login(@Body Login login);

//
//    // get Payment modes
//    @GET(BaseUrl.GET_PAYMENTS_URL+"{userId}")
//    Call<PaymentModeResponse> getPaymentModes(@Header(AppInit.APP_VERSION_DESC) String version, @Header(OwnerShip.CLIENT_NAME_DESC) String clientName, @Header(OwnerShip.CLIENT_IDENTIFICATION_DESC) String clientIdentification, @Path("userId") int userId);
//
    // get Pumps
    @GET(BaseUrl.GET_PUMPS_URL+"{userId}")
    Call<LoadPumpsResponse> getPumps(@Path("userId") int userId);

//
//    @POST("{url}")
//    Call<T> universalPost(@Header(AppInit.APP_VERSION_DESC) String version, @Header(OwnerShip.CLIENT_NAME_DESC) String clientName, @Header(OwnerShip.CLIENT_IDENTIFICATION_DESC) String clientIdentification, @Body Object object, @Path("url") String url);
//
//    //topUp Airtime
//    @POST(BaseUrl.topUpUrl)
//    Call<TopUpResponse> topUp(@Header(BaseUrl.headerToken) String token, @Body TopUpRequest topUpRequest);
//
//    //getPayment mode list
//    @GET(BaseUrl.paymentModes)
//    Call<PaymentModesResponse> getPaymentModes();
//
//    //getTopup history
//    @GET(BaseUrl.transactionHistory+"{number}")
//    Call<TransactionHistoryResponse> getTransactionHistory(@Header(BaseUrl.headerToken) String token, @Path("number") int number);
//
//    //initiate wallet recharge
//    @POST(BaseUrl.initiateWalletRecharge)
//    Call<StatusUsage> initWalletRecharge(@Body InitiateWalletRecharge initiateWalletRecharge);
//
//    //confirm wallet recharge
//    @POST(BaseUrl.confirmWalletRecharge)
//    Call<StatusUsage> confWalletRecharge(@Body ConfirmWalletPayment confirmWalletPayment);
//
//    //getTopup favorites
//    @GET(BaseUrl.transactionHistory+"{number}")
//    Call<TransactionHistoryResponse> getFavorites(@Header(BaseUrl.headerToken) String token, @Path("number") int number);

}
