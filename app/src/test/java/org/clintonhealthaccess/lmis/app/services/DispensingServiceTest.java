package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class DispensingServiceTest {
    @Inject
    DispensingService dispensingService;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);
    }

    @Test
    public void testSaveDispensingSavesItsDispensingItems() throws Exception {

        DispensingItem item1 = new DispensingItem(new Commodity("food"), 1);
        DispensingItem item2 = new DispensingItem(new Commodity("greens"), 1);

        Dispensing dispensing = new Dispensing();

        dispensing.addItem(item1);
        dispensing.addItem(item2);

        dispensingService.addDispensing(dispensing);

        assertThat(dispensingService.getAllDispensingItems(), is(notNullValue()));
        assertThat(dispensingService.getAllDispensingItems().size(), is(2));
        assertThat(dispensingService.getAllDispensingItems().get(0).getQuantity(), is(1));


    }


}