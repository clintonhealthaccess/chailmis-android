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

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.RoutineOrderAlert;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class AlertsServiceTest {
    @Inject
    AlertsService alertsService;

    @Inject
    private CommodityService commodityService;

    @Inject
    DbUtil dbUtil;

    @Inject
    SharedPreferences sharedPreferences;


    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void shouldCreateLowStockAlertsForItemsWithStockBelowTheThreshold() throws Exception {
        alertsService.updateLowStockAlerts();
        List<LowStockAlert> lowStockAlerts = alertsService.getLowStockAlerts();
        assertThat(lowStockAlerts.size(), is(2));
    }


    @Test
    public void shouldCreateLowStockAlert() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        LowStockAlert alert = new LowStockAlert(commodity);
        alertsService.createAlert(alert);
        Long count = dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, Long>() {
            @Override
            public Long operate(Dao<LowStockAlert, String> dao) throws SQLException {
                return dao.countOf();
            }
        });
        assertThat(count, is(1L));

    }

    @Test
    public void shouldUpdatesStockAlert() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        LowStockAlert alert = new LowStockAlert(commodity);
        alertsService.createAlert(alert);
        alert.setDisabled(true);
        alertsService.updateLowStockAlert(alert);
        LowStockAlert fetchedAlert = dbUtil.withDao(LowStockAlert.class, new DbUtil.Operation<LowStockAlert, LowStockAlert>() {
            @Override
            public LowStockAlert operate(Dao<LowStockAlert, String> dao) throws SQLException {
                return dao.queryForAll().get(0);
            }
        });
        assertThat(fetchedAlert.isDisabled(), is(true));
    }

    @Test
    public void shouldGetCommodityViewModelsForEachLowStockAlert() throws Exception {
        alertsService.updateLowStockAlerts();
        List<OrderCommodityViewModel> commodityViewModels = alertsService.getOrderCommodityViewModelsForLowStockAlert();
        assertThat(commodityViewModels.size(), is(2));
        assertThat(commodityViewModels.get(0).getExpectedOrderQuantity(), is(30));
    }

    @Test
    public void shouldFilterLowStockAlertsForGivenCommodities() throws Exception {
        alertsService.updateLowStockAlerts();
        List<LowStockAlert> alerts = alertsService.getLowStockAlerts();
        assertThat(alerts.size(), is(2));
        Commodity commodity = alerts.get(0).getCommodity();
        List<Commodity> commodities = Arrays.asList(commodity);

        List<LowStockAlert> lowStockAlerts = alertsService.getLowStockAlertsForCommodities(commodities);
        assertThat(lowStockAlerts.size(), is(1));
        assertThat(lowStockAlerts.get(0).getCommodity(), is(commodity));
    }

    @Test
    public void shouldDisableAlertsForCommodities() throws Exception {
        alertsService.updateLowStockAlerts();
        List<LowStockAlert> alerts = alertsService.getLowStockAlerts();
        Commodity commodity = alerts.get(0).getCommodity();
        List<Commodity> commodities = Arrays.asList(commodity);

        alertsService.disableAlertsForCommodities(commodities);
        List<LowStockAlert> lowStockAlerts = alertsService.getLowStockAlertsForCommodities(commodities);
        assertThat(lowStockAlerts.size(), is(1));
        assertThat(lowStockAlerts.get(0).isDisabled(), is(true));
    }

    @Test
    public void shouldReturnEnabledAlerts() throws Exception {
        alertsService.updateLowStockAlerts();
        List<LowStockAlert> alerts = alertsService.getLowStockAlerts();
        assertThat(alerts.size(), is(2));

        alertsService.disableLowStockAlert(alerts.get(0));
        alerts = alertsService.getEnabledLowStockAlerts();
        assertThat(alerts.size(), is(1));
    }

    @Test
    public void shouldNotGenerateRoutineOrderAlert() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 24);

        setRoutineOrderDay(25);
        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(0));
    }

    @Test
    public void shouldGenerateRoutineOrderAlertWhenDateIsRoutineOrderDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 25);

        setRoutineOrderDay(25);
        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(1));
    }

    @Test
    public void shouldGenerateRoutineOrderAlertWhenDateIsPastRoutineOrderAlertDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 27);

        setRoutineOrderDay(25);
        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(1));
    }

    @Test
    public void shouldNotReGenerateRoutineOrderAlertOnceGenerated() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 25);

        setRoutineOrderDay(25);
        alertsService.generateRoutineOrderAlert(calendar.getTime());

        calendar.set(2025, Calendar.JULY, 27);
        alertsService.generateRoutineOrderAlert(calendar.getTime());

        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(1));
    }

    @Test
    public void shouldReturnAllExistingAlerts() throws Exception {
        assertThat(alertsService.getNotificationMessages().size(), is(0));
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        assertThat(alertsService.getNotificationMessages().size(), is(2));
    }

    @Test
    public void shouldOnlyReturnTheLatestRoutineOrderAlertOnTheHomePage() throws Exception {

        assertThat(alertsService.getNotificationMessages().size(), is(0));
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        assertThat(alertsService.getNotificationMessagesForHomePage().size(), is(1));
    }

    private void createRoutineOrderAlert(final RoutineOrderAlert data) {
        dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, Object>() {
            @Override
            public Object operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                return dao.create(data);
            }
        });
    }

    @Test
    public void shouldGetRoutineOrderAlertDayFromPreferences() throws Exception {
        Integer day = 12;
        setRoutineOrderDay(day);
        assertThat(alertsService.getRoutineOrderAlertDay(), is(12));
    }

    private void setRoutineOrderDay(Integer day) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CommodityService.ROUTINE_ORDER_ALERT_DAY, day);
        editor.commit();
    }
}