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

import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
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

import static org.clintonhealthaccess.lmis.app.activities.OrderActivity.setupOrderCommodityViewModel;

public class AlertsService {

    public static final String DISABLED = "disabled";
    @Inject
    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;

    @Inject
    SharedPreferences sharedPreferences;

    public static SimpleDateFormat ALERT_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy");
    private static List<LowStockAlert> lowStockAlerts;

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
        return FluentIterable.from(getLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
            @Override
            public boolean apply(LowStockAlert input) {
                return !input.isDisabled();
            }
        }).toList();
    }


    public int numberOfAlerts() {
        return getEnabledLowStockAlerts().size() + getNumberOfRoutineOrderAlerts();
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

        AlertsService.lowStockAlerts = null;
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
        return FluentIterable.from(queryAllLowStockAlerts()).transform(new Function<LowStockAlert, Commodity>() {
            @Override
            public Commodity apply(LowStockAlert input) {
                return input.getCommodity();
            }
        }).toList();
    }

    public ImmutableList<LowStockAlert> getLowStockAlertsForCommodities(final List<Commodity> commodities) {
        return FluentIterable
                .from(queryAllLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
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

    public List<OrderCommodityViewModel> getOrderCommodityViewModelsForLowStockAlert(final String orderTypeName) {
        return FluentIterable.from(getEnabledLowStockAlerts()).transform(new Function<LowStockAlert, OrderCommodityViewModel>() {
            @Override
            public OrderCommodityViewModel apply(LowStockAlert lowStockAlert) {
                int quantity = 0;
                if (orderTypeName.equalsIgnoreCase(OrderType.EMERGENCY)) {
                    quantity = lowStockAlert.getCommodity().calculateEmergencyPrepopulatedQuantity();
                } else if (orderTypeName.equalsIgnoreCase(OrderType.ROUTINE)) {
                    quantity = lowStockAlert.getCommodity().calculateRoutinePrePopulatedQuantityl();
                }
                OrderCommodityViewModel orderCommodityViewModel = createOrderCommodityViewModel(lowStockAlert, quantity);
                return orderCommodityViewModel;
            }
        }).toList();
    }

    private OrderCommodityViewModel createOrderCommodityViewModel(LowStockAlert input, int quantity) {
        OrderCommodityViewModel orderCommodityViewModel = setupOrderCommodityViewModel(input.getCommodity());
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
        messages.add(getLatestRoutineOrderAlerts());
        return messages;
    }

    public List<? extends NotificationMessage> getNotificationMessages() {
        return getAllRoutineOrderAlerts();
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

    private List<RoutineOrderAlert> getAllRoutineOrderAlerts() {
        return dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, List<RoutineOrderAlert>>() {
            @Override
            public List<RoutineOrderAlert> operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
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
                return queryBuilder.orderBy(RoutineOrderAlert.DATE_CREATED, true).queryForFirst();
            }
        });
    }
}
