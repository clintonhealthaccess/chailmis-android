package org.clintonhealthaccess.lmis.app.activities;

import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesActivityTest {


    private LossesActivity getLossesActivity() {
        return buildActivity(LossesActivity.class).create().get();
    }

    @Test
    public void testBuildActivity() throws Exception {
        LossesActivity lossesActivity = getLossesActivity();
        assertThat(lossesActivity, not(nullValue()));
    }
}
