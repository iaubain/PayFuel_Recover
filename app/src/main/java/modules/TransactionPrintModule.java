package modules;

import android.content.Context;
import android.os.AsyncTask;
import databaseBean.DBHelper;
import entities.SellingTransaction;

/**
 * Created by Hp on 12/9/2016.
 */

public class TransactionPrintModule {
    private Context context;
    private SellingTransaction sellingTransaction;
    private int userId;
    private DBHelper db;
    private TransactionPrintInteraction mListener;

    public TransactionPrintModule(TransactionPrintInteraction mListener, Context context, int userId, DBHelper db, SellingTransaction sellingTransaction) {
        this.mListener = mListener;
        this.context = context;
        this.userId = userId;
        this.db = db;
        this.sellingTransaction = sellingTransaction;
    }

    public void generateReceipt(){
        if(sellingTransaction != null){
            new PrintHandler().execute(sellingTransaction);
        }

    }

    private class PrintHandler extends AsyncTask<SellingTransaction, String, String> {

        SellingTransaction sellingTransaction;
        @Override
        protected String doInBackground(SellingTransaction... params) {
            sellingTransaction = params[0];
            models.TransactionPrint tp = new models.TransactionPrint();

            tp.setAmount(sellingTransaction.getAmount());
            tp.setQuantity(sellingTransaction.getQuantity());
            tp.setBranchName(db.getSingleUser(userId).getBranch_name());
            tp.setDeviceId(db.getSingleDevice().getDeviceNo());
            tp.setUserName(db.getSingleUser(userId).getName());
            tp.setDeviceTransactionId(sellingTransaction.getDeviceTransactionId()+"");
            tp.setDeviceTransactionTime(sellingTransaction.getDeviceTransactionTime());
            tp.setNozzleName(db.getSingleNozzle(sellingTransaction.getNozzleId()).getNozzleName());
            tp.setPaymentMode(db.getSinglePaymentMode(sellingTransaction.getPaymentModeId()).getName());

            if (sellingTransaction.getPlateNumber() != null)
                tp.setPlateNumber(sellingTransaction.getPlateNumber());
            else
                tp.setPlateNumber("N/A");

            tp.setProductName(db.getSingleNozzle(sellingTransaction.getNozzleId()).getProductName());
            tp.setPumpName(db.getSinglePump(sellingTransaction.getPumpId()).getPumpName());

            if (sellingTransaction.getTelephone() != null)
                tp.setTelephone(sellingTransaction.getTelephone());
            else
                tp.setTelephone("N/A");

            if (sellingTransaction.getTin() != null)
                tp.setTin(sellingTransaction.getTin());
            else
                tp.setTin("N/A");

            if (sellingTransaction.getVoucherNumber() != null)
                tp.setVoucherNumber(sellingTransaction.getVoucherNumber());
            else
                tp.setVoucherNumber("N/A");

            if (sellingTransaction.getName() != null)
                tp.setCompanyName(sellingTransaction.getName());
            else
                tp.setCompanyName("N/A");


            tp.setPaymentStatus("Success");


            //launch printing procedure
            features.PrintHandler ph = new features.PrintHandler(context, tp);
            String printMessage = "";
            try{
                printMessage = ph.printOut();
            }catch (Exception e){
                e.printStackTrace();
                return "Error: "+e.getLocalizedMessage();
            }
            if (!printMessage.equalsIgnoreCase("Success")) {
                return printMessage;
            }
            return printMessage;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            mListener.printResult(result);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    public interface TransactionPrintInteraction{
        void printResult(String printingMessage);
    }
}
