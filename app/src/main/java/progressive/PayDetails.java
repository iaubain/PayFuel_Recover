package progressive;

/**
 * Created by Owner on 5/19/2016.
 */
public class PayDetails {
    private int payId;
    private String tel;
    private String voucher;
    private String authorCode;
    private int authentCode;

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public String getAuthorCode() {
        return authorCode;
    }

    public void setAuthorCode(String authorCode) {
        this.authorCode = authorCode;
    }

    public int getAuthentCode() {
        return authentCode;
    }

    public void setAuthentCode(int authentCode) {
        this.authentCode = authentCode;
    }
}
