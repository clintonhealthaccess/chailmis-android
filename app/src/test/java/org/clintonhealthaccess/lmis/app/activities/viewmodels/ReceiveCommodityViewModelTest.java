package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ReceiveCommodityViewModelTest {

    public static final int QUANTITY_ALLOCATED = 4;
    public static final int QUANTITY_RECEIVED = 3;
    private static final String PANADOL = "Panadol";

    @Test
    public void shouldReturnTheDifferenceQuantity() {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity("Panadol"), QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        assertThat(viewModel.getDifference(), is(1));
    }

    @Test
    public void shouldGenerateRecieveItem() {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL));
        viewModel.setQuantityAllocated(QUANTITY_ALLOCATED);
        viewModel.setQuantityReceived(QUANTITY_RECEIVED);

        ReceiveItem receiveItem = viewModel.getReceiveItem();

        assertThat(receiveItem.getCommodity().getName(), is(PANADOL));
        assertThat(receiveItem.getQuantityAllocated(), is(QUANTITY_ALLOCATED));
        assertThat(receiveItem.getQuantityReceived(), is(QUANTITY_RECEIVED));
    }
}