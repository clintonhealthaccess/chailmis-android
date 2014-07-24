package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.junit.Test;

import java.util.Date;

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
        commodity.setExpectedOrderQuantity(10);
        commodity.setQuantityEntered(9);
        assertFalse(commodity.quantityIsUnexpected());
        commodity.setQuantityEntered(12);
        assertTrue(commodity.quantityIsUnexpected());
        commodity.setQuantityEntered(15);
        assertTrue(commodity.quantityIsUnexpected());

    }

    @Test
    public void shouldBeValidIfDatesAreNotNullAndQuantityIsGreaterThanZero() {
        CommodityViewModel model = new CommodityViewModel(new Commodity("Some commodity"), 10);
        model.setOrderPeriodStartDate(new Date());
        model.setOrderPeriodEndDate(new Date());
        assertTrue(model.isValidAsOrderItem());
    }

    @Test
    public void shouldBeInvalidIfDatesAreNull() {
        CommodityViewModel model = new CommodityViewModel(new Commodity("Some commodity"), 10);
        assertFalse(model.isValidAsOrderItem());
    }
}
