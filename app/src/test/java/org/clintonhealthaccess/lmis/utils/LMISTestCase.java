/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.utils;

import org.apache.commons.io.IOUtils;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.services.AdjustmentService;
import org.clintonhealthaccess.lmis.app.services.DispensingService;
import org.clintonhealthaccess.lmis.app.services.GenericDao;
import org.clintonhealthaccess.lmis.app.services.LossService;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import roboguice.inject.InjectResource;

import static org.robolectric.Robolectric.application;

public class LMISTestCase {
    @InjectResource(R.string.dhis2_base_url)
    protected String dhis2BaseUrl;

    protected void setUpSuccessHttpGetRequest(int i, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addPendingHttpResponse(200, rootDataSetJson);
    }

    protected void setUpSuccessHttpPostRequest(int i, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addPendingHttpResponse(200, rootDataSetJson);
    }

    protected void setUpSuccessHttpGetRequest(String uri, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addHttpResponseRule("GET", String.format("%s%s", dhis2BaseUrl, uri), new TestHttpResponse(200, rootDataSetJson));
    }

    protected void setUpSuccessHttpPostRequest(String uri, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addHttpResponseRule("POST", String.format("%s%s", dhis2BaseUrl, uri), new TestHttpResponse(200, rootDataSetJson));
    }

    private String readFixtureFile(String fileName) throws IOException {
        URL url = this.getClass().getClassLoader().getResource("fixtures/" + fileName);
        InputStream src = url.openStream();
        String content = IOUtils.toString(src);
        src.close();
        return content;
    }

    public static void adjust(Commodity commodity, int quantity, boolean positive, AdjustmentReason reason, AdjustmentService adjustmentService) {
        Adjustment adjustment = new Adjustment(commodity, quantity, positive, reason.getName());
        adjustmentService.save(Arrays.asList(adjustment));
    }

    public static void receive(Commodity commodity, int quantityReceived, ReceiveService receiveService) {
        Receive receive = new Receive("LGA");

        ReceiveItem receiveItem = new ReceiveItem();
        receiveItem.setCommodity(commodity);
        receiveItem.setQuantityAllocated(quantityReceived);
        receiveItem.setQuantityReceived(quantityReceived);

        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);
    }

    public static void dispense(Commodity commodity, int quantity, DispensingService dispensingService) {
        Dispensing dispensing = new Dispensing();
        DispensingItem dispensingItem = new DispensingItem(commodity, quantity);
        dispensing.addItem(dispensingItem);
        dispensingService.addDispensing(dispensing);
    }


    public static void lose(Commodity commodity, int quantityLost, LossService lossService) {
        Loss loss = new Loss();
        LossItem lossItem = new LossItem(commodity, quantityLost);
        loss.addLossItem(lossItem);
        lossService.saveLoss(loss);
    }


    public static void order(Commodity commodity, int quantity, OrderService orderService) {
        Order order = new Order();
        order.setSrvNumber("TEST");
        order.setOrderType(new OrderType(OrderType.ROUTINE));

        OrderCommodityViewModel orderCommodityViewModel = new OrderCommodityViewModel(commodity, quantity);
        orderCommodityViewModel.setOrderPeriodStartDate(new Date());
        orderCommodityViewModel.setOrderPeriodEndDate(new Date());

        OrderItem orderItem = new OrderItem(orderCommodityViewModel);
        order.addItem(orderItem);

        orderService.saveOrder(order);
    }

    public static StockItemSnapshot createStockItemSnapshot(Commodity commodity, Date time, int difference) {
        StockItemSnapshot stockItemSnapshot = new StockItemSnapshot(commodity,
                time, commodity.getStockOnHand() + difference);

        new GenericDao<StockItemSnapshot>(StockItemSnapshot.class, application)
                .create(stockItemSnapshot);

        return stockItemSnapshot;
    }
}
