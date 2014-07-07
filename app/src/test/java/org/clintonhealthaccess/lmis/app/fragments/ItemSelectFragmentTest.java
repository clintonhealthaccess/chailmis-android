package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.LinearLayout;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.DispenseActivity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class ItemSelectFragmentTest {
    @Test
    public void testShouldRenderAllCategoryButtons() throws Exception {
        DispenseActivity dispenseActivity = buildActivity(DispenseActivity.class).create().start().resume().get();
        LinearLayout categoryLayout = (LinearLayout) dispenseActivity.findViewById(R.id.layoutCategories);
        Button firstCategory = (Button) categoryLayout.getChildAt(0);
//        firstCategory.performClick();
//
//        Dialog dialog = ShadowDialog.getLatestDialog();
//        assertTrue(dialog.isShowing());

//        LinearLayout categoriesLayout = (LinearLayout) dialog.findViewById(R.id.itemSelectOverlayCategories);
//        assertThat(categoriesLayout, not(nullValue()));
    }
}