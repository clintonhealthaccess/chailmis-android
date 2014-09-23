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

import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.activities.OrderActivity.setupOrderCommodityViewModel;

public class AlertsService {

    @Inject
    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;
    public static SimpleDateFormat ALERT_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy");
    private static List<LowStockAlert> lowStockAlerts;

    public List<LowStockAlert> getLowStockAlerts() {
        if (lowStockAlerts == null) {
            List<LowStockAlert> lowStockAlerts = queryAllLowStockAlerts();
            Collections.sort(lowStockAlerts, new Comparator<LowStockAlert>() {
                @Override
                public int compare(LowStockAlert lhs, LowStockAlert rhs) {
                    return new Integer(lhs.getCommodity().getStockOnHand()).compareTo(new Integer(rhs.getCommodity().getStockOnHand()));
                }
            });
            AlertsService.lowStockAlerts = lowStockAlerts;
            return AlertsService.lowStockAlerts;
        }
        return lowStockAlerts;
    }

    private List<LowStockAlert> getEnabledAlerts() {
        return FluentIterable.from(getLowStockAlerts()).filter(new Predicate<LowStockAlert>() {
            @Override
            public boolean apply(LowStockAlert input) {
                return !input.isDisabled();
            }
        }).toList();
    }


    public int numberOfAlerts() {
        return getLowStockAlerts().size();
    }

    public List<LowStockAlert> getTop5LowStockAlerts() {
        List<LowStockAlert> lowStockAlerts = getLowStockAlerts();
        if (lowStockAlerts.size() > 5) {
            return lowStockAlerts.subList(0, 5);
        } else {
            return lowStockAlerts;
        }
    }

    public void updateLowStockAlerts() {
        checkIfExistingAlertsAreStillValid();
        checkForNewLowStockAlerts();
        updateCache();
    }

    private void updateCache() {
        clearCache();
        getLowStockAlerts();
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

    private ImmutableList<Commodity> getCommoditiesInLowStockAlerts() {
        return FluentIterable.from(queryAllLowStockAlerts()).transform(new Function<LowStockAlert, Commodity>() {
            @Override
            public Commodity apply(LowStockAlert input) {
                return input.getCommodity();
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

    private void checkIfExistingAlertsAreStillValid() {
        List<LowStockAlert> availableLowStockAlerts = queryAllLowStockAlerts();
        for (LowStockAlert alert : availableLowStockAlerts) {
            if (!alert.getCommodity().isBelowThreshold()) {
                deleteAlert(alert);
            }
        }
    }

    public void disableAlert(LowStockAlert alert) {
        alert.setDisabled(true);
        alert.setDateDisabled(new Date());
        updateAlert(alert);
    }

    public void deleteAlert(final LowStockAlert alert) {
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

    public void updateAlert(final LowStockAlert lowStockAlert) {
        dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, Object>() {
            @Override
            public Object operate(Dao<LowStockAlert, String> dao) throws SQLException {
                dao.update(lowStockAlert);
                return null;
            }
        });
    }

    public void clearCache() {
        this.lowStockAlerts = null;
    }

    public List<OrderCommodityViewModel> getOrderCommodityViewModelsForLowStockAlert() {
        return FluentIterable.from(getEnabledAlerts()).transform(new Function<LowStockAlert, OrderCommodityViewModel>() {
            @Override
            public OrderCommodityViewModel apply(LowStockAlert input) {
                int quantity = input.getCommodity().calculatePrepopulatedQuantity();
                OrderCommodityViewModel orderCommodityViewModel = setupOrderCommodityViewModel(input.getCommodity());
                orderCommodityViewModel.setQuantityEntered(quantity);
                orderCommodityViewModel.setExpectedOrderQuantity(quantity);
                return orderCommodityViewModel;
            }
        }).toList();
    }
}
