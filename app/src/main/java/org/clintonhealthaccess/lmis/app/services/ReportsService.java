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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.reports.BinCard;
import org.clintonhealthaccess.lmis.app.models.reports.BinCardItem;
import org.clintonhealthaccess.lmis.app.models.reports.ConsumptionValue;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityConsumptionReportRH2Item;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.MonthlyVaccineUtilizationReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.services.GenericService.getItemsByDate;

public class ReportsService {

    @Inject
    StockItemSnapshotService stockItemSnapshotService;
    @Inject
    AdjustmentService adjustmentService;
    @Inject
    Context context;
    @Inject
    CommodityActionService commodityActionService;
    @Inject
    ReceiveService receiveService;
    @Inject
    DbUtil dbUtil;
    @Inject
    private CommodityService commodityService;

    @Inject
    private CategoryService categoryService;

    public List<FacilityStockReportItem> getFacilityReportItemsForCategory(
            Category category, String startingYear, String startingMonth, String endingYear, String endingMonth) {

        ArrayList<FacilityStockReportItem> facilityStockReportItems = new ArrayList<>();

        try {
            Date startingDate = convertToDate(startingYear, startingMonth, true);
            Date endDate = convertToDate(endingYear, endingMonth, false);

            category = categoryService.get(category);

            for (Commodity commodity : category.getCommodities()) {

                int openingStock = stockItemSnapshotService.getLatestStock(commodity, startingDate, true);

                int quantityReceived = GenericService.getTotal(commodity, startingDate, endDate,
                        Receive.class, ReceiveItem.class, context);
                int quantityDispensed = GenericService.getTotal(commodity, startingDate, endDate,
                        Dispensing.class, DispensingItem.class, context);
                int quantityLost = GenericService.getTotal(commodity, startingDate, endDate,
                        Loss.class, LossItem.class, context);

                int minThreshold = commodityActionService.getMonthlyValue(commodity, startingDate, endDate, DataElementType.MIN_STOCK_QUANTITY);

                int quantityAdjusted = adjustmentService.totalAdjustment(commodity, startingDate, endDate);

                int stockOnHand = stockItemSnapshotService.getLatestStock(commodity, endDate, false);

                int amc = commodityActionService.getMonthlyValue(commodity, startingDate, endDate, DataElementType.AMC);

                int maxThreshold = commodityActionService.getMonthlyValue(commodity, startingDate, endDate, DataElementType.MAX_STOCK_QUANTITY);

                int stockOutDays = stockItemSnapshotService.getStockOutDays(commodity, startingDate, endDate);

                FacilityStockReportItem item = new FacilityStockReportItem(commodity.getName(),
                        openingStock, quantityReceived, quantityAdjusted, quantityLost,
                        amc, stockOutDays, maxThreshold, minThreshold, quantityDispensed, stockOnHand);

                facilityStockReportItems.add(item);
            }
        } catch (Exception e) {
            Log.e("ReportsService", e.getMessage());
        }
        return facilityStockReportItems;
    }

    public List<FacilityCommodityConsumptionRH1ReportItem> getFacilityCommodityConsumptionReportRH1(Category category, String startingYear,
                                                                                                    String startingMonth, String endingYear, String endingMonth) {

        ArrayList<FacilityCommodityConsumptionRH1ReportItem> items = new ArrayList<>();
        try {
            Date startingDate = convertToDate(startingYear, startingMonth, true);
            Date endDate = convertToDate(endingYear, endingMonth, false);

            category = categoryService.get(category);

            for (Commodity commodity : category.getCommodities()) {
                FacilityCommodityConsumptionRH1ReportItem reportItem = new FacilityCommodityConsumptionRH1ReportItem(commodity);
                reportItem.setValues(getConsumptionValuesForCommodityBetweenDates(commodity, startingDate, endDate));
                items.add(reportItem);
            }
        } catch (ParseException e) {
            Log.e("ReportsService", e.getMessage());
        }


        return items;
    }

    protected ArrayList<ConsumptionValue> getConsumptionValuesForCommodityBetweenDates(final Commodity commodity, final Date startingDate, final Date endDate) {
        ArrayList<ConsumptionValue> consumptionValues = new ArrayList<>();
        List<DispensingItem> dispensingItems = GenericService.getItems(commodity, startingDate, endDate, Dispensing.class, DispensingItem.class, context);

        Date stopDate = DateUtil.addDayOfMonth(endDate, 1);
        Date currentDate = startingDate;
        while (currentDate.before(stopDate)) {
            int total = GenericService.getTotal(currentDate, dispensingItems);
            ConsumptionValue consumptionValue = new ConsumptionValue(currentDate, total);
            consumptionValues.add(consumptionValue);
            currentDate = DateUtil.addDayOfMonth(currentDate, 1);
        }

        return consumptionValues;
    }

