package appBean;

import java.util.List;

/**
 * Created by Owner on 5/5/2016.
 */
public class PumpAndNozzleList {
    private List<PumpAndNozzle> selectedPumpAndNozzle;

    public List<PumpAndNozzle> getSelectedPumpAndNozzle() {
        return selectedPumpAndNozzle;
    }

    public void setSelectedPumpAndNozzle(List<PumpAndNozzle> selectedPumpAndNozzle) {
        this.selectedPumpAndNozzle = selectedPumpAndNozzle;
    }
}
