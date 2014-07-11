package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CommodityViewModelTest {

    @Test
    public void shouldToggleSelectedStatusOfCommodity() {
        CommodityViewModel commodity = new CommodityViewModel(new Commodity("Some commodity"));
        assertFalse(commodity.getSelected());
        commodity.toggleSelected();
        assertTrue(commodity.getSelected());
    }
}
