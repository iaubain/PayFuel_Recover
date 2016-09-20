package models;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Owner on 5/3/2016.
 */
public class UrlNozzles {

    @JsonProperty("nozzleId")
    private int nozzleId;
    @JsonProperty("nozzleName")
    private String nozzleName;
    @JsonProperty("nozzleIndex")
    private Double nozzleIndex;
    @JsonProperty("productId")
    private int productId;
    @JsonProperty("productName")
    private String productName;
    @JsonProperty("unitPrice")
    private int unitPrice;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("status")
    private int status;


    public int getNozzleId() {
        return nozzleId;
    }

    public void setNozzleId(int nozzleId) {
        this.nozzleId = nozzleId;
    }

    public String getNozzleName() {
        return nozzleName;
    }

    public void setNozzleName(String nozzleName) {
        this.nozzleName = nozzleName;
    }

    public Double getNozzleIndex() {
        return nozzleIndex;
    }

    public void setNozzleIndex(Double nozzleIndex) {
        this.nozzleIndex = nozzleIndex;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
