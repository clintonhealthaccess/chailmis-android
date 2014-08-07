package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveCommodityViewModel extends BaseCommodityViewModel {

    private boolean quantityOrderedDisabled = false;
    private int quantityOrdered;
    private int quantityReceived;

    public ReceiveCommodityViewModel(Commodity commodity) {
        super(commodity);
    }

    public ReceiveCommodityViewModel(Commodity commodity, int quantityOrdered, int quantityReceived) {
        super(commodity);
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
    }

    public ReceiveCommodityViewModel(AllocationItem item) {
        super(item.getCommodity());
        this.quantityOrdered = item.getQuantity();
        this.quantityOrderedDisabled = true;
    }

    public int getDifference() {
        return quantityOrdered - quantityReceived;
    }
}
