package org.clintonhealthaccess.lmis.app.activities;

import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

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
}
