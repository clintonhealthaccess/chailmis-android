package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderConfirmationFragmentTest {
    private OrderConfirmationFragment orderConfirmationFragment;

    private OrderService mockOrderingService;


    @Before
    public void setUp() throws Exception {
        mockOrderingService = mock(OrderService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrderService.class).toInstance(mockOrderingService);
            }
        });

        orderConfirmationFragment = OrderConfirmationFragment.newInstance(new Order());
        startFragment(orderConfirmationFragment);
    }

    @Test
    public void testConfirmButtonLogic() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button buttonClose = (Button) dialog.findViewById(R.id.buttonOrderConfirm);
        buttonClose.callOnClick();
        verify(mockOrderingService).saveOrder(Matchers.<Order>anyObject());
        assertFalse(dialog.isShowing());

    }

    @Test
    public void testGoBackButtonLogic() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button buttonGoBack = (Button) dialog.findViewById(R.id.buttonOrderGoBack);
        buttonGoBack.callOnClick();
        verify(mockOrderingService, never()).saveOrder(Matchers.<Order>anyObject());
        assertFalse(dialog.isShowing());

    }
}