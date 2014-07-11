package org.clintonhealthaccess.lmis.app.activities.viewModels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class CommodityViewModel {

    private boolean selected;
    private int quantityToDispense;
    private Commodity commodity;

    public CommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
    }

    public boolean getSelected() {
        return selected;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public int getQuantityToDispense() {
        return quantityToDispense;
    }

    public void setQuantityToDispense(int quantityToDispense) {
        this.quantityToDispense = quantityToDispense;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public String getName() {
        return commodity.getName();
    }

    public boolean stockIsFinished() {
        return commodity.stockIsFinished();
    }
}
