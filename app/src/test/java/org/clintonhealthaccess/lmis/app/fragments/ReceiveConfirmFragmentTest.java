package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.Toast;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;


import static junit.framework.Assert.assertFalse;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveConfirmFragmentTest {

    private ReceiveService mockReceiveService;

    @Before
    public void setUp() throws Exception {
        mockReceiveService = mock(ReceiveService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ReceiveService.class).toInstance(mockReceiveService);
            }
        });

        ReceiveConfirmFragment receiveConfirmFragment = ReceiveConfirmFragment.newInstance(new Receive());
        startFragment(receiveConfirmFragment);
    }

    @Test
    public void shouldCloseFragmentWhenBackButtonIsClicked() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button closeButton = (Button) dialog.findViewById(R.id.button_receive_go_back);
        closeButton.performClick();
        assertFalse(dialog.isShowing());
    }

    @Test
    public void shouldSaveReceiveWhenConfirmButtonIsClicked() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        Button confirmButton = (Button) dialog.findViewById(R.id.button_receive_confirm);
        confirmButton.performClick();

        verify(mockReceiveService).saveReceive(Matchers.<Receive> anyObject());
        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.receive_successful)));
        assertFalse(dialog.isShowing());
    }
}