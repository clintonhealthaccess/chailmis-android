package org.clintonhealthaccess.lmis.sample;

import org.clintonhealthaccess.lmis.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ReallySimpleTest {
    @Test
    public void testSomethingReallySimple() throws Exception {
        assertThat(1, equalTo(1));
    }
}
