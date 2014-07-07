package org.clintonhealthaccess.lmis.app.activities;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class DispenseActivityTest {
    private DispenseActivity getDispenseActivity() {
        return buildActivity(DispenseActivity.class).create().get();
    }

    @Test
    public void testBuildActivity() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();
        assertThat(dispenseActivity, not(nullValue()));
    }

    @Test
    public void testShouldDisplayAllCategoriesAsButtons() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        LinearLayout categoryLayout = (LinearLayout) dispenseActivity.findViewById(R.id.layoutCategories);
        int buttonAmount = categoryLayout.getChildCount();
        assertThat(buttonAmount, is(6));

        for(int i=0; i < buttonAmount; i++) {
            View childView = categoryLayout.getChildAt(i);
            assertThat(childView, instanceOf(Button.class));
        }
    }
}
