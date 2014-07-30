package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class LossesCommodityViewModel extends BaseCommodityViewModel {

    private int wastage, damages, expiries, missing;

    public LossesCommodityViewModel(Commodity commodity) {
        super(commodity);
    }

    public void setWastages(int wastage) {
        this.wastage = wastage;
    }

    public int getWastage() {
        return wastage;
    }

    public int getMissing() {
        return missing;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public int getDamages() {
        return damages;
    }

    public void setDamages(int damages) {
        this.damages = damages;
    }

    public int getExpiries() {
        return expiries;
    }

    public void setExpiries(int expired) {
        this.expiries = expired;
    }

    public int totalLosses() {
        return wastage + damages + expiries + missing;
    }
}
