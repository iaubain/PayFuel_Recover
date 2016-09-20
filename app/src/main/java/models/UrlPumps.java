package models;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Created by Owner on 5/3/2016.
 */
public class UrlPumps {
    @JsonProperty("pumpId")
    private int pumpId;
    @JsonProperty("pumpName")
    private String pumpName;
    @JsonProperty("branchId")
    private int branchId;
    @JsonProperty("status")
    private int status;
    @JsonProperty("nozzleList")
    private List<UrlNozzles> nozzles;

    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

    public String getPumpName() {
        return pumpName;
    }

    public void setPumpName(String pumpName) {
        this.pumpName = pumpName;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<UrlNozzles> getNozzles() {
        return nozzles;
    }

    public void setNozzles(List<UrlNozzles> nozzles) {
        this.nozzles = nozzles;
    }
}
