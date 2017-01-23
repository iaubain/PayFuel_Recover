package modules;

import appBean.Login;
import appBean.LoginResponse;
import entities.Logged_in_user;

/**
 * Created by Hp on 12/22/2016.
 */

public class LoginManager implements PostCredential.PostCredentialInteraction {
    LoginManagerInteraction mListener;
    Login login;

    public LoginManager(LoginManagerInteraction mListener, Login login) {
        this.mListener = mListener;
        this.login = login;
    }

    public void loginRequest(){
        if(login == null){
            mListener.onLoginManagerInteraction(false, "Invalid login credential", null);
            return;
        }
        PostCredential postCredential = new PostCredential(LoginManager.this, login);
        postCredential.startPost();
    }

    @Override
    public void onPostCredentialInteraction(int statusCode, String message, LoginResponse loginResponse) {
        if(statusCode == 200 && loginResponse != null){
            if(loginResponse.getStatusCode() != 100){
                mListener.onLoginManagerInteraction(false, loginResponse.getMessage(), null);
                return;
            }

            if(loginResponse.getLogged_in_user() != null){
                mListener.onLoginManagerInteraction(true, loginResponse.getMessage(), loginResponse.getLogged_in_user());
            }else{
                mListener.onLoginManagerInteraction(false, loginResponse.getMessage(), null);
            }
        }else{
            mListener.onLoginManagerInteraction(false, message, null);
        }
    }

    public interface LoginManagerInteraction{
        void onLoginManagerInteraction(boolean isLoginSuccessFull, String message, Logged_in_user logged_in_user);
    }
}
