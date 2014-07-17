package org.clintonhealthaccess.lmis.app.watchers;


import android.text.Editable;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderQuantityTextWatcherTest {
    private boolean eventFired;
    private int quantity;

    @Before
    public void setUp() throws Exception {
        EventBus.getDefault().register(this);
        eventFired = false;

    }

    @Test
    public void testAfterTextChanged() throws Exception {
        OrderQuantityTextWatcher watcher = new OrderQuantityTextWatcher(mock(CommodityViewModel.class));
        watcher.afterTextChanged(new Editable.Factory().newEditable("12"));
        assertTrue(eventFired);
        assertThat(quantity, is(12));
    }

    public void onEvent(OrderQuantityChangedEvent event) {
        eventFired = true;
        quantity = event.getQuantity();
    }
}