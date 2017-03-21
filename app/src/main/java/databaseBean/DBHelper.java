package databaseBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import entities.AsyncTransaction;
import entities.DeviceIdentity;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.PaymentMode;
import entities.Product;
import entities.Pump;
import entities.SelectedPumps;
import entities.SellingTransaction;
import entities.WorkStatus;

/**
 * Created by Owner on 4/5/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    //database name
    public static final String DATABASE_NAME = "sppayfuel.db";
    //______________Database fields and variables____________________\\
    //device
    public static final String deviceTable="device";
    public static final String devId="devId";
    public static final String deviceId="deviceId";
    public static final String serialNumber="serialNumber";
    public static final String createDeviceTable = "create table "+ deviceTable +
            "(" + devId + " integer primary key AUTOINCREMENT, " + deviceId + " text," + serialNumber + " text)";
    //logged Users
    public static final String userTable="user";
    public static final String userId="userId";
    public static final  String userName="userName";
    public static final String branchId="branchId";
    public static final String branch_name="branchName";
    public static final String logged="logged";
    public static final String logged_time="loggedTime";
    public static final String permissions="permissions";
    public static final String createUserTable = "create table "+ userTable +
            "(" + userId + " integer primary key UNIQUE, " + userName + " text,"+ branchId + " text,"+ branch_name + " text," + permissions + " text," + logged + " integer," + logged_time + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    //Product
    public static final String productTable="products";
    public static final String productId="productId";
    public static final  String productName="productName";
    public static final String price="price";
    public static final String createProductTable = "create table "+ productTable +
            "(" + productId + " integer primary key, " + productName + " text,"+ price + " text)";
    //Pump
    public static final String pumpTable="pumps";
    public static final String pumpId="pumpId";
    public static final String pumpName="pumpName";
    public static final String pumpStatus="status";
    public static final String createPumpTable = "create table "+ pumpTable +
            "(" + pumpId + " integer primary key, "+branchId+" integer, "+pumpStatus+" integer, " + pumpName + " text)";
    //Nozzle
    public static final String nozzleTable="nozzle";
    public static final String nozzleId="nozzleId";
    public static final  String nozzleName="nozzleName";
    public static final String nozzleIndex ="nozzleIndex";
    public static final String statusCode ="statusCode";
    public static final String createNozzleTable = "create table "+ nozzleTable +
            "(" + nozzleId + " integer primary key, " + nozzleName + " text,"+ nozzleIndex + " text,"+ pumpId + " integer," + productId + " integer,"+ productName + " text, "+ price + " integer, "+statusCode+" integer, "+userName+" text)";
    //Payment Mode
    public static final String paymentModeTable="payments";
    public static final  String paymentModeId="paymentModeId";
    public static final String paymentName="paymentModeName";
    public static final  String paymentType="type";
    public static final  String paymentStatus="status";
    public static final  String paymentDescr="descr";
    public static final String createPaymentModeTable = "create table "+ paymentModeTable +
            "(" + paymentModeId + " integer primary key, " + paymentName + " text,"+ paymentType + " text,"+paymentStatus+" integer,"+paymentDescr+" text)";
    //selected Pump
    public static final String selectedPumpTable="selectedPump";
    public static final String select_pump_id="selectedPumpId";
    public static final String createSelectedPumpTable = "create table "+ selectedPumpTable +
            "(" + select_pump_id + " integer primary key AUTOINCREMENT, " + nozzleId + " text,"+userId+" text)";
    //Selling Transaction
    public static final String transactionTable="sellingTransaction";
    public static final String transactionId="transactionId";
    public static final String amount="amount";
    public static final String quantity="quantity";
    public static final String plateNumber="plateNumber";
    public static final String telephone="telephone";
    public static final String customerName="customerName";
    public static final String tin="tin";
    public static final String voucherNumber="vouchernumber";
    public static final String authorisationCode="code";
    public static final String time="time";
    public static final String status="status";
    public static final String authenticationCode="authenticationcode";
    public static final String createTransactionTable = "create table "+ transactionTable + "("+ transactionId +" integer primary key," +
            userId + " integer, " + deviceId + " text," + branchId + " integer," +
            productId + " integer," + nozzleId + " integer,"+ pumpId + " integer," + paymentModeId + " integer," +
            amount+" text,"+quantity+" text,"+ status +" integer, "+customerName+" text, " + plateNumber + " text," + telephone + " text,"
            +tin+" text," + voucherNumber + " text," + authorisationCode + " text,"+ authenticationCode + " integer,"+ time + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    //Asynchronious Transaction
    public static final String asyncTable="asncTransaction";
    public static final String asyncId="id";
    public static final String sum="sum";
    public static final String createAsyncTable = "create table "+ asyncTable +
            "(" + asyncId + " integer primary key AUTOINCREMENT, " + userId + " integer, "+ deviceId + " text, "+ branchId + " integer, " + transactionId + " integer, "+sum+" integer)";
    //Work Status
    public static final String statusTable="statusTable";
    public static final String statusId="statusId";
    //public static final String userId="userId";
    //public static final String pumpId;
    //public static final String nozzleId;
    public static final String statusMessage="statusMessage";
    public static final String createStatusTable = "create table "+ statusTable +
            "(" + statusId + " integer primary key AUTOINCREMENT, " + userId + " integer, "+ pumpId + " text,"+ nozzleId + " integer," + statusMessage + " integer, "+statusCode+" integer)";
    // Logcat tag
     static final String tag = "PayFuel: "+DBHelper.class.getSimpleName();
     static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createDeviceTable);
        db.execSQL(createUserTable);
        db.execSQL(createPaymentModeTable);
        db.execSQL(createProductTable);
        db.execSQL(createPumpTable);
        db.execSQL(createNozzleTable);
        db.execSQL(createTransactionTable);
        db.execSQL(createSelectedPumpTable);
        db.execSQL(createAsyncTable);
        db.execSQL(createStatusTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+deviceTable);
        db.execSQL("DROP TABLE IF EXISTS "+userTable);
        db.execSQL("DROP TABLE IF EXISTS " + createPaymentModeTable);
        db.execSQL("DROP TABLE IF EXISTS " + createProductTable);
        db.execSQL("DROP TABLE IF EXISTS " + pumpTable);
        db.execSQL("DROP TABLE IF EXISTS " + nozzleTable);
        db.execSQL("DROP TABLE IF EXISTS " + transactionTable);
        db.execSQL("DROP TABLE IF EXISTS " + selectedPumpTable);
        db.execSQL("DROP TABLE IF EXISTS "+asyncTable);
        db.execSQL("DROP TABLE IF EXISTS "+statusTable);
        onCreate(db);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    //_______________________User Table Actions________________________\\
    /**
     * Creating a user
     */
    public long createUser(Logged_in_user user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(userId, user.getUser_id());
        values.put(userName, user.getName());
        values.put(branchId, user.getBranch_id());
        values.put(branch_name, user.getBranch_name());
        values.put(permissions, user.getPermissions());
        values.put(logged, user.getLogged());
        // insert row
        long userId = db.insertWithOnConflict(userTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        Log.d(tag, "The item Id is: " + userId);
        return userId;
    }

    /**
     * get single user
     */
    public Logged_in_user getSingleUser(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + userTable + " WHERE "+ userId + " = " + u_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst() && c.getCount()>0) {
            c.moveToFirst();
            Logged_in_user user = new Logged_in_user();
            user.setUser_id(c.getInt(c.getColumnIndex(userId)));
            user.setName(c.getString(c.getColumnIndex(userName)));
            user.setPermissions(c.getString(c.getColumnIndex(permissions)));
            user.setBranch_id(c.getInt(c.getColumnIndex(branchId)));
            user.setBranch_name(c.getString(c.getColumnIndex(branch_name)));
            user.setLogged(c.getInt(c.getColumnIndex(logged)));
            user.setLogged_time(c.getString(c.getColumnIndex(logged)));

            return user;
        }
        return null;
    }

    /*
    * getting all Users
    */
    public List<Logged_in_user> getAllUsers() {
        List<Logged_in_user> users = new ArrayList<Logged_in_user>();
        String selectQuery = "SELECT  * FROM " + userTable;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while (c.isAfterLast() == false) {
                Logged_in_user user = new Logged_in_user();
                user.setUser_id(c.getInt(c.getColumnIndex(userId)));
                user.setName(c.getString(c.getColumnIndex(userName)));
                user.setPermissions(c.getString(c.getColumnIndex(permissions)));
                user.setBranch_id(c.getInt(c.getColumnIndex(branchId)));
                user.setBranch_name(c.getString(c.getColumnIndex(branch_name)));
                user.setLogged(c.getInt(c.getColumnIndex(logged)));
                user.setLogged_time(c.getString(c.getColumnIndex(logged)));

                // adding user to list
                users.add(user);
                c.moveToNext();
            }
        }
        return users;
    }

    /**
     * Deleting a user
     */
    public void deleteUser(long u_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(userTable, userId + " = ?",
                new String[]{String.valueOf(u_id)});
    }

    /**
     * Truncate logged users
     */
    public int truncateUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(userTable, null, null);
        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{userTable});
    }

    /**
     * getting user count
     */
    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + userTable;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    /**
     * getting user count
     */
    public int getLoggedUserCount() {
        String countQuery = "SELECT  * FROM " + userTable+" WHERE "+logged+" = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }
    /**
     * Updating a user
     */
    public int updateUser(Logged_in_user user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(userId, user.getUser_id());
        values.put(userName, user.getName());
        values.put(branchId, user.getBranch_id());
        values.put(branch_name, user.getBranch_name());
        values.put(permissions, user.getPermissions());
        values.put(logged, user.getLogged());

        // updating row
        return db.update(userTable, values, userId + " = ?", new String[]{String.valueOf(user.getUser_id())});
    }

    //_______________________Transactions Table Actions________________________\\
    /**
     * Creating a transaction
     */
    public long createTransaction(SellingTransaction st) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(transactionId, st.getDeviceTransactionId());
        values.put(userId, st.getUserId());
        values.put(branchId,st.getBranchId());
        values.put(deviceId,st.getDeviceNo());
        values.put(productId,st.getProductId());
        values.put(paymentModeId,st.getPaymentModeId());
        values.put(nozzleId, st.getNozzleId());
        values.put(pumpId,st.getPumpId());
        values.put(amount, st.getAmount());
        values.put(quantity, st.getQuantity());
        values.put(plateNumber, st.getPlateNumber());
        values.put(telephone, st.getTelephone());
        values.put(customerName, st.getName());
        values.put(tin, st.getTin());
        values.put(voucherNumber, st.getVoucherNumber());
        values.put(authorisationCode, st.getAuthorisationCode());
        values.put(authenticationCode, st.getAuthenticationCode());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
        values.put(time,sdf.format(calendar.getTime()));
        if(st.getStatus()!= 0)
            values.put(status,st.getStatus());
        else
            values.put(status,301);
        //use the default in the database
       // values.put(time,st.getDeviceTransactionTime());

        // insert row
        long transactionId = db.insert(transactionTable, null, values);

        return transactionId;
    }

    /**
     * get single Transaction
     */
    public SellingTransaction getSingleTransaction(long t_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        SellingTransaction st = new SellingTransaction();
        String selectQuery = "SELECT  * FROM " + transactionTable + " WHERE "+ transactionId + " = " + t_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()){
            c.moveToFirst();
            st.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));
            st.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
            st.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(amount))));
            st.setQuantity(Double.parseDouble(c.getString(c.getColumnIndex(quantity))));
            st.setPlateNumber(c.getString(c.getColumnIndex(plateNumber)));
            st.setTelephone(c.getString(c.getColumnIndex(telephone)));
            st.setName(c.getString(c.getColumnIndex(customerName)));
            st.setTin(c.getString(c.getColumnIndex(tin)));
            st.setVoucherNumber(c.getString(c.getColumnIndex(voucherNumber)));
            st.setAuthorisationCode(c.getString(c.getColumnIndex(authorisationCode)));
            st.setDeviceTransactionTime(c.getString(c.getColumnIndex(time)).toString());
            st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
            st.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
            st.setStatus(c.getInt(c.getColumnIndex(status)));
            st.setBranchId(c.getInt(c.getColumnIndex(branchId)));
            st.setUserId(c.getInt(c.getColumnIndex(userId)));
            st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
            st.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
            st.setProductId(c.getInt(c.getColumnIndex(productId)));
            st.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
        }

        return st;
    }

    /**
     * getting all Transaction
     */
    public List<SellingTransaction> getAllTransactions() {
        List<SellingTransaction> sts = new ArrayList<SellingTransaction>();
        String selectQuery = "SELECT  * FROM " + transactionTable;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                SellingTransaction st = new SellingTransaction();
                st.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));
                st.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                st.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(amount))));
                st.setQuantity(Double.parseDouble(c.getString(c.getColumnIndex(quantity))));
                st.setPlateNumber(c.getString(c.getColumnIndex(plateNumber)));
                st.setTelephone(c.getString(c.getColumnIndex(telephone)));
                st.setName(c.getString(c.getColumnIndex(customerName)));
                st.setTin(c.getString(c.getColumnIndex(tin)));
                st.setVoucherNumber(c.getString(c.getColumnIndex(voucherNumber)));
                st.setAuthorisationCode(c.getString(c.getColumnIndex(authorisationCode)));
                st.setDeviceTransactionTime(c.getString(c.getColumnIndex(time)).toString());
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
                st.setStatus(c.getInt(c.getColumnIndex(status)));
                st.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                st.setUserId(c.getInt(c.getColumnIndex(userId)));
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                st.setProductId(c.getInt(c.getColumnIndex(productId)));
                st.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
                // adding transaction to list
                sts.add(st);
                c.moveToNext();
            }
        }

        return sts;
    }

    /**
     * getting all Transactions per user
     */
    public List<SellingTransaction> getAllTransactionsPerUser(long user_id) {
        List<SellingTransaction> sts = new ArrayList<SellingTransaction>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// Datetime('"+sdf.format(calendar.getTime())+"')
        String selectQuery = "SELECT  * FROM " + transactionTable + " WHERE "+ userId + " = " + user_id+" AND date("+time+") >= date('now', '-1 days') ORDER BY "+time+" DESC";

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while (!c.isAfterLast()){
                SellingTransaction st = new SellingTransaction();
                st.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));
                st.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                st.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(amount))));
                st.setQuantity(Double.parseDouble(c.getString(c.getColumnIndex(quantity))));
                st.setPlateNumber(c.getString(c.getColumnIndex(plateNumber)));
                st.setTelephone(c.getString(c.getColumnIndex(telephone)));
                st.setName(c.getString(c.getColumnIndex(customerName)));
                st.setTin(c.getString(c.getColumnIndex(tin)));
                st.setVoucherNumber(c.getString(c.getColumnIndex(voucherNumber)));
                st.setAuthorisationCode(c.getString(c.getColumnIndex(authorisationCode)));
                st.setDeviceTransactionTime(c.getString(c.getColumnIndex(time)));
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
                st.setStatus(c.getInt(c.getColumnIndex(status)));
                st.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                st.setUserId(c.getInt(c.getColumnIndex(userId)));
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                st.setProductId(c.getInt(c.getColumnIndex(productId)));
                st.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
                // adding transaction to list
                sts.add(st);
                c.moveToNext();
            }
        }
        c.close();

        return sts;
    }


    public List<SellingTransaction> getAllUnsuccessfulTransaction(long user_id) {
        List<SellingTransaction> sts = new ArrayList<SellingTransaction>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String selectQuery = "SELECT  * FROM " + transactionTable + " WHERE ("+ userId + " = " + user_id+" AND ("+status+" == 301 OR "+status+" == 302)) AND "+time+" >= Datetime('"+sdf.format(calendar.getTime())+"') ORDER BY "+transactionId+" DESC";

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while (c.isAfterLast() == false){
                SellingTransaction st = new SellingTransaction();
                st.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));
                st.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                st.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(amount))));
                st.setQuantity(Double.parseDouble(c.getString(c.getColumnIndex(quantity))));
                st.setPlateNumber(c.getString(c.getColumnIndex(plateNumber)));
                st.setTelephone(c.getString(c.getColumnIndex(telephone)));
                st.setName(c.getString(c.getColumnIndex(customerName)));
                st.setTin(c.getString(c.getColumnIndex(tin)));
                st.setVoucherNumber(c.getString(c.getColumnIndex(voucherNumber)));
                st.setAuthorisationCode(c.getString(c.getColumnIndex(authorisationCode)));
                st.setDeviceTransactionTime(c.getString(c.getColumnIndex(time)).toString());
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
                st.setStatus(c.getInt(c.getColumnIndex(status)));
                st.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                st.setUserId(c.getInt(c.getColumnIndex(userId)));
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                st.setProductId(c.getInt(c.getColumnIndex(productId)));
                st.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
                // adding transaction to list
                sts.add(st);
                c.moveToNext();
            }
        }
        c.close();
        return sts;
    }

    /**
     * getting all Transactions based on time per user
     */
    public List<SellingTransaction> getAllTransactionsPerTime(long user_id, int reportDate) {
        String selectQuery = null;

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String now=year+"-"+month+"-"+day;
        if(month<10)
            now=year+"-0"+month+"-"+day;

        if(day<10)
            now=year+"-"+month+"-0"+day;

        Log.v(tag,"Report date is:"+now);

        List<SellingTransaction> sts = new ArrayList<SellingTransaction>();
        if(reportDate == 0){
//            selectQuery = "SELECT  * FROM " + transactionTable + " WHERE ("+ userId + " = " + user_id+" AND date("+time+") == '"+now +"') AND "+status+" = 100 OR "+status+" = 101 ORDER BY "+time+" DESC";
            selectQuery = "SELECT * FROM "+transactionTable+" WHERE ("+userId+" = "+user_id+" AND date("+time+") == date('now')) AND "+status+" = 100 OR "+status+" = 101 ORDER BY "+time+" DESC";

        }

        if(reportDate == -1){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);//'"+dateFormat.format(cal.getTime())+"'
            selectQuery = "SELECT * FROM "+transactionTable+" WHERE ("+userId+" = "+user_id+" AND date("+time+") == date('now', '-1 days')) AND "+status+" = 100 OR "+status+" = 101 ORDER BY "+time+" DESC";
        }
        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while (!c.isAfterLast()){
                SellingTransaction st = new SellingTransaction();
                st.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));
                st.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                st.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(amount))));
                st.setQuantity(Double.parseDouble(c.getString(c.getColumnIndex(quantity))));
                st.setPlateNumber(c.getString(c.getColumnIndex(plateNumber)));
                st.setTelephone(c.getString(c.getColumnIndex(telephone)));
                st.setName(c.getString(c.getColumnIndex(customerName)));
                st.setTin(c.getString(c.getColumnIndex(tin)));
                st.setVoucherNumber(c.getString(c.getColumnIndex(voucherNumber)));
                st.setAuthorisationCode(c.getString(c.getColumnIndex(authorisationCode)));
                st.setDeviceTransactionTime(c.getString(c.getColumnIndex(time)).toString());
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
                st.setStatus(c.getInt(c.getColumnIndex(status)));
                st.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                st.setUserId(c.getInt(c.getColumnIndex(userId)));
                st.setAuthenticationCode(c.getInt(c.getColumnIndex(authenticationCode)));
                st.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                st.setProductId(c.getInt(c.getColumnIndex(productId)));
                st.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
                // adding transaction to list
                sts.add(st);
                c.moveToNext();
            }
        }
        c.close();
        Log.v(tag,"Size of transaction report: "+sts.size());
        return sts;
    }


    /**
     * Deleting a Transaction
     */
    public void deleteTransaction(long t_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(transactionTable, transactionId + " = ?", new String[]{String.valueOf(t_id)});
        db.close();
    }

    /**
     * Deleting old Transactions
     */
    public void deleteOldTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM "+transactionTable+" WHERE "+time+" <= date('now','-6 day')";
        db.execSQL(sql);
        db.close();
    }

    /**
     * Truncate all transaction
     */
    public int truncateTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(transactionTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        int valueToReturn = db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{transactionTable});
        db.close();
        return valueToReturn;
    }

    /**
     * getting transaction count per user
     */
    public int getTransactionCount(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String countQuery = "SELECT  * FROM " + transactionTable+ " WHERE " + userId + " = " + String.valueOf(u_id)+" AND "+time+" >= Datetime('"+sdf.format(calendar.getTime())+"')";
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        db.close();
        return count;
    }
    /**
     * getting pending transaction count per user
     */
    public int getTransactionCountPending(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String countQuery = "SELECT  * FROM " + transactionTable+ " WHERE (" + userId + " = " + String.valueOf(u_id)+" AND "+status+" = 301 OR "+status+" = 302)"+" AND "+time+" >= Datetime('"+sdf.format(calendar.getTime())+"')";
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        db.close();
        return count;
    }

    /**
     * getting pending transaction count per user
     */
    public int getTransactionCountCancelled(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String countQuery = "SELECT  * FROM " + transactionTable+ " WHERE " + userId + " = " + String.valueOf(u_id)+" AND "+status+" = 500"+" AND "+time+" >= Datetime('"+sdf.format(calendar.getTime())+"')";
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        db.close();
        return count;
    }

    /**
     * getting pending transaction count per user
     */
    public int getTransactionCountSucceeded(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String countQuery = "SELECT  * FROM " + transactionTable+ " WHERE (" + userId + " = " + String.valueOf(u_id)+" AND "+time+" >= Datetime('"+sdf.format(calendar.getTime())+"')"+") AND "+status+" = 100 OR "+status+" = 101";
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        db.close();
        return count;
    }

    /**
     * Updating a transaction
     */
    public int updateTransaction(SellingTransaction st) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(status,st.getStatus());
        // updating row
        int valueToReturn = db.update(transactionTable, values, transactionId + " = ?", new String[]{String.valueOf(st.getDeviceTransactionId())});
        db.close();
        return valueToReturn;
    }



    //_______________________Pumps Table Actions________________________\\
    /**
     * Creating a pump
     */
    public long createPump(Pump pump) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(pumpId, pump.getPumpId());
        values.put(pumpName, pump.getPumpName());
        values.put(pumpStatus, pump.getStatus());
        values.put(branchId, pump.getBranchId());

        // insert row
        long pumpId = db.insertWithOnConflict(pumpTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
        return pumpId;
    }

    /**
     * get single Pump
     */
    public Pump getSinglePump(long p_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Pump pump = new Pump();
        String selectQuery = "SELECT  * FROM " + pumpTable + " WHERE "+ pumpId + " = " + p_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst() && c.getCount()>0) {
            c.moveToFirst();

            pump.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
            pump.setPumpName(c.getString(c.getColumnIndex(pumpName)));
            pump.setBranchId(c.getInt(c.getColumnIndex(branchId)));
            pump.setStatus(c.getInt(c.getColumnIndex(pumpStatus)));

            c.close();
            db.close();
            return pump;
        }

        db.close();
        return null;
    }

    /**
     * getting all pumps
     */
    public List<Pump> getAllPumps() {
        List<Pump> pumps = new ArrayList<Pump>();
        String selectQuery = "SELECT  * FROM " + pumpTable;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst() && c!=null) {
            while(c.isAfterLast() == false){
                Pump pump = new Pump();
                pump.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                pump.setPumpName(c.getString(c.getColumnIndex(pumpName)));
                pump.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                pump.setStatus(c.getInt(c.getColumnIndex(pumpStatus)));

                // adding pump to list
                pumps.add(pump);
                c.moveToNext();
            }
        }

        return pumps;
    }


    /**
     * Deleting a Pump
     */
    public void deletePump(long p_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(pumpTable, pumpId + " = ?", new String[]{String.valueOf(p_id)});
    }

    /**
     * Truncate all Pumps
     */
    public int truncatePumps() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(pumpTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{pumpTable});
    }

    /**
     * getting pump Count Per User
     */
    public int getPumpCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + pumpTable;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    //_______________________Nozzles Table Actions________________________\\
    /**
     * Creating a nozzle
     */
    public long createNozzle(Nozzle nozzle) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(nozzleId,nozzle.getNozzleId());
        values.put(nozzleName, nozzle.getNozzleName());
        values.put(nozzleIndex,nozzle.getNozzleIndex());
        values.put(pumpId,nozzle.getPumpId());
        values.put(statusCode,nozzle.getStatusCode());
        values.put(productId,nozzle.getProductId());
        values.put(productName, nozzle.getProductName());
        values.put(price, nozzle.getUnitPrice());
        values.put(userName, nozzle.getUserName());
        // insert row
        long dbId = db.insert(nozzleTable, null, values);

        return dbId;
    }

    /**
     * get single Nozzle
     */
    public Nozzle getSingleNozzle(long n_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Nozzle nozzle=new Nozzle();
        String selectQuery = "SELECT  * FROM " + nozzleTable + " WHERE "+ nozzleId + " = " + n_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst() && c.getCount()>0) {
            c.moveToFirst();
            nozzle.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
            nozzle.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
            nozzle.setProductId(c.getInt(c.getColumnIndex(productId)));
            nozzle.setProductName(c.getString(c.getColumnIndex(productName)));
            nozzle.setNozzleIndex(c.getDouble(c.getColumnIndex(nozzleIndex)));
            nozzle.setNozzleName(c.getString(c.getColumnIndex(nozzleName)));
            nozzle.setUnitPrice(c.getInt(c.getColumnIndex(price)));
            nozzle.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
            nozzle.setUserName(c.getString(c.getColumnIndex(userName)));

            return nozzle;
        }
        return  null;
    }

    /**
     * getting all Nozzle
     */
    public List<Nozzle> getAllNozzle() {
        List<Nozzle> nozzles = new ArrayList<Nozzle>();
        String selectQuery = "SELECT  * FROM " + nozzleTable;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                Nozzle nozzle=new Nozzle();
                nozzle.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                nozzle.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                nozzle.setProductId(c.getInt(c.getColumnIndex(productId)));
                nozzle.setProductName(c.getString(c.getColumnIndex(productName)));
                nozzle.setNozzleIndex(c.getDouble(c.getColumnIndex(nozzleIndex)));
                nozzle.setNozzleName(c.getString(c.getColumnIndex(nozzleName)));
                nozzle.setUnitPrice(c.getInt(c.getColumnIndex(price)));
                nozzle.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
                nozzle.setUserName(c.getString(c.getColumnIndex(userName)));

                // adding nozzle to list
                nozzles.add(nozzle);
                c.moveToNext();
            }
        }

        return nozzles;
    }

    /**
     * getting all Nozzle per Pump
     */
    public List<Nozzle> getAllNozzlePerPump(long pump_Id) {
        List<Nozzle> nozzles = new ArrayList<Nozzle>();
        String selectQuery = "SELECT  * FROM " + nozzleTable+ " WHERE " + pumpId + " = " + String.valueOf(pump_Id);

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst() && c.getCount()>0 && c!=null) {
            while(c.isAfterLast() == false){
                Nozzle nozzle=new Nozzle();
                nozzle.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                nozzle.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                nozzle.setProductId(c.getInt(c.getColumnIndex(productId)));
                nozzle.setProductName(c.getString(c.getColumnIndex(productName)));
                nozzle.setNozzleIndex(c.getDouble(c.getColumnIndex(nozzleIndex)));
                nozzle.setNozzleName(c.getString(c.getColumnIndex(nozzleName)));
                nozzle.setUnitPrice(c.getInt(c.getColumnIndex(price)));
                nozzle.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
                nozzle.setUserName(c.getString(c.getColumnIndex(userName)));

                // adding nozzle to list
                nozzles.add(nozzle);
                c.moveToNext();
            }
            return nozzles;
        }

        return null;
    }
    /**
     * Updating a selectedPump
     */
    public int updateNozzle(Nozzle nozzle) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(nozzleIndex,nozzle.getNozzleIndex());

        // updating row
        return db.update(nozzleTable, values, nozzleId + " = ?", new String[]{String.valueOf(nozzle.getNozzleId())});
    }


    /**
     * Deleting a Nozzle
     */
    public void deleteNozzle(long n_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(nozzleTable, nozzleId + " = ?", new String[]{String.valueOf(n_id)});
    }

    /**
     * Deleting a Nozzle by pumpId
     */
    public void deleteNozzleByPump(long p_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(nozzleTable, pumpId + " = ?", new String[]{String.valueOf(p_id)});
    }

    /**
     * Truncate all nozzles
     */
    public int truncateNozzles() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(nozzleTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{nozzleTable});
    }

    /**
     * getting nozzle count
     */
    public int getNozzleCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + nozzleTable;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    /**
     * getting nozzle count by pump Id
     */
    public int getNozzleCountByPump(long pump_Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + nozzleTable+ " WHERE " + pumpId + " = " + String.valueOf(pump_Id);
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    //_______________________Device Table Actions________________________\\
    /**
     * Creating a nozzle
     */
    public long createDevice(DeviceIdentity di) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(deviceId, di.getDeviceNo());
        values.put(serialNumber, di.getSerialNumber());
        // insert row
        long dbId = db.insert(deviceTable, null, values);

        return dbId;
    }

    /**
     * get single Device
     */
    public DeviceIdentity getSingleDevice() {
        SQLiteDatabase db = this.getReadableDatabase();

        DeviceIdentity di=new DeviceIdentity();
        String selectQuery = "SELECT  * FROM " + deviceTable;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
            c.moveToFirst();
        di.setID(c.getInt(c.getColumnIndex(devId)));
        di.setDeviceNo(c.getString(c.getColumnIndex(deviceId)));
        di.setSerialNumber(c.getString(c.getColumnIndex(serialNumber)));
        return di;
    }

    /**
     * getting device count
     */
    public int getDeviceCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + deviceTable;
        Cursor cursor = db.rawQuery(countQuery, null, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }
    /**
     * Truncate all device
     */
    public int truncateDevice() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(deviceTable, null, null);
        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
       return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{deviceTable});
    }

    //_______________________PaymentMode Table Actions________________________\\
    /**
     * Creating a nozzle
     */
    public long createPayment(PaymentMode pm) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(paymentModeId,pm.getPaymentModeId());
        values.put(paymentName, pm.getName());
        values.put(paymentType,pm.getPaymentType());
        values.put(paymentStatus, pm.getStatus());
        values.put(paymentDescr, pm.getDescr());
        // insert row
        long dbId = db.insertWithOnConflict(paymentModeTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return dbId;
    }

    /**
     * get single PaymentMode
     */
    public PaymentMode getSinglePaymentMode(long p_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        PaymentMode pm=new PaymentMode();
        String selectQuery = "SELECT  * FROM " + paymentModeTable + " WHERE "+ paymentModeId + " = " + p_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst() && c.getCount()>0) {
            c.moveToFirst();
            pm.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
            pm.setName(c.getString(c.getColumnIndex(paymentName)));
            pm.setPaymentType(c.getString(c.getColumnIndex(paymentType)));
            pm.setStatus(c.getInt(c.getColumnIndex(paymentStatus)));
            pm.setDescr(c.getString(c.getColumnIndex(paymentDescr)));
            return pm;
        }
        return null;
    }

    /**
     * getting all PaymentMode
     */
    public List<PaymentMode> getAllPaymentMode() {
        List<PaymentMode> pms = new ArrayList<PaymentMode>();
        String selectQuery = "SELECT  * FROM " + paymentModeTable;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                PaymentMode pm=new PaymentMode();
                pm.setPaymentModeId(c.getInt(c.getColumnIndex(paymentModeId)));
                pm.setName(c.getString(c.getColumnIndex(paymentName)));
                pm.setPaymentType(c.getString(c.getColumnIndex(paymentType)));
                pm.setStatus(c.getInt(c.getColumnIndex(paymentStatus)));
                pm.setDescr(c.getString(c.getColumnIndex(paymentDescr)));

                // adding paymentMode to list
                pms.add(pm);
                c.moveToNext();
            }
        }

        return pms;
    }


    /**
     * Deleting a paymentMode
     */
    public void deletePaymentMode(long p_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(paymentModeTable, paymentModeId + " = ?", new String[]{String.valueOf(p_id)});
    }

    /**
     * Truncate all paymentMode
     */
    public int truncatePaymentMode() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(paymentModeTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{paymentModeTable});
    }

    /**
     * getting paymentMode count
     */
    public int getPaymentModeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + paymentModeTable;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    //_______________________SelectedPumps Table Actions________________________\\
    /**
     * Creating a SelectedPumps
     */
    public long createSelectedPump(SelectedPumps sp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(nozzleId, sp.getNozzleId());
        values.put(userId, sp.getUserId());

        // insert row
        long dbId = db.insert(selectedPumpTable, null, values);

        return dbId;
    }

    /**
     * get single nozzle pump
     */
    public SelectedPumps getSingleSelectedPump(long n_id, long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        SelectedPumps sp=new SelectedPumps();
        String selectQuery = "SELECT  * FROM " + selectedPumpTable + " WHERE "+ select_pump_id + " = " + n_id+" AND "+userId+" = "+u_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
            c.moveToFirst();
        sp.setId(c.getInt(c.getColumnIndex(select_pump_id)));
        sp.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
        sp.setUserId(c.getInt(c.getColumnIndex(userId)));
        return sp;
    }

    /**
     * getting all selected Pumps
     */
    public List<SelectedPumps> getAllSelectedPumps(long u_id) {
        List<SelectedPumps> sps = new ArrayList<SelectedPumps>();
        String selectQuery = "SELECT  * FROM " + selectedPumpTable + " WHERE "+userId+" = "+u_id;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                SelectedPumps sp=new SelectedPumps();
                sp.setId(c.getInt(c.getColumnIndex(select_pump_id)));
                sp.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                sp.setUserId(c.getInt(c.getColumnIndex(userId)));

                // adding transaction to list
                sps.add(sp);
                c.moveToNext();
            }
        }

        return sps;
    }


    /**
     * Deleting a SelectedPump
     */
    public void deleteSelectedPump(long n_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(selectedPumpTable, nozzleId + " = ?",
                new String[]{String.valueOf(n_id)});
    }

    /**
     * Deleting a SelectedPump
     */
    public void deleteAllSelectedPump(long u_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(selectedPumpTable, userId + " = ?",
                new String[]{String.valueOf(u_id)});
    }

    /**
     * Truncate all SelectedPumps
     */
    public int truncateSelectedPump() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(selectedPumpTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{selectedPumpTable});
    }

    /**
     * getting SelectedPumps count per user
     */
    public int getSelectedPumpsCount(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + selectedPumpTable + " WHERE " + userId + " = " + String.valueOf(u_id);
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    /**
     * Updating a selectedPump
     */
    public int updateSelectedPump(SelectedPumps sp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(nozzleId,sp.getNozzleId());
        values.put(userId, sp.getUserId());

        // updating row
        return db.update(userTable, values, userId + " = ?", new String[]{String.valueOf(sp.getId())});
    }

    //_______________________Product Table Actions________________________\\
    /**
     * Creating a product
     */
    public long createProduct(Product pr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(productId,pr.getProductId());
        values.put(productName, pr.getProductName());
        values.put(price, pr.getUnitPrice());
        // insert row
        long dbId = db.insert(productTable, null, values);

        return dbId;
    }

    /**
     * get single nozzle pump
     */
    public Product getSingleProduct(long p_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + productTable + " WHERE " + productId + " = " + p_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount()>0){
            c.moveToFirst();
            Product product=new Product();
            product.setProductId(c.getInt(c.getColumnIndex(productId)));
            product.setProductName(c.getString(c.getColumnIndex(productName)));
            product.setUnitPrice((double) c.getInt(c.getColumnIndex(price)));
            return product;
        }
        return null;
    }

    /**
     * getting product count
     */
    public int getProductCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + productTable;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }
    /**
     * Truncate all product
     */
    public int truncateProduct() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(productTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{productTable});
    }


    //_______________________AsyncTransaction Table Actions________________________\\
    /**
     * Creating a Async
     */
    public long createAsyncTransaction(AsyncTransaction at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();



        values.put(deviceId, at.getDeviceId());
        values.put(branchId, at.getBranchId());
        values.put(userId, at.getUserId());
        values.put(transactionId, at.getDeviceTransactionId());
        values.put(sum, at.getSum());

        // insert row
        long dbId = db.insert(asyncTable, null, values);

        values.clear();
        db.close();
        return dbId;
    }

    /**
     * get single Async
     */
    public AsyncTransaction getSingleAsyncTransaction(long t_id, long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        AsyncTransaction at=new AsyncTransaction();
        String selectQuery = "SELECT  * FROM " + asyncTable + " WHERE "+ transactionId + " = " + t_id+" AND "+userId+" = "+u_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()){
            c.moveToFirst();
            at.setId(c.getInt(c.getColumnIndex(asyncId)));
            at.setUserId(c.getInt(c.getColumnIndex(userId)));
            at.setBranchId(c.getInt(c.getColumnIndex(branchId)));
            at.setDeviceId(c.getString(c.getColumnIndex(deviceId)));
            at.setSum(c.getInt(c.getColumnIndex(sum)));
            at.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));

        }

        c.close();
        db.close();
        return at;
    }

    /**
     * get single Async per Transactoin ID
     */
    public AsyncTransaction getSingleAsyncPerTransacton(long t_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        AsyncTransaction at=new AsyncTransaction();
        String selectQuery = "SELECT  * FROM " + asyncTable + " WHERE "+ transactionId + " = " + t_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            at.setId(c.getInt(c.getColumnIndex(asyncId)));
            at.setUserId(c.getInt(c.getColumnIndex(userId)));
            at.setBranchId(c.getInt(c.getColumnIndex(branchId)));
            at.setDeviceId(c.getString(c.getColumnIndex(deviceId)));
            at.setSum(c.getInt(c.getColumnIndex(sum)));
            at.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));

            c.close();
            db.close();
            return at;
        }

        c.close();
        db.close();
        return null;
    }

    /**
     * getting all Async
     */
    public List<AsyncTransaction> getAllAsyncTransactions(long u_id) {
        List<AsyncTransaction> ats = new ArrayList<AsyncTransaction>();
        String selectQuery = "SELECT  * FROM " + asyncTable +" WHERE "+userId+" = "+u_id;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                AsyncTransaction at=new AsyncTransaction();
                at.setId(c.getInt(c.getColumnIndex(asyncId)));
                at.setUserId(c.getInt(c.getColumnIndex(userId)));
                at.setBranchId(c.getInt(c.getColumnIndex(branchId)));
                at.setDeviceId(c.getString(c.getColumnIndex(deviceId)));
                at.setSum(c.getInt(c.getColumnIndex(sum)));
                at.setDeviceTransactionId(c.getLong(c.getColumnIndex(transactionId)));

                // adding Async transaction to list
                ats.add(at);
                c.moveToNext();
            }
        }

        c.close();
        db.close();
        return ats;
    }


    /**
     * Deleting Async Transaction
     */
    public void deleteAsyncTransaction(long t_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(asyncTable, transactionId + " = ?", new String[]{String.valueOf(t_id)});
        db.close();
    }

    /**
     * Truncate all Async transaction
     */
    public int truncateAsyncTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(asyncTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{asyncTable});
    }

    /**
     * getting Async transaction count per user
     */
    public int getAsyncTransactionCount(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + asyncTable+ " WHERE " + userId + " = " + String.valueOf(u_id);
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    /**
     * Updating a transaction
     */
    public int updateAsyncTransaction(AsyncTransaction at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(asyncId,at.getId());
        values.put(deviceId, at.getDeviceId());
        values.put(branchId, at.getBranchId());
        values.put(deviceId,at.getDeviceId());
        values.put(transactionId,at.getDeviceTransactionId());
        values.put(sum,at.getSum());

        // updating row
        return db.update(asyncTable, values, transactionId + " = ?", new String[]{String.valueOf(at.getDeviceTransactionId())});
    }

    //_______________________Status Table Actions________________________\\
    /**
     * Creating a Status
     */
    public long createStatus(WorkStatus ws) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(userId, ws.getUserId());
        values.put(pumpId, ws.getPumpId());
        values.put(nozzleId,ws.getNozzleId());
        values.put(statusMessage,ws.getMessage());
        values.put(statusCode, ws.getStatusCode());
        // insert row
        long dbId = db.insertWithOnConflict(statusTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return dbId;
    }

    /**
     * get single status
     */
    public WorkStatus getSingleStatus(long user_id, long n_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        WorkStatus ws=new WorkStatus();
        String selectQuery = "SELECT  * FROM " + statusTable + " WHERE "+ nozzleId + " = " + n_id+" AND "+ userId + " = " + user_id;
        Log.e(tag, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst() && c.getCount()>0) {
            c.moveToFirst();

            ws.setStatusId(c.getInt(c.getColumnIndex(statusId)));
            ws.setUserId(c.getInt(c.getColumnIndex(userId)));
            ws.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
            ws.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
            ws.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
            ws.setMessage(c.getString(c.getColumnIndex(statusMessage)));

            return ws;
        }
        return  null;
    }

    /**
     * getting all Work Status per pump ID
     */
    public List<WorkStatus> getAllStatusPerPump(long user_id, long pump_id) {
        List<WorkStatus> statuses = new ArrayList<WorkStatus>();
        String selectQuery = "SELECT  * FROM " + statusTable+ " WHERE "+ userId + " = " + user_id + " AND "+ pumpId + " = " +pump_id;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                WorkStatus ws=new WorkStatus();

                ws.setStatusId(c.getInt(c.getColumnIndex(statusId)));
                ws.setUserId(c.getInt(c.getColumnIndex(userId)));
                ws.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
                ws.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                ws.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                ws.setMessage(c.getString(c.getColumnIndex(statusMessage)));

                // adding Status to list
                statuses.add(ws);
                c.moveToNext();
            }
        }

        return statuses;
    }
    /**
     * getting all Work Status
     */
    public List<WorkStatus> getAllStatus(long user_id) {
        List<WorkStatus> statuses = new ArrayList<WorkStatus>();
        String selectQuery = "SELECT  * FROM " + statusTable+ " WHERE "+ userId + " = " + user_id;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                WorkStatus ws=new WorkStatus();

                ws.setStatusId(c.getInt(c.getColumnIndex(statusId)));
                ws.setUserId(c.getInt(c.getColumnIndex(userId)));
                ws.setStatusCode(c.getInt(c.getColumnIndex(statusCode)));
                ws.setPumpId(c.getInt(c.getColumnIndex(pumpId)));
                ws.setNozzleId(c.getInt(c.getColumnIndex(nozzleId)));
                ws.setMessage(c.getString(c.getColumnIndex(statusMessage)));

                // adding Status to list
                statuses.add(ws);
                c.moveToNext();
            }
        }

        c.close();
        db.close();
        return statuses;
    }

    /**
     * Check for Status
     */
    public boolean isThereAnyStatus(long user_id){
        List<WorkStatus> statuses = new ArrayList<WorkStatus>();
        String selectQuery = "SELECT  * FROM " + statusTable+ " WHERE "+ userId + " = " + user_id;

        Log.e(tag, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(!c.isAfterLast()){
//                WorkStatus ws=new WorkStatus();

                if(c.getInt(c.getColumnIndex(statusCode))==2)
                    return true;
                c.moveToNext();
            }
        }

//        Iterator i=statuses.iterator();
        c.close();
        db.close();
        return  false;
    }
    /**
     * Deleting a Status
     */
    public void deleteStatus(long n_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(statusTable, nozzleId + " = ?", new String[]{String.valueOf(n_id)});
        db.close();
    }

    /**
     * Deleting a Status by pumpId
     */
    public void deleteStatusByPump(long p_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(statusTable, pumpId + " = ?", new String[]{String.valueOf(p_id)});
        db.close();
    }

    /**
     * Deleting a Status by userId
     */
    public void deleteStatusByUser(long user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(statusTable, userId + " = ?", new String[]{String.valueOf(user_id)});
        db.close();
    }

    /**
     * Truncate all statuses
     */
    public int truncateStatuses() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(statusTable, null, null);

        // updating row
        ContentValues value = new ContentValues();
        value.put("seq", 0);
        return db.update("SQLITE_SEQUENCE", value, "name" + " = ?", new String[]{statusTable});
    }

    /**
     * getting Status count
     */
    public int getStatusCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + statusTable;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return count;
    }

    /**
     * getting Status Count count by user Id
     */
    public int getStatusCountByUserAndPump(long user_id, long nozzle_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + statusTable+ " WHERE " + nozzleId + " = " + String.valueOf(nozzle_id)+" And "+ userId + " = " + String.valueOf(user_id);
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return count;
    }

    /**
     * getting nozzle count by user Id
     */
    public int getStatusCountByUser(long user_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + statusTable+ " WHERE " + userId + " = " + String.valueOf(user_id);
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return count;
    }
    /**
     * Updating a Status
     */
    public int updateStatus(WorkStatus ws) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(statusMessage,ws.getMessage());
        values.put(statusCode, ws.getStatusCode());

        // updating row
        return db.update(statusTable, values, nozzleId + " = ?", new String[]{String.valueOf(ws.getNozzleId())});
    }

}
