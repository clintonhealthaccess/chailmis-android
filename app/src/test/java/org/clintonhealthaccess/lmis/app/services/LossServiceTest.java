package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class LossServiceTest {
    @Inject
    private LossService lossService;

    @Inject
    private CommodityService commodityService;

    @Inject
    private DbUtil dbUtil;
    private GenericDao<Loss> lossDao;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
        lossDao = new GenericDao<>(Loss.class, Robolectric.application);
    }

    @Test
    public void shouldSaveLossToDatabase() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        LossItem lossItem = new LossItem(commodity);

        Loss loss = new Loss();
        loss.addLossItem(lossItem);
        lossService.saveLoss(loss);

        List<Loss> losses = lossDao.queryForAll();
        assertThat(losses.size(), is(1));

        Loss lossFromDb = losses.get(0);
        assertThat(lossFromDb.getLossItemsCollection().size(), is(1));
    }

    @Test
    public void shouldUpdateCommodityStockOnHandWhenSavingLoss() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int stockOnHand = commodity.getStockOnHand();
        int expectedStockOnHand = stockOnHand - 3;

        Loss loss = new Loss();
        loss.addLossItem(new LossItem(commodity, 1, 2));
        lossService.saveLoss(loss);

        commodity = commodityService.all().get(0);

        assertThat(commodity.getStockOnHand(), is(expectedStockOnHand));
    }
}