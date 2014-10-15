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

package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.util.Log;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportsService {

    @Inject
    private StockItemSnapshotService stockItemSnapshotService;
    @Inject
    private ReceiveService receiveService;
    @Inject
    private DispensingService dispensingService;

    @Inject
    Context context;

    public List<FacilityStockReportItem> getFacilityReportItemsForCategory(Category category, String startingYear,
                                                                           String startingMonth, String endingYear, String endingMonth) {
        ArrayList<FacilityStockReportItem> facilityStockReportItems = new ArrayList<>();

        try {
            Date startingDate = convertToDate(startingYear, startingMonth, true);
            Date endDate = convertToDate(endingYear, endingMonth, false);

            for (Commodity commodity : category.getCommodities()) {
                int openingStock = getOpeningStock(commodity, startingDate);
                int quantityReceived = GenericService.getTotal(commodity, startingDate, endDate,
                        Receive.class, ReceiveItem.class, context);
                int quantityDispensed = GenericService.getTotal(commodity, startingDate, endDate,
                        Dispensing.class, DispensingItem.class, context);

                FacilityStockReportItem item = new FacilityStockReportItem(commodity.getName(), openingStock, quantityReceived, 0, 0, 0, 0, 0, 0, quantityDispensed);
                facilityStockReportItems.add(item);
            }
        } catch (Exception e) {
            Log.e("ReportsService", e.getMessage());
        }

        return facilityStockReportItems;
    }

    private int getOpeningStock(Commodity commodity, Date date) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDayOfPreviousMonth = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM-dd");

        StockItemSnapshot stockItemSnapshot1 = stockItemSnapshotService.get(commodity, lastDayOfPreviousMonth);
        StockItemSnapshot latestStockItemSnapshot = stockItemSnapshotService.getLatest(commodity, lastDayOfPreviousMonth);
        if (latestStockItemSnapshot != null) {
            return latestStockItemSnapshot.getQuantity();
        }

        return 0;
    }

    private Date convertToDate(String year, String month, boolean isStartingDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM-dd");
        Date firstDateOfMonth = dateFormat.parse(year + "-" + month + "-01");
        if (isStartingDate) {
            return firstDateOfMonth;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstDateOfMonth);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }
}
