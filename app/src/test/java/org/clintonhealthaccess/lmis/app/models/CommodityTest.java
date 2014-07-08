package org.clintonhealthaccess.lmis.app.models;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CommodityTest {

    @Test
    public void shouldToggleSelectedStatusOfCommodity() {
        Commodity commodity = new Commodity("Some commodity");
        assertFalse(commodity.getSelected());
        commodity.toggleSelected();
        assertTrue(commodity.getSelected());
    }
}
