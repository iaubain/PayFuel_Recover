package features;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.aub.oltranz.payfuel.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import justtide.ThermalPrinter;
import models.TransactionPrint;

/**
 * Created by Owner on 6/2/2016.
 */
public class PrintHandler {
    String tag="PayFuel: "+getClass().getSimpleName();
    Context context;
    int size;
    private Object data;
    private ThermalPrinter thermalPrinter;

    public PrintHandler(Context context, Object data) {
        Log.d(tag, "Initiating a print");
        thermalPrinter=ThermalPrinter.getInstance();
        this.context = context;
        this.data = data;
    }

    public String printOut(){
        int ret = thermalPrinter.getState();
        Log.v("PRINT", "getState:"+ ret);

        ret = thermalPrinter.getTemperature();
        Log.v("PRINT", "getTemperature:"+ ret);

        boolean blnRet = thermalPrinter.isOverTemperature();
        Log.v("PRINT", "isOverTemperature:"+ blnRet);

        blnRet = thermalPrinter.isPaperOut();
        Log.v("PRINT", "isPaperOut:" + blnRet);

        if ((!thermalPrinter.isOverTemperature())&&(!thermalPrinter.isPaperOut())&&(thermalPrinter.getState() > -1)){
            //check the type of Object that came
            try{
                return transPrint((TransactionPrint) data);
            }catch (Exception e){
                e.printStackTrace();
                return "Error: "+e.getCause();
            }

        }else{
            if(thermalPrinter.isPaperOut()){
                return "Printer Failure: Out Of Paper";
            }
            if(thermalPrinter.isPaperOut() && thermalPrinter.isOverTemperature()){
                return "Printer Failure: Out Of Paper And Over Temperature";
            }

            if(thermalPrinter.isOverTemperature()){
                return "Printer Failure: Over Temperature";
            }
            return "Printer Failure: "+thermalPrinter.getState();
        }
    }

    private void line(){
        Log.d(tag,"Printing line");
        byte[] lineBuffer = new byte[96];
        for(int index=0;index<54;index++) {
            lineBuffer[index]= (byte) 0xfc;
        }
        thermalPrinter.printLine(lineBuffer);
        thermalPrinter.setStep(5);
    }

    public String transPrint(TransactionPrint tp){
        Log.d(tag,"Transaction printing...");

        if(tp != null)
            tp=(TransactionPrint) data;
        else
            return "Printer Failure: No Data";
        //initialize the printer
        thermalPrinter.initBuffer();
        thermalPrinter.setGray(3);
        thermalPrinter.setHeightAndLeft(20, 10);
        thermalPrinter.setLineSpacing(3);
        //thermalPrinter.setDispMode(ThermalPrinter.CMODE);
        thermalPrinter.setDispMode(ThermalPrinter.UMODE);

        //Setting bill logo
        Bitmap bitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        thermalPrinter.printLogo(0, 10, bitmap);

        //Setting header of the bill
        thermalPrinter.setStep(10);
        thermalPrinter.setFont(ThermalPrinter.ASC16X24B, ThermalPrinter.HZK12);
        //thermalPrinter.shiftRight(50);
//            thermalPrinter.print("___________________\n\n");
        line();
        thermalPrinter.shiftRight(75);
        thermalPrinter.print(tp.getBranchName() + "\n");
        //thermalPrinter.shiftRight(50);
        line();
        thermalPrinter.setStep(10);
        //thermalPrinter.print("___________________\n\n");

        //thermalPrinter.setHeightAndLeft(20, 10);
        thermalPrinter.setFont(ThermalPrinter.ASC12X24Y, ThermalPrinter.ASC12X24);
        thermalPrinter.print("Product: " + tp.getProductName() + "\n");
        thermalPrinter.print("Amount: " + tp.getAmount() + " RWF\n");
        thermalPrinter.print("Quantity: " + tp.getQuantity() + " Liters\n\n");
        //thermalPrinter.print("___________________________\n");

        //transaction
        line();
        thermalPrinter.shiftRight(60);
        thermalPrinter.print("TRANSACTION DETAILS\n");
        line();
        thermalPrinter.setStep(4);
        thermalPrinter.print("Transaction: " + tp.getDeviceTransactionId() + "\n");
        thermalPrinter.print("Payment Method: " + tp.getPaymentMode() + "\n");
        if(tp.getPaymentMode().equalsIgnoreCase("voucher")){
            thermalPrinter.print("Voucher Number: " + tp.getVoucherNumber() + "\n");
        }
        thermalPrinter.print("Payment Status: " + tp.getPaymentStatus() + "\n");
        thermalPrinter.print("Served Pump: " + tp.getPumpName() + "\n");
        thermalPrinter.print("Served Nozzle: " + tp.getNozzleName() + "\n");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateString=tp.getDeviceTransactionTime();
        try {

            Date date = formatter.parse(dateString);
            thermalPrinter.print("Time:" + formatter.format(date) + "\n");

        } catch (ParseException e) {
            e.printStackTrace();
            thermalPrinter.print("Time:" + tp.getDeviceTransactionTime() + "\n");
        }

        //transaction
        thermalPrinter.print("Device: " + tp.getDeviceId() + "\n");

        //customer
        thermalPrinter.print("Customer Tel:" + tp.getTelephone() + "\n");
        thermalPrinter.print("Name:" + tp.getCompanyName() + "\n");
        thermalPrinter.print("TIN:" + tp.getTin() + "\n");
        thermalPrinter.print("Number Plate:" + tp.getPlateNumber() + "\n");
        thermalPrinter.print("Served by:" + tp.getUserName() + "\n\n");
        //thermalPrinter.print("___________________________\n\n");
        line();

        //Company Details
        thermalPrinter.shiftRight(60);
        thermalPrinter.print("COMPANY DETAILS \n");
        //thermalPrinter.shiftRight(50);
        //thermalPrinter.print("___________________\n");
        line();
        thermalPrinter.setStep(10);
        thermalPrinter.setFont(ThermalPrinter.HZK24F, ThermalPrinter.ASC12X24);

        thermalPrinter.print("ENGEN RWANDA\n");
        thermalPrinter.print("Gasabo, Kigali/Rwanda\n");
        thermalPrinter.print("TeL: (250) 788 175 111/(250) 788 304 747\n");
        thermalPrinter.print("B.P. 1342, KIGALI-RWANDA\n");
        thermalPrinter.print("Registration Number(TIN): 101835101\n\n");

        thermalPrinter.setFont(ThermalPrinter.ASC12X24YB, ThermalPrinter.HZK24F);

        thermalPrinter.setFont(ThermalPrinter.ASC12X24YB, ThermalPrinter.HZK24F);

        thermalPrinter.shiftRight(120);
        thermalPrinter.print("Thank You!\n\n");

        thermalPrinter.setFont(ThermalPrinter.ASC8X16B, ThermalPrinter.HZK24F);
        thermalPrinter.shiftRight(90);
        thermalPrinter.print("_________________________\n");
        thermalPrinter.shiftRight(100);

        thermalPrinter.print("Powered By Oltranz.com \n");

        thermalPrinter.shiftRight(90);
        thermalPrinter.print("_________________________\n");

        thermalPrinter.setStep(200);

        thermalPrinter.printStart();
        int state = thermalPrinter.waitForPrintFinish();

        Log.v(tag, "Successful transaction Printing: " + state);

        return "Success";
    }
}
