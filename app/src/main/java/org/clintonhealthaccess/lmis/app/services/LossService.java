package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;

import java.util.List;

public class LossService {

    @Inject
    Context context;

    @Inject
    private StockService stockService;

    public void saveLoss(Loss loss) {
        GenericDao<Loss> lossDao = new GenericDao<>(Loss.class, context);
        lossDao.create(loss);
        saveLossItems(loss.getLossItems());
    }

    private void saveLossItems(List<LossItem> lossItems) {
        GenericDao<LossItem> lossItemDao = new GenericDao<>(LossItem.class, context);
        for (LossItem lossItem : lossItems) {
            lossItemDao.create(lossItem);
            adjustStockLevel(lossItem);
        }
    }

    private void adjustStockLevel(LossItem lossItem) {
        stockService.reduceStockLevelFor(lossItem.getCommodity(), lossItem.getTotalLosses());
    }
}
