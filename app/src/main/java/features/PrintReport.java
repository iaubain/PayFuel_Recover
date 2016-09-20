package features;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.aub.oltranz.payfuel.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import justtide.ThermalPrinter;
import models.TransactionPrint;

/**
 * Created by Owner on 6/15/2016.
 */
public class PrintReport {
    String tag="PayFuel: "+getClass().getSimpleName();
    Context context;
    int size;
    String data;
    String branchName;
    ThermalPrinter thermalPrinter;
    byte[] lineBuffer;

    int i,state;
    int ret = -1;
    boolean blnRet = false;

    public PrintReport(Context context, String branchName, String data) {
        Log.d(tag, "Initiating a print");
        thermalPrinter=ThermalPrinter.getInstance();
        this.context = context;
        this.data = data;
        this.branchName=branchName;
    }

    public String printOut(){
        ret = thermalPrinter.getState();
        Log.v("PRINT", "getState:"+ret);

        ret = thermalPrinter.getTemperature();
        Log.v("PRINT", "getTemperature:"+ret);

        blnRet = thermalPrinter.isOverTemperature();
        Log.v("PRINT", "isOverTemperature:"+blnRet);

        blnRet = thermalPrinter.isPaperOut();
        Log.v("PRINT", "isPaperOut:" + blnRet);

        if ((!thermalPrinter.isOverTemperature())&&(!thermalPrinter.isPaperOut())&&(thermalPrinter.getState() > -1)){
            //check the type of Object that came
            if(data.getClass().getSimpleName().equalsIgnoreCase("TransactionPrint")){
                return reportPrint();
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

        return null;
    }

    public void line(){
        Log.d(tag,"Printing line");
        lineBuffer = new byte[96];
        for(int index=0;index<54;index++) {
            lineBuffer[index]= (byte) 0xfc;
        }
        thermalPrinter.printLine(lineBuffer);
        thermalPrinter.setStep(5);
    }

    public String reportPrint(){
        Log.d(tag,"Report printing...");

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
        thermalPrinter.print(branchName + "\n");
        //thermalPrinter.shiftRight(50);
        line();
        thermalPrinter.setStep(10);

        thermalPrinter.setFont(ThermalPrinter.ASC12X24Y, ThermalPrinter.ASC12X24);
        thermalPrinter.print(data);
        thermalPrinter.print("\n\n");

        line();

        //Company Details
        thermalPrinter.shiftRight(60);
        thermalPrinter.print("COMPANY DETAILS \n");
        //thermalPrinter.shiftRight(50);
        //thermalPrinter.print("___________________\n");
        line();
        thermalPrinter.setStep(10);
        thermalPrinter.setFont(ThermalPrinter.HZK24F, ThermalPrinter.ASC12X24);

        thermalPrinter.print("SP PETROLIERE Ltd\n");
        thermalPrinter.print("Nyarugenge, Kigali/Rwanda\n");
        thermalPrinter.print("TeL: (250) 787 230 666/(250) 788 306 232\n");
        thermalPrinter.print("B.P. 144 KIGALI-RWANDA\n");
        thermalPrinter.print("Registration Number(TIN): 100222174\n\n");

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
        state = thermalPrinter.waitForPrintFinish();

        Log.v(tag, "Successful transaction Printing: " + state);

        return "Success";
    }
}
