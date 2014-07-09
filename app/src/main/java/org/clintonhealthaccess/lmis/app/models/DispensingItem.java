package org.clintonhealthaccess.lmis.app.models;

public class DispensingItem {
    Commodity commodity;
    Integer quantity;

    public DispensingItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.quantity = quantity;
    }
}
