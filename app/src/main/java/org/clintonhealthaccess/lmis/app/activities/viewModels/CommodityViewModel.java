package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.io.Serializable;

public class CommodityViewModel implements Serializable {
    private boolean selected;
    private int quantityEntered;
    private Commodity commodity;
    private int quantityPopulated;

    public CommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public int getQuantityEntered() {
        return quantityEntered;
    }

    public void setQuantityEntered(int quantityEntered) {
        this.quantityEntered = quantityEntered;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommodityViewModel that = (CommodityViewModel) o;

        return commodity.equals(that.commodity);
    }

    @Override
    public int hashCode() {
        return commodity.hashCode();
    }

    public boolean quantityIsUnexpected() {
        return (quantityEntered > 1.1 * this.quantityPopulated);
    }

    public int getQuantityPopulated() {
        return quantityPopulated;
    }

    public void setQuantityPopulated(int quantityPopulated) {
        this.quantityPopulated = quantityPopulated;
    }
}
