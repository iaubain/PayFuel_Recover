package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SellingTransaction {
	public SellingTransaction() {
	}

    @JsonProperty("deviceTransactionId")
	 private long deviceTransactionId;
    @JsonProperty("userId")
	 private int userId;
    @JsonProperty("branchId")
	 private int branchId;
    @JsonProperty("nozzleId")
	 private int nozzleId;
    @JsonProperty("pumpId")
     private int pumpId;
    @JsonProperty("deviceId")
	 private String deviceNo;
    @JsonProperty("productId")
	 private int productId;
    @JsonProperty("paymentModeId")
	 private int paymentModeId;
    @JsonProperty("amount")
	 private Double amount;
    @JsonProperty("quantity")
	 private Double quantity;
    @JsonProperty("plateNumber")
	 private String plateNumber;
    @JsonProperty("telephone")
	 private String telephone;
    @JsonProperty("name")
	 private String name;
    @JsonProperty("tin")
	 private String tin;
    @JsonProperty("voucherNumber")
	 private String voucherNumber;
    @JsonProperty("deviceTransactionTime")
	 private String deviceTransactionTime;
    @JsonProperty("authorisationCode")
    private String authorisationCode;
    @JsonProperty("authenticationCode")
	 private int authenticationCode;
    @JsonIgnore
     private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDeviceTransactionId() {
        return deviceTransactionId;
    }

    public void setDeviceTransactionId(long deviceTransactionId) {
        this.deviceTransactionId = deviceTransactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getNozzleId() {
        return nozzleId;
    }

    public void setNozzleId(int nozzleId) {
        this.nozzleId = nozzleId;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(int paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public String getAuthorisationCode() {
        return authorisationCode;
    }

    public void setAuthorisationCode(String authorisationCode) {
        this.authorisationCode = authorisationCode;
    }

    public String getDeviceTransactionTime() {
        return deviceTransactionTime;
    }

    public void setDeviceTransactionTime(String deviceTransactionTime) {
        this.deviceTransactionTime = deviceTransactionTime;
    }

    public int getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(int authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

}
