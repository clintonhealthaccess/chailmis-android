package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class BaseCommodityViewModel {

    private boolean selected;
    private Commodity commodity;

    public BaseCommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public String getName() {
        return commodity.getName();
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean stockIsFinished() {
        return commodity.stockIsFinished();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseCommodityViewModel)) return false;

        BaseCommodityViewModel that = (BaseCommodityViewModel) o;

        return commodity.equals(that.commodity);

    }

    @Override
    public int hashCode() {
        return commodity.hashCode();
    }
}
