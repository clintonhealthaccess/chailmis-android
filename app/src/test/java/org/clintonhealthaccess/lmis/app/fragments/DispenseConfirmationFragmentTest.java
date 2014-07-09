package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.services.DispensingService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.shadows.ShadowDialog;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class DispenseConfirmationFragmentTest {

    private DispenseConfirmationFragment dispenseConfirmationFragment;


    @Before
    public void setUp() throws Exception {
        setUpInjection(this);

        dispenseConfirmationFragment = DispenseConfirmationFragment.newInstance(new Dispensing());
        startFragment(dispenseConfirmationFragment);
    }

    @Test
    public void testConfirmButtonExists() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        Button buttonClose = (Button) dialog.findViewById(R.id.buttonDispenseConfirm);
        assertThat(buttonClose, not(nullValue()));

    }


}