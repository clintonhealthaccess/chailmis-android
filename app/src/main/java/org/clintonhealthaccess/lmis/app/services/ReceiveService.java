package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReceiveService {

    @Inject
    Context context;

    @Inject
    StockService stockService;

    public List<String> getReadyAllocationIds() {
        return new ArrayList<>(Arrays.asList("UG-2004", "UG-2005"));
    }

    public List<String> getCompletedIds() {
        return new ArrayList<>();
    }

    public void saveReceive(Receive receive) {
        GenericDao<Receive> receiveDao = new GenericDao<>(Receive.class, context);
        receiveDao.create(receive);
        saveReceiveItems(receive.getReceiveItems());
    }

    private void saveReceiveItems(List<ReceiveItem> receiveItems) {
        GenericDao<ReceiveItem> receiveItemDao = new GenericDao<>(ReceiveItem.class, context);
        for (ReceiveItem receiveItem : receiveItems) {
            receiveItemDao.create(receiveItem);
            stockService.increaseStockLevelFor(receiveItem.getCommodity(), receiveItem.getQuantityReceived());
        }
    }
}
