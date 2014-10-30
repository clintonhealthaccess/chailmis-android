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
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.alerts.AllocationAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.MonthlyStockCountAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.RoutineOrderAlert;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.ArrayList;
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
    AdjustmentService adjusmentService;

    @Inject
    private CommodityService commodityService;

    @Inject
    DbUtil dbUtil;

    @Inject
    SharedPreferences sharedPreferences;


    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);

    }

    @Test
    public void shouldCreateLowStockAlertsForItemsWithStockBelowTheThreshold() throws Exception {
        setupCommodities();
        alertsService.updateLowStockAlerts();
        List<LowStockAlert> lowStockAlerts = alertsService.getLowStockAlerts();
        assertThat(lowStockAlerts.size(), is(2));
    }


    @Test
    public void shouldCreateLowStockAlert() throws Exception {
        setupCommodities();
        Commodity commodity = getCommodity();
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

    private Commodity getCommodity() {
        return commodityService.all().get(0);
    }

    @Test
    public void shouldUpdatesStockAlert() throws Exception {
        setupCommodities();
        Commodity commodity = getCommodity();
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
        setupCommodities();
        alertsService.updateLowStockAlerts();
        List<OrderCommodityViewModel> commodityViewModels = alertsService.getOrderCommodityViewModelsForLowStockAlert();
        assertThat(commodityViewModels.size(), is(2));
        assertThat(commodityViewModels.get(0).getExpectedOrderQuantity(), is(30));
    }

    private void setupCommodities() {
        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void shouldGetCommodityViewModelsForEachCommodityForRoutineOrderAlert() throws Exception {
        setupCommodities();
        alertsService.updateLowStockAlerts();
        List<OrderCommodityViewModel> commodityViewModels = alertsService.getOrderViewModelsForRoutineOrderAlert();
        int numberOfCommodities = 7;
        assertThat(commodityViewModels.size(), is(numberOfCommodities));
        assertThat(commodityViewModels.get(0).getExpectedOrderQuantity(), is(25));
    }

    @Test
    public void shouldFilterLowStockAlertsForGivenCommodities() throws Exception {
        setupCommodities();
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
        setupCommodities();
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
        setupCommodities();
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

        setPreferenceIntegerValue(25, CommodityService.ROUTINE_ORDER_ALERT_DAY);
        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(0));
    }

    @Test
    public void shouldGenerateRoutineOrderAlertWhenDateIsRoutineOrderDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 25);

        setPreferenceIntegerValue(25, CommodityService.ROUTINE_ORDER_ALERT_DAY);
        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(1));
    }

    @Test
    public void shouldGenerateRoutineOrderAlertWhenDateIsPastRoutineOrderAlertDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.ROUTINE_ORDER_ALERT_DAY);

        alertsService.generateRoutineOrderAlert(calendar.getTime());
        List<RoutineOrderAlert> routineOrderAlerts = alertsService.getRoutineOrderAlertsInCurrentMonth(calendar.getTime());
        assertThat(routineOrderAlerts.size(), is(1));
    }

    @Test
    public void shouldNotReGenerateRoutineOrderAlertOnceGenerated() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 25);

        setPreferenceIntegerValue(25, CommodityService.ROUTINE_ORDER_ALERT_DAY);
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


    @Test
    public void shouldGetRoutineOrderAlertDayFromPreferences() throws Exception {
        Integer day = 12;
        setPreferenceIntegerValue(day, CommodityService.ROUTINE_ORDER_ALERT_DAY);
        assertThat(alertsService.getRoutineOrderAlertDay(), is(day));
    }

    @Test
    public void shouldGetStockCountDayFromPreferences() throws Exception {
        Integer day = 23;
        setPreferenceIntegerValue(day, CommodityService.MONTHLY_STOCK_COUNT_DAY);
        assertThat(alertsService.getMonthlyStockCountDay(), is(day));
    }

    @Test
    public void shouldDisableAllRoutineOrderAlerts() throws Exception {
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        createRoutineOrderAlert(new RoutineOrderAlert(new Date()));
        assertThat(alertsService.getNotificationMessages().size(), is(2));
        alertsService.disableAllRoutineOrderAlerts();
        assertThat(alertsService.getNotificationMessages().size(), is(0));

    }

    @Test
    public void shouldGenerateAllocationAlertsForAllocationsThatHaveNoAlertsAndHaveNotYetBeenReceived() throws Exception {
        Allocation allocation = new Allocation("job", "now");
        allocation.setReceived(true);
        Allocation allocation1 = new Allocation("james", "then");
        createAllocation(allocation);
        createAllocation(allocation1);
        alertsService.generateAllocationAlerts();
        List<AllocationAlert> allocationAlerts = alertsService.getAllocationAlerts();
        assertThat(allocationAlerts.size(), is(1));
    }

    @Test
    public void shouldGetAllAllocationAlerts() throws Exception {
        assertThat(alertsService.getNotificationMessages().size(), is(0));
        Allocation allocation = new Allocation("james", "then");
        createAllocation(allocation);
        createAllocationAlert(new AllocationAlert(allocation));
        assertThat(alertsService.getAllocationAlerts().size(), is(1));
    }

    @Test
    public void shouldDeleteAllocationAlertGivenTheAllocation() throws Exception {
        Allocation allocation = new Allocation("james", "then");
        createAllocation(allocation);
        createAllocationAlert(new AllocationAlert(allocation));
        assertThat(alertsService.getAllocationAlerts().size(), is(1));
        alertsService.deleteAllocationAlert(allocation);
        assertThat(alertsService.getAllocationAlerts().size(), is(0));
    }

    @Test
    public void shouldGenerateMonthlyStockCountAlertForMonthIfNotYetAvailable() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.MONTHLY_STOCK_COUNT_DAY);

        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());

        assertThat(alertsService.getMonthlyStockCountAlerts(calendar.getTime()).size(), is(1));

        calendar.set(2025, Calendar.JUNE, 20);

        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());

        assertThat(alertsService.getMonthlyStockCountAlerts(calendar.getTime()).size(), is(0));
    }

    @Test
    public void shouldNotRegenerateMonthlyStockAlertForIfAlreadyGenerated() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.MONTHLY_STOCK_COUNT_DAY);

        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());
        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());
        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());

        List<MonthlyStockCountAlert> monthlyStockCountAlerts = alertsService.getMonthlyStockCountAlerts(calendar.getTime());

        assertThat(monthlyStockCountAlerts.size(), is(1));
    }

    @Test
    public void shouldIncludeMonthlyStockCountAlertsInGetNotificationMessages() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.MONTHLY_STOCK_COUNT_DAY);

        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());

        assertThat(alertsService.getNotificationMessages().size(), is(1));


    }

    @Test
    public void shouldIncludeMonthlyStockCountAlertsInNotificationCount() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.MONTHLY_STOCK_COUNT_DAY);

        int numberOfAlertsBefore = alertsService.numberOfAlerts();
        alertsService.generateMonthlyStockCountAlerts(calendar.getTime());
        alertsService.disableAllRoutineOrderAlerts();
        assertThat(alertsService.numberOfAlerts(), is(numberOfAlertsBefore + 1));

    }

    @Test
    public void shouldDisableAllMonthlyStockCountAlerts() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);

        setPreferenceIntegerValue(25, CommodityService.MONTHLY_STOCK_COUNT_DAY);

        Date date = calendar.getTime();
        alertsService.generateMonthlyStockCountAlerts(date);

        List<MonthlyStockCountAlert> enabledMonthlyStockAlerts = alertsService.getEnabledMonthlyStockAlerts();
        assertThat(enabledMonthlyStockAlerts.size(), is(1));
        createAdjusmentsForEachCommodity(date);
        assertThat(alertsService.adjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert(date), is(true));
        alertsService.disableAllMonthlyStockCountAlerts();
        assertThat(alertsService.getEnabledMonthlyStockAlerts().size(), is(0));
    }

    @Test
    public void shouldPrepopulateOnlyLGACommodities() throws Exception {
        setupCommodities();
        alertsService.updateLowStockAlerts();
        List<OrderCommodityViewModel> commodityViewModels = alertsService.getOrderViewModelsForRoutineOrderAlert();
        int numberOfCommodities = 7;
        assertThat(commodityViewModels.size(), is(numberOfCommodities));
        assertThat(commodityViewModels.get(0).getExpectedOrderQuantity(), is(25));
    }


    @Test
    public void shouldCheckIfAdjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert() throws Exception {
        setupCommodities();
        Adjustment adjustment = new Adjustment(commodityService.all().get(0), 10, true, AdjustmentReason.PHYSICAL_COUNT.getName());

        saveAdjusment(adjustment);

        assertThat(alertsService.adjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert(new Date()), is(false));
        createAdjusmentsForEachCommodity(new Date());
        assertThat(alertsService.adjustmentsHaveBeenMadeForEachCommodityInMonthOfAlert(new Date()), is(true));


    }

    private void createAdjusmentsForEachCommodity(Date date) {
        final List<Adjustment> adjustments = new ArrayList<>();
        for (Commodity commodity : commodityService.all()) {
            final Adjustment adjustment_new = new Adjustment(commodity, 10, true, AdjustmentReason.PHYSICAL_COUNT.getName());
            adjustment_new.setCreated(date);
            adjustments.add(adjustment_new);
        }

        createAdjusments(adjustments);
    }

    private void createAdjusments(final List<Adjustment> adjustments) {
        dbUtil.withDaoAsBatch(Adjustment.class, new DbUtil.Operation<Adjustment, Object>() {
            @Override
            public Object operate(Dao<Adjustment, String> dao) throws SQLException {
                for (Adjustment adj : adjustments) {
                    dao.create(adj);
                }
                return null;
            }
        });
    }

    private void saveAdjusment(final Adjustment adjustment) {
        dbUtil.withDao(Adjustment.class, new DbUtil.Operation<Adjustment, Object>() {
            @Override
            public Object operate(Dao<Adjustment, String> dao) throws SQLException {
                dao.create(adjustment);
                return null;
            }
        });
    }

    private void createRoutineOrderAlert(final RoutineOrderAlert data) {
        dbUtil.withDao(RoutineOrderAlert.class, new DbUtil.Operation<RoutineOrderAlert, Object>() {
            @Override
            public Object operate(Dao<RoutineOrderAlert, String> dao) throws SQLException {
                return dao.create(data);
            }
        });
    }

    private void setPreferenceIntegerValue(Integer value, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void createAllocation(final Allocation data) {
        dbUtil.withDao(Allocation.class, new DbUtil.Operation<Allocation, Object>() {
            @Override
            public Object operate(Dao<Allocation, String> dao) throws SQLException {
                return dao.create(data);
            }
        });
    }


    private void createAllocationAlert(final AllocationAlert data) {
        dbUtil.withDao(AllocationAlert.class, new DbUtil.Operation<AllocationAlert, Object>() {
            @Override
            public Object operate(Dao<AllocationAlert, String> dao) throws SQLException {
                return dao.create(data);
            }
        });
    }

}