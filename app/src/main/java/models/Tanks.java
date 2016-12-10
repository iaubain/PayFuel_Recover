package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Owner on 6/21/2016.
 */
public class Tanks {
    @JsonProperty("branchName")
    private String branchName;
    @JsonProperty("tankName")
    private String tankName;
    @JsonProperty("pumpList")
    private List<UrlPumps> pumps;

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getTankName() {
        return tankName;
    }

    public void setTankName(String tankName) {
        this.tankName = tankName;
    }

    public List<UrlPumps> getPumps() {
        return pumps;
    }

    public void setPumps(List<UrlPumps> pumps) {
        this.pumps = pumps;
    }
}
