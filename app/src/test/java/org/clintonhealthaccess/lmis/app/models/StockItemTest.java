package org.clintonhealthaccess.lmis.app.models;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StockItemTest {

    @Test
    public void testReduceQuantityBy() throws Exception {
        StockItem item = new StockItem(new Commodity("item"), 10);
        item.reduceQuantityBy(5);
        assertThat(item.getQuantity(), is(5));
    }

    @Test
    public void testIsFinished() throws Exception {
        StockItem item = new StockItem(new Commodity("item"), 0);
        assertTrue(item.isFinished());
        StockItem item2 = new StockItem(new Commodity("item"), 10);
        assertFalse(item2.isFinished());
    }
}