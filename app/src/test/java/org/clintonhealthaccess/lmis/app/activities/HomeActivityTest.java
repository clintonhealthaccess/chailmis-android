package org.clintonhealthaccess.lmis.app.activities;

import org.clintonhealthaccess.lmis.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class HomeActivityTest {
    @Test
    public void testBuildActivity() throws Exception {
        HomeActivity homeActivity = buildActivity(HomeActivity.class).create().get();
        assertThat(homeActivity, not(nullValue()));
    }
}
