package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ReceiveTest {

    public static final String PANADOL = "Panadol";

    @Test
    public void shouldAddRecieveItemToRecieve() throws Exception {
        Receive receive = new Receive();

        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL));
        viewModel.setQuantityAllocated(7);
        viewModel.setQuantityReceived(6);
        ReceiveItem receiveItem = viewModel.getReceiveItem();

        receive.addReceiveItem(receiveItem);
        assertThat(receive.getReceiveItems().size(), is(1));
        assertThat(receive.getReceiveItems().get(0).getCommodity().getName(), is(PANADOL));
    }
}