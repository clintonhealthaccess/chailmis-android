package org.clintonhealthaccess.lmis.app.adapters;

import android.app.Application;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getIntFromView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getStringFromView;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class ConfirmReceiveAdapterTest {

    public static final String PANADOL = "Panadol";
    public static final int QUANTITY_ALLOCATED = 3;
    public static final int QUANTITY_RECEIVED = 2;
    public static final int DIFFERENCE_QUANTITY = 1;
    private Application application;

    private ConfirmReceiveAdapter confirmReceiveAdapter;

    @Before
    public void setUp() throws Exception {
        application = Robolectric.application;
        ReceiveItem receiveItem = new ReceiveItem(new Commodity(PANADOL), QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        confirmReceiveAdapter = new ConfirmReceiveAdapter(application, R.layout.receive_confirm_list_item, Arrays.asList(receiveItem));
    }

    @Test
    public void shouldListReceiveItemsInDialog() throws Exception {

        String commodityName = getStringFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewCommodityName);
        int allocated = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityAllocated);
        int received = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityReceived);
        int difference = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityDifference);

        assertThat(commodityName, is(PANADOL));
        assertThat(allocated, is(QUANTITY_ALLOCATED));
        assertThat(received, is(QUANTITY_RECEIVED));
        assertThat(difference, is(DIFFERENCE_QUANTITY));
    }
}