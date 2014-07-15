package org.clintonhealthaccess.lmis.app.activities;

import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderActivityTest {

    private OrderActivity orderActivity;

    private OrderActivity getOrderActivity() {
        return buildActivity(OrderActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        orderActivity = getOrderActivity();
    }

    @Test
    public void testBuildActivity() throws Exception {
        assertThat(orderActivity, not(nullValue()));
    }

    @Test
    public void shouldReturnTrueForCheckboxVisibility() throws Exception {
        assertTrue(orderActivity.allowCheckboxVisibility());
    }
}
