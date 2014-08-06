package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ReceiveCommodityViewModelTest {

    public static final int QUANTITY_ORDERED = 4;
    public static final int QUANTITY_RECEIVED = 3;

    @Test
    public void shouldReturnTheDifferenceQuantity() {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity("Panadol"), QUANTITY_ORDERED, QUANTITY_RECEIVED);
        assertThat(viewModel.getDifference(), is(1));
    }

}