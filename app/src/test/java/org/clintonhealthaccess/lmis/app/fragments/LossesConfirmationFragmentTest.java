package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.Toast;

import com.google.inject.AbstractModule;


import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.services.LossService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesConfirmationFragmentTest {

    private LossService lossServiceMock;

    @Before
    public void setUp() throws Exception {
        lossServiceMock = mock(LossService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LossService.class).toInstance(lossServiceMock);
            }
        });
        LossesConfirmationFragment lossesConfirmationFragment = LossesConfirmationFragment.newInstance(new Loss());
        startFragment(lossesConfirmationFragment);
    }

    @Test
    public void shouldCloseDialogWhenBackButtonIsPressed() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button closeButton = (Button) dialog.findViewById(R.id.button_losses_goBack);
        closeButton.performClick();
        assertFalse(dialog.isShowing());
    }

    @Test
    public void shouldSaveLossesGivenConfirmButtonIsClicked() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button confirmButton = (Button) dialog.findViewById(R.id.button_losses_confirm);
        confirmButton.performClick();

        verify(lossServiceMock).saveLoss(Matchers.<Loss>anyObject());
        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.loss_successful)));
        assertFalse(dialog.isShowing());
    }
}