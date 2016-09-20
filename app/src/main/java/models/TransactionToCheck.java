package models;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Owner on 6/24/2016.
 */
public class TransactionToCheck {
    @JsonProperty("deviceTransactionId")
    private long deviceTransactionId;
    @JsonProperty("branchId")
    private int branchId;
    @JsonProperty("userId")
    private int userId;
    @JsonProperty("deviceId")
    private long deviceId;
    @JsonProperty("pumpId")
    private int pumpId;
    @JsonProperty("nozzleId")
    private int nozzleId;
    @JsonProperty("productId")
    private int productId;
    @JsonProperty("customerId")
    private long customerId;
    @JsonProperty("paymentModeId")
    private int paymentModeId;
    @JsonProperty("paymentStatus")
    private String paymentStatus;
    @JsonProperty("amount")
    private long amount;
    @JsonProperty("quantity")
    private long quantity;
    @JsonProperty("indexbefore")
    private long indexbefore;
    @JsonProperty("indexafter")
    private long indexafter;

    public long getDeviceTransactionId() {
        return deviceTransactionId;
    }

    public void setDeviceTransactionId(long deviceTransactionId) {
        this.deviceTransactionId = deviceTransactionId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

    public int getNozzleId() {
        return nozzleId;
    }

    public void setNozzleId(int nozzleId) {
        this.nozzleId = nozzleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public int getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(int paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getIndexbefore() {
        return indexbefore;
    }

    public void setIndexbefore(long indexbefore) {
        this.indexbefore = indexbefore;
    }

    public long getIndexafter() {
        return indexafter;
    }

    public void setIndexafter(long indexafter) {
        this.indexafter = indexafter;
    }
}
