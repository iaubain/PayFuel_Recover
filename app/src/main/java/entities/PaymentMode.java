package entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class PaymentMode {
	public PaymentMode() {
	}

    @JsonProperty("paymentModeId")
	private int paymentModeId;
    @JsonProperty("name")
	private String name;
    @JsonProperty("paymentType")
	private String paymentType;
    @JsonProperty("status")
	private int status;
    @JsonProperty("descr")
	private String descr;

    public int getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(int paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
}
