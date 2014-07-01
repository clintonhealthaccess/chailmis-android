package org.clintonhealthaccess.lmis.app;

import org.clintonhealthaccess.lmis.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

@RunWith(RobolectricGradleTestRunner.class)
public class HomeActivityTest {
    @Test
    public void testBuildActivity() throws Exception {
        HomeActivity homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();
        assertThat(homeActivity, not(nullValue()));
    }
}
