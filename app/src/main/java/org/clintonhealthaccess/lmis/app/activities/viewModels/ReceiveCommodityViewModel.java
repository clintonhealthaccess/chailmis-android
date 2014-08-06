package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveCommodityViewModel extends BaseCommodityViewModel {

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

    public int getDifference () {
        return quantityOrdered - quantityReceived;
    }
}
