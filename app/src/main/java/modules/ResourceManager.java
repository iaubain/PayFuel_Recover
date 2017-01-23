package modules;

import android.content.Context;

import databaseBean.DBHelper;
import entities.Logged_in_user;
import features.LoadPumps;

/**
 * Created by Hp on 12/22/2016.
 */

public class ResourceManager implements LoadPumps.LoadPumpsInteraction {
    private ResourceManagerInteraction mListener;
    private Context context;
    private Logged_in_user user;
    private DBHelper db;

    public ResourceManager(ResourceManagerInteraction mListener, Context context, Logged_in_user user) {
        this.mListener = mListener;
        this.context = context;
        this.user = user;
    }

    public void organizeResources(){
        db = new DBHelper(context);

        if(!isUserLoggedIn(user.getUser_id()) && (!arePumpsAvailable() && !areNozzleAvailable())){
            requestResources(user.getName()+" Requesting resources");
            return;
        }

        if(arePumpsAvailable() && areNozzleAvailable()){
            if(isWorkPlaceAvailable(user.getUser_id())){
                mListener.onResourceAvailable(true, user.getName()+" ressource available", user);
            }else{
                //No workplace is available, request resources
                user.setLogged(0);
                db.updateUser(user);
                mListener.onSelectPumps(true, user.getName()+" select pump", user);
            }
        }else{
            // request for resources
            requestResources(user.getName()+" Requesting resources");
        }
    }

    private void requestResources(String info){
        mListener.onUiLoading(true, info, user);

        LoadPumps loadPumps = new LoadPumps(ResourceManager.this, context, user.getUser_id());
        loadPumps.startLoading();
    }

    private boolean isUserLoggedIn(int userId){
        Logged_in_user loggedInUser = db.getSingleUser(userId);
        return loggedInUser.getLogged() == 1;
    }

    private boolean areNozzleAvailable(){
        return db.getNozzleCount() > 0;
    }

    private boolean arePumpsAvailable(){
        return db.getPumpCount() > 0;
    }

    private boolean isWorkPlaceAvailable(int userId){
        return db.getStatusCountByUser(userId) > 0;
    }

    @Override
    public void onLoadPumpsInteraction(boolean arePumpsLoaded, String message) {
        if(arePumpsLoaded){
            //pump successfully loaded
            mListener.onSelectPumps(true, message, user);
        }else{
            //loading pump failed
            mListener.onResourceAvailable(false, message, user);
        }
    }

    public interface ResourceManagerInteraction{
        void onResourceAvailable(boolean isUserAllowed, String message, Logged_in_user user);
        void onUiLoading(boolean loading, String message, Logged_in_user user);
        void onSelectPumps(boolean selectPumps, String message, Logged_in_user user);
    }
}
