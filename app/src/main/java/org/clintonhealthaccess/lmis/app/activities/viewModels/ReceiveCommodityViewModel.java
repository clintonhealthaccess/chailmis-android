package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveCommodityViewModel extends BaseCommodityViewModel {

    private boolean quantityAllocatedDisabled = false;
    private int quantityAllocated;
    private int quantityReceived;

    public ReceiveCommodityViewModel(Commodity commodity) {
        super(commodity);
    }

    public ReceiveCommodityViewModel(Commodity commodity, int quantityAllocated, int quantityReceived) {
        super(commodity);
        this.quantityAllocated = quantityAllocated;
        this.quantityReceived = quantityReceived;
    }

    public ReceiveCommodityViewModel(AllocationItem item) {
        super(item.getCommodity());
        this.quantityAllocated = item.getQuantity();
        this.quantityAllocatedDisabled = true;
    }

    public int getDifference() {
        return quantityAllocated - quantityReceived;
    }

    public ReceiveItem getReceiveItem() {
        ReceiveItem receiveItem = new ReceiveItem(this.getCommodity(), quantityAllocated, quantityReceived);
        return receiveItem;
    }
}
