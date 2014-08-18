/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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
