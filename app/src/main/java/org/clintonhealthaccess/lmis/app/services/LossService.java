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

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.LossItemDetail;

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
            adjustStockLevel(lossItem);
            snapshotService.add(lossItem);
        }
        commodityService.reloadMostConsumedCommoditiesCache();
    }

    private void saveLossItemDetails(List<LossItemDetail> lossItemDetails) {
        GenericDao<LossItemDetail> lossItemDetailDao = new GenericDao<>(LossItemDetail.class, context);
        for (LossItemDetail lossItemDetail : lossItemDetails) {
            lossItemDetailDao.create(lossItemDetail);
        }
    }

    private void adjustStockLevel(LossItem lossItem) {
        stockService.reduceStockLevelFor(lossItem.getCommodity(), lossItem.getTotalLosses(), lossItem.created());
    }
}
