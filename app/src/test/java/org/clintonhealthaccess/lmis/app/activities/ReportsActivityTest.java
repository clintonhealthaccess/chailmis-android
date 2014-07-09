package org.clintonhealthaccess.lmis.app.activities;

import android.support.v7.internal.view.menu.MenuBuilder;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class ReportsActivityTest {


    private ReportsActivity getReportsActivity() {
        return buildActivity(ReportsActivity.class).create().get();
    }

    @Test
    public void testBuildActivity() throws Exception {
        ReportsActivity reportsActivity = getReportsActivity();
        assertThat(reportsActivity, not(nullValue()));
    }

    @Test
    public void testCanChangeHeaderText() throws Exception {
        ReportsActivity reportsActivity = getReportsActivity();
        String name = "James";
        reportsActivity.setFacilityName(name);
        assertThat(reportsActivity.textFacilityName.getText().toString(), is(name));

    }

    @Ignore("we didn't find a way to verify menu item visible yet.")
    @Test
    public void testHelpIsAvailableAsAMenuItem() {
        ReportsActivity reportsActivity = getReportsActivity();
        MenuBuilder menu = new MenuBuilder(reportsActivity);
        reportsActivity.onCreateOptionsMenu(menu);
        //reportsActivity.menu1
        assertTrue(menu.findItem(R.id.action_help).isVisible());
    }

}
