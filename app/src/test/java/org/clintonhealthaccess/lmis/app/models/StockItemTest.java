package org.clintonhealthaccess.lmis.app.models;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StockItemTest {

    @Test
    public void testReduceQuantityBy() throws Exception {
        StockItem item = new StockItem(new Commodity("item"), 10);
        item.reduceQuantityBy(5);
        assertThat(item.quantity(), is(5));
    }
}