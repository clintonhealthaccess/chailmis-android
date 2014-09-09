/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

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

    private GenericDao<Loss> lossDao;
    private GenericDao<CommoditySnapshot> snapshotGenericDao;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
        lossDao = new GenericDao<>(Loss.class, Robolectric.application);
        snapshotGenericDao = new GenericDao<>(CommoditySnapshot.class, Robolectric.application);
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
        int expectedStockOnHand = stockOnHand - 1;

        Loss loss = new Loss();
        loss.addLossItem(new LossItem(commodity, 1));
        lossService.saveLoss(loss);

        commodity = commodityService.all().get(0);

        assertThat(commodity.getStockOnHand(), is(expectedStockOnHand));
    }

    @Test
    public void shouldUpdateSnapShotWhenLossItemIsSaved() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        assertThat(snapshotGenericDao.queryForAll().size(), is(0));
        int stockOnHand = commodity.getStockOnHand();
        int expectedStockOnHand = stockOnHand - 1;
        Loss loss = new Loss();
        loss.addLossItem(new LossItem(commodity, 1));
        lossService.saveLoss(loss);
        commodity = commodityService.all().get(0);
        assertThat(snapshotGenericDao.queryForAll().size(), is(3));

    }
}