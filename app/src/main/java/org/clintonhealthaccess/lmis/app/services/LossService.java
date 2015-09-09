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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.LossItemDetail;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationValue;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LossService {

    @Inject
    Context context;

    @Inject
    private StockService stockService;

    @Inject
    private CommoditySnapshotService snapshotService;

    @Inject
    CommodityService commodityService;

    public void saveLoss(Loss loss) {
        GenericDao<Loss> lossDao = new GenericDao<>(Loss.class, context);
        lossDao.create(loss);
        saveLossItems(loss.getLossItems());
    }

    private void saveLossItems(List<LossItem> lossItems) {
        GenericDao<LossItem> lossItemDao = new GenericDao<>(LossItem.class, context);
        for (LossItem lossItem : lossItems) {
            lossItemDao.create(lossItem);
            saveLossItemDetails(lossItem.getLossItemDetails());
            snapshotService.add(adjustStockLevel(lossItem), true);
            snapshotService.add(lossItem);
        }
    }

    private void saveLossItemDetails(List<LossItemDetail> lossItemDetails) {
        GenericDao<LossItemDetail> lossItemDetailDao = new GenericDao<>(LossItemDetail.class, context);
        for (LossItemDetail lossItemDetail : lossItemDetails) {
            lossItemDetailDao.create(lossItemDetail);
        }
    }

    private StockItemSnapshot adjustStockLevel(LossItem lossItem) {
        return stockService.reduceStockLevelFor(lossItem.getCommodity(), lossItem.getTotalLosses(), lossItem.created());
    }

    public List<UtilizationValue> getLossesValues(Commodity commodity, Date startDate, Date endDate) {
        List<LossItem> lossItems = GenericService.getItems(commodity, startDate, endDate, Loss.class, LossItem.class, context);

        List<UtilizationValue> utilizationValues = new ArrayList<>();

        Calendar calendar = DateUtil.calendarDate(startDate);
        Date upperLimitDate = DateUtil.addDayOfMonth(endDate, 1);
        while (calendar.getTime().before(upperLimitDate)) {
            int dayReceiveItems = getTotal(calendar.getTime(), lossItems);
            UtilizationValue utilizationValue =
                    new UtilizationValue(DateUtil.dayNumber(calendar.getTime()), dayReceiveItems);
            utilizationValues.add(utilizationValue);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return utilizationValues;
    }


    private int getTotal(final Date date, List<LossItem> lossItems) {
        List<LossItem> daysLossItems = FluentIterable.from(lossItems).filter(new Predicate<LossItem>() {
            @Override
            public boolean apply(LossItem input) {
                return DateUtil.equal(input.getLoss().getCreated(), date);
            }
        }).toList();

        int totalLoss = 0;
        for (LossItem lossItem : daysLossItems) {
            totalLoss += lossItem.getQuantity();
        }

        return totalLoss;
    }
}