    public List<DateTime> getDays(Date start, Date stop) {
        DateTime actualStart = new DateTime(start).withTimeAtStartOfDay();
        DateTime actualEnd = new DateTime(stop).withTimeAtStartOfDay();
        DateTime inter = actualStart;
        List<DateTime> days = new ArrayList<>();
        while (inter.compareTo(actualEnd) < 0) {
            days.add(inter);
            inter = inter.plusDays(1);
        }
        return days;
    }

    public List<Integer> getDayNumbers(Date startDate, Date endDate) {
        List<Integer> days = new ArrayList<>();

        Calendar calendar = DateUtil.calendarDate(startDate);
        Date upperLimitDate = DateUtil.addDayOfMonth(endDate, 1);

        while (calendar.getTime().before(upperLimitDate)) {
            days.add(DateUtil.dayNumber(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }


    private ListMultimap<Date, DispensingItem> groupDispensingItems(List<DispensingItem> dispensingItems) {
        return Multimaps.index(dispensingItems, new Function<DispensingItem, Date>() {
            @Override
            public Date apply(DispensingItem input) {
                return new DateTime(input.created()).withTimeAtStartOfDay().toDate();
            }
        });
    }

    public List<FacilityConsumptionReportRH2Item> getFacilityConsumptionReportRH2Items(Category category, String startingYear, String startingMonth, String endingYear, String endingMonth) {
        ArrayList<FacilityConsumptionReportRH2Item> facilityConsumptionReportRH2Items = new ArrayList<>();

        try {
            Date startingDate = convertToDate(startingYear, startingMonth, true);
            Date endDate = convertToDate(endingYear, endingMonth, false);

            category = categoryService.get(category);
            for (Commodity commodity : category.getCommodities()) {

                int openingStock = stockItemSnapshotService.getLatestStock(commodity, startingDate, true);

                int quantityReceived = GenericService.getTotal(commodity, startingDate, endDate,
                        Receive.class, ReceiveItem.class, context);

                int quantityDispensedToClients = GenericService.getTotal(commodity, startingDate, endDate,
                        Dispensing.class, DispensingItem.class, context);
                int quantityAdjusted = adjustmentService.totalAdjustment(commodity, startingDate, endDate);

                int quantityLost = GenericService.getTotal(commodity, startingDate, endDate,
                        Loss.class, LossItem.class, context);

                int commoditiesDispensedToFacilities = adjustmentService.totalAdjustment(commodity, startingDate, endDate, AdjustmentReason.SENT_TO_ANOTHER_FACILITY);

                int closingStock = stockItemSnapshotService.getLatestStock(commodity, endDate, false);

                FacilityConsumptionReportRH2Item item = new FacilityConsumptionReportRH2Item(commodity.getName(),
                        openingStock, quantityReceived, quantityDispensedToClients, quantityAdjusted,
                        commoditiesDispensedToFacilities, quantityLost, closingStock);

                facilityConsumptionReportRH2Items.add(item);
            }
        } catch (Exception e) {
            Log.e("ReportsService", e.getMessage());
        }

        return facilityConsumptionReportRH2Items;
    }

    public Date convertToDate(String year, String month, boolean isStartingDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMMM-dd");
        Date firstDateOfMonth = dateFormat.parse(year + "-" + month + "-01");
        if (isStartingDate) {
            return firstDateOfMonth;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstDateOfMonth);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date todayWithoutTime = dateFormat.parse(dateFormat.format(new Date()));
        return calendar.getTime().after(todayWithoutTime) ? todayWithoutTime : calendar.getTime();
    }


    public List<MonthlyVaccineUtilizationReportItem> getMonthlyVaccineUtilizationReportItems(
            Category category, String year, String month, boolean isForDevices) {

        ArrayList<MonthlyVaccineUtilizationReportItem> reportItems = new ArrayList<>();

        try {
            Date date = convertToDate(year, month, true);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            category = categoryService.get(category);

            for (Commodity commodity : category.getCommodities()) {
                if (isForDevices && !commodity.isDevice() || !isForDevices && commodity.isDevice()) {
                    continue;
                }
                List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, date);
                MonthlyVaccineUtilizationReportItem monthlyVaccineUtilizationReportItem
                        = new MonthlyVaccineUtilizationReportItem(commodity.getName(), utilizationItems);
                reportItems.add(monthlyVaccineUtilizationReportItem);
            }
        } catch (Exception e) {
            Log.e("ReportsService", e.getMessage());
        }
        return reportItems;
    }

    public BinCard generateBinCard(Commodity commodity) throws Exception {

        List<String> receiveSources = Arrays.asList(context.getString(R.string.lga),
                context.getString(R.string.zonal_store), context.getString(R.string.others));

        Date today = new Date();
        Date tomorrow = DateUtil.addDayOfMonth(today, 1);
        Date startDate = DateUtil.addDayOfMonth(today, -31);

        List<ReceiveItem> receiveItems = GenericService.getItems(commodity, startDate, today, Receive.class, ReceiveItem.class, context);
        List<DispensingItem> dispensingItems = GenericService.getItems(commodity, startDate, today, Dispensing.class, DispensingItem.class, context);
        List<LossItem> lossItems = GenericService.getItems(commodity, startDate, today, Loss.class, LossItem.class, context);
        List<StockItemSnapshot> stockItemSnapshots = stockItemSnapshotService.get(commodity, startDate, today);
        List<Adjustment> adjustments = adjustmentService.getAdjustments(commodity, startDate, today);

        int previousDaysClosingStock = stockItemSnapshotService.getLatestStock(commodity, startDate, false);

        List<BinCardItem> binCardItems = new ArrayList<>();
        Date date = startDate;
        while (date.before(tomorrow)) {

            int quantityDispensed = GenericService.getTotal(date, dispensingItems);
            int quantityLost = GenericService.getTotal(date, lossItems);
            int closingBalance = previousDaysClosingStock;

            List<ReceivedBySource> receivedBySources = new ArrayList<>();
            for (final String source : receiveSources) {
                List<ReceiveItem> receiveItemsForDate = getItemsByDate(date, receiveItems);
                List<ReceiveItem> sourceReceiveItems = FluentIterable.from(receiveItemsForDate).filter(new Predicate<ReceiveItem>() {
                    @Override
                    public boolean apply(ReceiveItem input) {
                        return input.getReceive().getSource().equalsIgnoreCase(source);
                    }
                }).toList();

                int quantityReceived = 0;
                if (sourceReceiveItems != null) {
                    for (ReceiveItem item : sourceReceiveItems) {
                        quantityReceived += item.getQuantity();
                    }
                }

                if (quantityReceived > 0) {
                    receivedBySources.add(new ReceivedBySource(quantityReceived, source));
                }
            }

            int quantitySentToAnotherFacility =
                    adjustmentService.totalAdjustment(
                            commodity, adjustments, date, AdjustmentReason.SENT_TO_ANOTHER_FACILITY);

            int quantityReceivedFromAnotherFacility =
                    adjustmentService.totalAdjustment(commodity, adjustments, date,
                            AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY);

            int quantityAdjusted = adjustmentService.totalAdjustment(
                    commodity, adjustments, date,
                    Arrays.asList(AdjustmentReason.PHYSICAL_COUNT, AdjustmentReason.RETURNED_TO_LGA));

            StockItemSnapshot dayStockItemSnapshot = stockItemSnapshotService.getSnapshot(date, stockItemSnapshots);
            if (dayStockItemSnapshot != null) {
                closingBalance = dayStockItemSnapshot.getQuantity();
            }

            if (quantityReceivedFromAnotherFacility > 0) {
                binCardItems.add(new BinCardItem(date, "Received from Facility", 0, 0, 0, quantityReceivedFromAnotherFacility, closingBalance));
            }

            if (quantitySentToAnotherFacility > 0) {
                binCardItems.add(new BinCardItem(date, "Sent to Facility", 0, 0, 0, quantitySentToAnotherFacility, closingBalance));
            }

            if (quantityDispensed > 0 || quantityLost > 0 || quantityAdjusted > 0 || receivedBySources.size() > 0) {
                binCardItems.add(new BinCardItem(date,
                        (receivedBySources.size() > 0 ? receivedBySources.get(0).getSource() : ""),
                        (receivedBySources.size() > 0 ? receivedBySources.get(0).getQuantityReceived() : 0),
                        quantityDispensed, quantityLost, quantityAdjusted, closingBalance));
            }

            if (receivedBySources.size() > 1) {
                for (int i = 1; i < receivedBySources.size(); i++) {
                    binCardItems.add(new BinCardItem(date, receivedBySources.get(i).getSource(), receivedBySources.get(i).getQuantityReceived(), 0, 0, 0, closingBalance));
                }
            }

            previousDaysClosingStock = closingBalance;
            date = DateUtil.addDayOfMonth(date, 1);
        }

        return new BinCard(commodity.getMinimumThreshold(), commodity.getMaximumThreshold(), binCardItems, commodity);
    }

    private class ReceivedBySource {
        private int quantityReceived;
        private String source;

        public ReceivedBySource(int quantityReceived, String source) {
            this.quantityReceived = quantityReceived;
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        public int getQuantityReceived() {
            return quantityReceived;
        }
    }
}
