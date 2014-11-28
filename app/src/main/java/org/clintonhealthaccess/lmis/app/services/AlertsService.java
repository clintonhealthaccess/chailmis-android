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

import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.alerts.AllocationAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.MonthlyStockCountAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.NotificationMessage;
import org.clintonhealthaccess.lmis.app.models.alerts.RoutineOrderAlert;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.activities.OrderActivity.setupOrderCommodityViewModel;

public class AlertsService {

    public static final String DISABLED = "disabled";
    public static SimpleDateFormat ALERT_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy");
    private static List<LowStockAlert> lowStockAlerts;
    @Inject
    CommodityService commodityService;
    @Inject
    AllocationService allocationService;

    @Inject
    DbUtil dbUtil;
    @Inject
    SharedPreferences sharedPreferences;

    public List<LowStockAlert> getLowStockAlerts() {
        if (lowStockAlerts == null) {
            AlertsService.lowStockAlerts = queryLowStockAlertsFromDB();
            return AlertsService.lowStockAlerts;
        }
        return lowStockAlerts;
    }

    private List<LowStockAlert> queryLowStockAlertsFromDB() {
        List<LowStockAlert> lowStockAlerts = queryAllLowStockAlerts();
        Collections.sort(lowStockAlerts, new Comparator<LowStockAlert>() {
            @Override
            public int compare(LowStockAlert lhs, LowStockAlert rhs) {
                return new Integer(lhs.getCommodity().getStockOnHand()).compareTo(new Integer(rhs.getCommodity().getStockOnHand()));
            }
        });
        return lowStockAlerts;
    }

