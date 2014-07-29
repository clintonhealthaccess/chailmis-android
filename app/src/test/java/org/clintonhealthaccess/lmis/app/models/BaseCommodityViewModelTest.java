package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BaseCommodityViewModelTest {

    @Test
    public void shouldToggleSelectedStatusOfCommodity() {
        BaseCommodityViewModel commodity = new BaseCommodityViewModel(new Commodity("Some commodity"));
        assertFalse(commodity.isSelected());
        commodity.toggleSelected();
        assertTrue(commodity.isSelected());
    }

}
