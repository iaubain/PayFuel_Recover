package models;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Created by Owner on 5/13/2016.
 */
public class ChoosenPumps {
    @JsonProperty("assignPumpModel")
    private List<ChoosenPumpAndNozzle> assignPumpModel;

    public List<ChoosenPumpAndNozzle> getAssignPumpModel() {
        return assignPumpModel;
    }

    public void setAssignPumpModel(List<ChoosenPumpAndNozzle> assignPumpModel) {
        this.assignPumpModel = assignPumpModel;
    }
}
