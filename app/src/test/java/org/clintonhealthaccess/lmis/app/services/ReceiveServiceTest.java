package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveServiceTest {
    @Inject
    CommodityService commodityService;

    @Inject
    ReceiveService receiveService;

    public static final int QUANTITY_ALLOCATED = 4;
    public static final int QUANTITY_RECEIVED = 3;
    private GenericDao<Receive> receiveDao;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
        receiveDao = new GenericDao<>(Receive.class, Robolectric.application);
    }

    @Test
    public void shouldSaveReceiveAndReceiveItems() {
        Commodity commodity = commodityService.all().get(0);
        ReceiveItem receiveItem = new ReceiveItem(commodity, QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        Receive receive = new Receive();
        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);

        assertThat(receiveDao.queryForAll().size(), is(1));
        assertThat(receiveDao.queryForAll().get(0).getReceiveItemsCollection().size(), is(1));
    }

    @Test
    public void shouldUpdateCommodityStockOnHandOnSaveReceive() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int newStockOnHand = commodity.getStockOnHand()+QUANTITY_RECEIVED;

        ReceiveItem receiveItem = new ReceiveItem(commodity, QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        Receive receive = new Receive();
        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);
        commodity = commodityService.all().get(0);

        assertThat(commodity.getStockOnHand() ,is(newStockOnHand));
    }
}