package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class LossesCommodityViewModel extends CommodityViewModel {

    private int wastage, damages, expiries, missing;

    public LossesCommodityViewModel(Commodity commodity) {
        super(commodity);
    }

    public void setWastages(int wastage) {
        this.wastage = wastage;
    }

    public void setDamages(int damages) {
        this.damages = damages;
    }

    public void setExpiries(int expired) {
        this.expiries = expired;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public int getWastage() {
        return wastage;
    }

    public int getMissing() {
        return missing;
    }

    public int getDamages() {
        return damages;
    }

    public int getExpiries() {
        return expiries;
    }
}
