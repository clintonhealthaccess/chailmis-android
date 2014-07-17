package org.clintonhealthaccess.lmis.app.watchers;


import android.text.Editable;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderQuantityTextWatcherTest {

    @Ignore("Work in Progress")
    @Test
    public void testAfterTextChanged() throws Exception {
        OrderQuantityTextWatcher watcher = new OrderQuantityTextWatcher(Robolectric.application, mock(CommodityViewModel.class));
        watcher.afterTextChanged(new Editable.Factory().newEditable("12"));
    }


}