    public List<LowStockAlert> getEnabledLowStockAlerts() {
        return from(getLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
            @Override
            public boolean apply(LowStockAlert input) {
                return !input.isDisabled();
            }
        }).toList();
    }


    public int numberOfAlerts() {
        return getEnabledLowStockAlerts().size() + getNumberOfRoutineOrderAlerts() + getAllocationAlerts().size() + getEnabledMonthlyStockAlerts().size();
    }

    public int getNumberOfRoutineOrderAlerts() {
        return getAllRoutineOrderAlerts().size();
    }

    public List<LowStockAlert> getTop5LowStockAlerts() {
        List<LowStockAlert> lowStockAlerts = getEnabledLowStockAlerts();
        if (lowStockAlerts.size() > 5) {
            return lowStockAlerts.subList(0, 5);
        } else {
            return lowStockAlerts;
        }
    }

    public void updateLowStockAlerts() {
        checkIfExistingLowStockAlertsAreStillValid();
        checkForNewLowStockAlerts();
        updateCache();
    }

    public void updateCommodityLowStockAlert(Commodity commodity) {
        LowStockAlert alert = queryLowStockAlert(commodity);
        if (alert == null && commodity.isBelowThreshold()) {
            createAlert(new LowStockAlert(commodity));
        } else if (alert != null && !alert.getCommodity().isBelowThreshold()) {
            deleteLowStockAlert(alert);
        }
        updateCache();

    }

    private LowStockAlert queryLowStockAlert(final Commodity commodity) {
        return dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, LowStockAlert>() {
            @Override
            public LowStockAlert operate(Dao<LowStockAlert, String> dao) throws SQLException {
                QueryBuilder queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq("commodity_id", commodity.getId());
                return (LowStockAlert) queryBuilder.queryForFirst();
            }
        });
    }

    public void updateCache() {
        List<LowStockAlert> alerts = queryLowStockAlertsFromDB();
        this.lowStockAlerts = null;
        AlertsService.lowStockAlerts = alerts;
    }

    private void checkForNewLowStockAlerts() {
        List<Commodity> commodities = commodityService.all();
        List<Commodity> commoditiesInAlerts = getCommoditiesInLowStockAlerts();
        for (Commodity commodity : commodities) {
            if (!commoditiesInAlerts.contains(commodity)) {
                try {
                    if (commodity.isBelowThreshold()) {
                        LowStockAlert lowStockAlert = new LowStockAlert(commodity);
                        createAlert(lowStockAlert);
                    }
                } catch (Exception ex) {
                    Log.e("Alert service:", ex.getMessage());
                }
            }
        }
    }

    public void disableAlertsForCommodities(List<Commodity> commodities) {
        List<LowStockAlert> lowStockAlerts = getLowStockAlertsForCommodities(commodities);
        for (LowStockAlert alert : lowStockAlerts) {
            disableLowStockAlert(alert);
        }
        updateCache();
    }

    public void disableAllRoutineOrderAlerts() {
        dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, Integer>() {
            @Override
            public Integer operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                UpdateBuilder<RoutineOrderAlert, String> updateBuilder = dao.updateBuilder();
                updateBuilder.where().eq(DISABLED, false);
                updateBuilder.updateColumnValue(DISABLED, true);
                return updateBuilder.update();
            }
        });
    }

    private ImmutableList<Commodity> getCommoditiesInLowStockAlerts() {
        return from(queryAllLowStockAlerts()).transform(new Function<LowStockAlert, Commodity>() {
            @Override
            public Commodity apply(LowStockAlert input) {
                return input.getCommodity();
            }
        }).toList();
    }

    public ImmutableList<LowStockAlert> getLowStockAlertsForCommodities(final List<Commodity> commodities) {
        return
                from(queryAllLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
                    @Override
                    public boolean apply(LowStockAlert input) {
                        return commodities.contains(input.getCommodity());
                    }
                }).toList();
    }

    public void createAlert(final LowStockAlert lowStockAlert) {
        dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, Object>() {
            @Override
            public Object operate(Dao<LowStockAlert, String> dao) throws SQLException {
                dao.create(lowStockAlert);
                return null;
            }
        });
    }

    private void checkIfExistingLowStockAlertsAreStillValid() {
        List<LowStockAlert> availableLowStockAlerts = queryAllLowStockAlerts();
        for (LowStockAlert alert : availableLowStockAlerts) {
            if (!alert.getCommodity().isBelowThreshold()) {
                deleteLowStockAlert(alert);
            }
        }
    }

    public void disableLowStockAlert(LowStockAlert alert) {
        alert.setDisabled(true);
        alert.setDateDisabled(new Date());
        updateLowStockAlert(alert);
    }

    public void deleteLowStockAlert(final LowStockAlert alert) {
        dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, Void>() {
            @Override
            public Void operate(Dao<LowStockAlert, String> dao) throws SQLException {
                dao.delete(alert);
                return null;
            }
        });
    }

    private List<LowStockAlert> queryAllLowStockAlerts() {
        return dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, List<LowStockAlert>>() {
            @Override
            public List<LowStockAlert> operate(Dao<LowStockAlert, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public void updateLowStockAlert(final LowStockAlert lowStockAlert) {
        dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, Object>() {
            @Override
            public Object operate(Dao<LowStockAlert, String> dao) throws SQLException {
                dao.update(lowStockAlert);
                return null;
            }
        });
    }

    public List<OrderCommodityViewModel> getOrderCommodityViewModelsForLowStockAlert() {
        return from(getEnabledLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
            @Override
            public boolean apply(LowStockAlert input) {
                return !input.getCommodity().isNonLGA();
            }
        }).transform(new Function<LowStockAlert, OrderCommodityViewModel>() {
            @Override
            public OrderCommodityViewModel apply(LowStockAlert lowStockAlert) {
                int quantity = lowStockAlert.getCommodity().calculateEmergencyPrepopulatedQuantity();
                OrderCommodityViewModel orderCommodityViewModel = createOrderCommodityViewModel(quantity, lowStockAlert.getCommodity());
                return orderCommodityViewModel;
            }
        }).toList();
    }

    private OrderCommodityViewModel createOrderCommodityViewModel(int quantity, Commodity commodity) {
        OrderCommodityViewModel orderCommodityViewModel = setupOrderCommodityViewModel(commodity);
        orderCommodityViewModel.setQuantityEntered(quantity);
        orderCommodityViewModel.setExpectedOrderQuantity(quantity);
        return orderCommodityViewModel;
    }

    public int getRoutineOrderAlertDay() {
        return sharedPreferences.getInt(CommodityService.ROUTINE_ORDER_ALERT_DAY, 24);
    }

    public void generateRoutineOrderAlert(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.DAY_OF_MONTH) < getRoutineOrderAlertDay()) {
            return;
        }

        if (getRoutineOrderAlertsInCurrentMonth(date).size() == 0) {
            RoutineOrderAlert routineOrderAlert = new RoutineOrderAlert(date);
            createRoutineOrderAlert(routineOrderAlert);
        }
    }

    public List<? extends NotificationMessage> getNotificationMessagesForHomePage() {
        List<NotificationMessage> messages = new ArrayList<>();
        RoutineOrderAlert latestRoutineOrderAlerts = getLatestRoutineOrderAlerts();
        if (latestRoutineOrderAlerts != null) {
            messages.add(latestRoutineOrderAlerts);
        }
        messages.addAll(getAllocationAlerts());
        messages.addAll(getEnabledMonthlyStockAlerts());
        if (messages.size() > 5) {
            return messages.subList(0, 5);
        }
        return messages;
    }

    public List<? extends NotificationMessage> getNotificationMessages() {
        List<NotificationMessage> messages = new ArrayList<>();
        messages.addAll(getAllRoutineOrderAlerts());
        messages.addAll(getAllocationAlerts());
        messages.addAll(getEnabledMonthlyStockAlerts());
        return newArrayList(messages);
    }

    protected List<RoutineOrderAlert> getRoutineOrderAlertsInCurrentMonth(final Date date) {
        return dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, List<RoutineOrderAlert>>() {
            @Override
            public List<RoutineOrderAlert> operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                Date firstDay = Helpers.firstDayOfMonth(date);
                Date lastDay = Helpers.lastDayOfMonth(date);
                return dao.queryBuilder().where().between(RoutineOrderAlert.DATE_CREATED, firstDay, lastDay).query();
            }
        });

    }

    private void createRoutineOrderAlert(final RoutineOrderAlert routineOrderAlert) {
        dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, Object>() {
            @Override
            public Object operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                dao.create(routineOrderAlert);
                return null;
            }
        });
    }

    private void createMonthlyStockCountAlert(final MonthlyStockCountAlert monthlyStockCountAlert) {
        dbUtil.withDao(MonthlyStockCountAlert.class, new DbUtil.Operation<MonthlyStockCountAlert, Object>() {
            @Override
            public Object operate(Dao<MonthlyStockCountAlert, String> dao) throws SQLException {
                dao.create(monthlyStockCountAlert);
                return null;
            }
        });
    }

    private List<RoutineOrderAlert> getAllRoutineOrderAlerts() {
        return dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, List<RoutineOrderAlert>>() {
            @Override
            public List<RoutineOrderAlert> operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq(DISABLED, false).query();
            }
        });
    }

    public List<MonthlyStockCountAlert> getEnabledMonthlyStockAlerts() {
        return dbUtil.withDao(MonthlyStockCountAlert.class, new DbUtil.Operation<MonthlyStockCountAlert, List<MonthlyStockCountAlert>>() {
            @Override
            public List<MonthlyStockCountAlert> operate(Dao<MonthlyStockCountAlert, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq(DISABLED, false).query();
            }
        });
    }

    private RoutineOrderAlert getLatestRoutineOrderAlerts() {
        return dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, RoutineOrderAlert>() {
            @Override
            public RoutineOrderAlert operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                QueryBuilder<RoutineOrderAlert, String> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq(DISABLED, false);
                return queryBuilder.orderBy(RoutineOrderAlert.DATE_CREATED, false).queryForFirst();
            }
        });
    }

    public List<OrderCommodityViewModel> getOrderViewModelsForRoutineOrderAlert() {
        return from(commodityService.all()).filter(new Predicate<Commodity>() {
            @Override
            public boolean apply(Commodity input) {
                return !input.isNonLGA();
            }
        }).transform(new Function<Commodity, OrderCommodityViewModel>() {
            @Override
            public OrderCommodityViewModel apply(Commodity commodity) {
                int quantity = commodity.calculateRoutinePrePopulatedQuantity();
                OrderCommodityViewModel orderCommodityViewModel = createOrderCommodityViewModel(quantity, commodity);
                return orderCommodityViewModel;
            }
        }).toList();
    }

    public void generateAllocationAlerts() {
        allocationService.clearCache();
        List<Allocation> availableAllocations = allocationService.getYetToBeReceivedAllocations();
        List<Allocation> allocationsWithAlerts = getAllocationsFromAlerts(getAllocationAlerts());
        for (Allocation allocation : availableAllocations) {
            if (!allocationsWithAlerts.contains(allocation)) {
                AllocationAlert allocationAlert = new AllocationAlert(allocation);
                createAllocationAlert(allocationAlert);
            }
        }
    }

    private void createAllocationAlert(final AllocationAlert allocationAlert) {
        dbUtil.withDao(AllocationAlert.class, new DbUtil.Operation<AllocationAlert, Void>() {
            @Override
            public Void operate(Dao<AllocationAlert, String> dao) throws SQLException {
                dao.create(allocationAlert);
                return null;
            }
        });
    }

    private List<Allocation> getAllocationsFromAlerts(List<AllocationAlert> allocationAlerts) {
        return from(allocationAlerts).transform(new Function<AllocationAlert, Allocation>() {
            @Override
            public Allocation apply(AllocationAlert input) {
                return input.getAllocation();
            }
        }).toList();
    }

    public List<AllocationAlert> getAllocationAlerts() {
        return dbUtil.withDao(AllocationAlert.class, new DbUtil.Operation<AllocationAlert, List<AllocationAlert>>() {
            @Override
            public List<AllocationAlert> operate(Dao<AllocationAlert, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public void deleteAllocationAlert(final Allocation allocation) {
        dbUtil.withDao(AllocationAlert.class, new DbUtil.Operation<AllocationAlert, Void>() {
            @Override
            public Void operate(Dao<AllocationAlert, String> dao) throws SQLException {
                DeleteBuilder<AllocationAlert, String> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq(AllocationAlert.ALLOCATION_ID_COLUMN, allocation.getId());
                deleteBuilder.delete();
                return null;
            }
        });
    }

    public void generateMonthlyStockCountAlerts(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.DAY_OF_MONTH) < getMonthlyStockCountDay()) {
            return;
        }

        if (getMonthlyStockCountAlerts(date).size() == 0) {
            MonthlyStockCountAlert monthlyStockCountAlert = new MonthlyStockCountAlert(date);
            createMonthlyStockCountAlert(monthlyStockCountAlert);
        }
    }

    public int getMonthlyStockCountDay() {
        return sharedPreferences.getInt(CommodityService.MONTHLY_STOCK_COUNT_DAY, 24);
    }

    public List<MonthlyStockCountAlert> getMonthlyStockCountAlerts(final Date date) {
        return dbUtil.withDao(MonthlyStockCountAlert.class, new DbUtil.Operation<MonthlyStockCountAlert, List<MonthlyStockCountAlert>>() {
            @Override
            public List<MonthlyStockCountAlert> operate(Dao<MonthlyStockCountAlert, String> dao) throws SQLException {
                Date firstDay = Helpers.firstDayOfMonth(date);
                Date lastDay = Helpers.lastDayOfMonth(date);
                return dao.queryBuilder().where().between(MonthlyStockCountAlert.DATE_CREATED, firstDay, lastDay).query();
            }
        });
    }


    public void disableAllMonthlyStockCountAlerts() {
        for (final MonthlyStockCountAlert alert : getEnabledMonthlyStockAlerts()) {
            if (adjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert(alert.getDateCreated())) {
                dbUtil.withDao(MonthlyStockCountAlert.class, new DbUtil.Operation<MonthlyStockCountAlert, Integer>() {
                    @Override
                    public Integer operate(Dao<MonthlyStockCountAlert, String> dao) throws SQLException {
                        alert.setDisabled(true);
                        return dao.update(alert);
                    }
                });
            }
        }

    }

    boolean adjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert(final Date date) {
        final Date low = Helpers.firstDayOfMonth(date);
        final Date high = Helpers.lastDayOfMonth(date);
        final List<Commodity> commoditiesFromAdjusments = from(getAdjustmentsBetweenDates(low, high)).transform(new Function<Adjustment, Commodity>() {
            @Override
            public Commodity apply(Adjustment input) {
                return input.getCommodity();
            }
        }).toList();
        List<Commodity> commoditiesWithoutAdjusments = from(commodityService.all()).filter(new Predicate<Commodity>() {
            @Override
            public boolean apply(Commodity input) {
                return !commoditiesFromAdjusments.contains(input);
            }
        }).toList();
        return commoditiesWithoutAdjusments.size() == 0;
    }

    private List<Adjustment> getAdjustmentsBetweenDates(final Date low, final Date high) {
        return dbUtil.withDao(Adjustment.class, new DbUtil.Operation<Adjustment, List<Adjustment>>() {
            @Override
            public List<Adjustment> operate(Dao<Adjustment, String> dao) throws SQLException {
                return dao.queryBuilder().where().between("created", low, high).query();
            }

        });
    }
}
