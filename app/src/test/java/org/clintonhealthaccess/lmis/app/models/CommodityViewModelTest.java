package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CommodityViewModelTest {

    @Test
    public void shouldToggleSelectedStatusOfCommodity() {
        CommodityViewModel commodity = new CommodityViewModel(new Commodity("Some commodity"));
        assertFalse(commodity.isSelected());
        commodity.toggleSelected();
        assertTrue(commodity.isSelected());
    }

    @Test
    public void testQuantityIsUnexpected() throws Exception {

        CommodityViewModel commodity = new CommodityViewModel(new Commodity("Some commodity"));
        commodity.setQuantityPopulated(10);
        assertFalse(commodity.quantityIsUnexpected(9));
        assertFalse(commodity.quantityIsUnexpected(0));
        assertTrue(commodity.quantityIsUnexpected(12));
        assertTrue(commodity.quantityIsUnexpected(15));

    }
}
