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

import android.content.Context;
import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.events.AllocationCreateEvent;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationValue;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReceiveService {

    @Inject
    Context context;

    @Inject
    StockService stockService;
    @Inject
    AllocationService allocationService;
    @Inject
    AlertsService alertsService;

    @Inject
    CommoditySnapshotService commoditySnapshotService;

    @Inject
    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;

    public List<String> getReadyAllocationIds() {
        return new ArrayList<>(Arrays.asList("UG-2004", "UG-2005"));
    }

    public List<String> getCompletedIds() {
        return new ArrayList<>();
    }

    public void saveReceive(Receive receive) {
        try {
            GenericDao<Receive> receiveDao = new GenericDao<>(Receive.class, context);
            receiveDao.create(receive);
            saveReceiveItems(receive.getReceiveItems());


            if (receive.getAllocation() != null) {
                if (!receive.getAllocation().isDummy()) {
                    Allocation allocation = receive.getAllocation();
                    allocation.setReceived(true);
                    allocationService.update(allocation);

                    alertsService.deleteAllocationAlert(allocation);
                } else {
                    allocationService.createAllocation(receive.getAllocation());
                    EventBus.getDefault().post(new AllocationCreateEvent(receive.getAllocation()));
                }
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveReceiveItems(List<ReceiveItem> receiveItems) {
        GenericDao<ReceiveItem> receiveItemDao = new GenericDao<>(ReceiveItem.class, context);
        for (ReceiveItem receiveItem : receiveItems) {
            receiveItemDao.create(receiveItem);
            stockService.increaseStockLevelFor(receiveItem.getCommodity(), receiveItem.getQuantityReceived(), receiveItem.created());
            commoditySnapshotService.add(receiveItem);
        }
        commodityService.reloadMostConsumedCommoditiesCache();
    }

    public List<UtilizationValue> getReceivedValues(Commodity commodity, Date startDate, Date endDate) {
        List<ReceiveItem> receiveItems = GenericService.getItems(commodity, startDate, endDate, Receive.class, ReceiveItem.class, context);

        List<UtilizationValue> utilizationValues = new ArrayList<>();

        Calendar calendar = DateUtil.calendarDate(startDate);
        Date upperLimitDate = DateUtil.addDayOfMonth(endDate, 1);
        while (calendar.getTime().before(upperLimitDate)) {
            int dayReceiveItems = getTotal(calendar.getTime(), receiveItems);
            UtilizationValue utilizationValue =
                    new UtilizationValue(DateUtil.dayNumber(calendar.getTime()), dayReceiveItems);
            utilizationValues.add(utilizationValue);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return utilizationValues;
    }

    public int getTotal(final Date date, List<ReceiveItem> receiveItems) {
        List<ReceiveItem> daysReceiveItems = FluentIterable.from(receiveItems).filter(new Predicate<ReceiveItem>() {
            @Override
            public boolean apply(ReceiveItem input) {
                return DateUtil.equal(input.getReceive().getCreated(), date);
            }
        }).toList();

        int totalReceived = 0;
        for (ReceiveItem receiveItem : daysReceiveItems) {
            totalReceived += receiveItem.getQuantityReceived();
        }

        return totalReceived;
    }

}
