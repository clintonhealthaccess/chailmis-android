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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataElementType;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionDataSet;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshotValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.sms.SmsSyncService;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.models.CommodityActionDataSet.generateCommodityActionDataSets;
import static org.clintonhealthaccess.lmis.app.models.CommoditySnapshot.toDataValueSet;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getID;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditySnapshotServiceTest extends LMISTestCase {
    public static final SimpleDateFormat PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    @Inject
    CommoditySnapshotService commoditySnapshotService;

//    @Inject
//    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;

    private GenericDao<Category> categoryDao;
    private GenericDao<Commodity> commodityDao;
    private GenericDao<CommodityAction> commodityActivityGenericDao;
    private GenericDao<CommodityActionDataSet> commodityActionDataSetGenericDao;
    private GenericDao<CommoditySnapshot> snapshotDao;
    private GenericDao<DataSet> dataSetGenericDao;

    private SmsSyncService mockSmsSyncService;

    @Before
    public void setUp() {
        Context context = Robolectric.application;
        categoryDao = new GenericDao<>(Category.class, context);
        commodityDao = new GenericDao<>(Commodity.class, context);
        snapshotDao = new GenericDao<>(CommoditySnapshot.class, context);
        commodityActivityGenericDao = new GenericDao<>(CommodityAction.class, context);
        commodityActionDataSetGenericDao = new GenericDao<>(CommodityActionDataSet.class, context);
        dataSetGenericDao = new GenericDao<>(DataSet.class, context);

        mockSmsSyncService = mock(SmsSyncService.class);
        when(mockSmsSyncService.send(any(DataValueSet.class))).thenReturn(true);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SmsSyncService.class).toInstance(mockSmsSyncService);
            }
        });

        generateTestCommodities();
    }

    @Test
    public void shouldCreateNewDailyCommoditySnapshotIfNotExist() throws SQLException {
        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);

        Dispensing dispensing = new Dispensing();
        DispensingItem snapshotable = new DispensingItem(fetchedCommodity1, 3);
        DispensingItem snapshotable1 = new DispensingItem(fetchedCommodity2, 4);
        snapshotable.setDispensing(dispensing);
        snapshotable1.setDispensing(dispensing);
        commoditySnapshotService.add(snapshotable);
        commoditySnapshotService.add(snapshotable1);

        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();

        assertThat(commoditySnapshots.size(), is(2));
        assertThat(commoditySnapshots.get(0).getValue(), is("3"));
    }


    @Test
    public void shouldUpdateDailyCommoditySummaryIfItExists() throws Exception {
        Commodity fetchedCommodity = commodityDao.queryForAll().get(0);

        DispensingItem dispensingItem = new DispensingItem(fetchedCommodity, 3);
        Dispensing dispensing = new Dispensing();
        dispensingItem.setDispensing(dispensing);
        commoditySnapshotService.add(dispensingItem);
        commoditySnapshotService.add(dispensingItem);
        commoditySnapshotService.add(dispensingItem);

        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(1));
        assertThat(commoditySnapshots.get(0).getValue(), is("9"));
    }


    @Test
    public void shouldCreateCommoditySnapshotForReceiving() throws Exception {
        Commodity commodity = commodityDao.queryForAll().get(0);
        ReceiveItem receiveItem = new ReceiveItem(commodity, 10, 10);
        receiveItem.setReceive(new Receive("Facility"));

        commoditySnapshotService.add(receiveItem);

        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(3));
        CommoditySnapshot receivedValueSnapshot = commoditySnapshots.get(0);
        assertThat(receivedValueSnapshot.getValue(), is("10"));
        assertThat(DateUtil.equal(receivedValueSnapshot.getPeriodDate(), receiveItem.getDate()), is(true));

        CommoditySnapshot receiveDateSnapshot = commoditySnapshots.get(1);
        assertThat(receiveDateSnapshot.getValue(), is(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
    }

    @Test
    public void shouldCreateCommoditySnapshotForReceivingWithAllocatedPeriod() throws Exception {
        String allocatedPeriod = "20140827";

        Commodity commodity = commodityDao.queryForAll().get(0);
        Allocation allocation = new Allocation("UG-12345", allocatedPeriod);
        AllocationItem allocationItem = new AllocationItem();
        allocationItem.setAllocation(allocation);
        allocationItem.setCommodity(commodity);
        allocationItem.setQuantity(10);

        ReceiveItem receiveItem = new ReceiveItem(commodity, 10, 10);
        receiveItem.setReceive(new Receive("LGA", allocation));

        commoditySnapshotService.add(receiveItem);

        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(3));

        CommoditySnapshot receivedValueSnapshot = commoditySnapshots.get(0);
        assertThat(receivedValueSnapshot.getValue(), is("10"));
        assertThat(receivedValueSnapshot.toDataValues("orgUnitId").get(0).getPeriod(), is(allocatedPeriod));

        CommoditySnapshot receiveDateSnapshot = commoditySnapshots.get(1);
        assertThat(receiveDateSnapshot.getValue(), is(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        assertThat(receiveDateSnapshot.toDataValues("orgUnitId").get(0).getPeriod(), is(allocatedPeriod));
    }


    @Test
    public void shouldMarkSyncedItemAsUnSyncedWhenAnUpdateOccurs() throws Exception {

        Commodity fetchedCommodity = commodityDao.queryForAll().get(0);
        DispensingItem dispensingItem = new DispensingItem(fetchedCommodity, 3);
        Dispensing dispensing = new Dispensing();
        dispensingItem.setDispensing(dispensing);

        CommoditySnapshot commoditySnapshot = new CommoditySnapshot(fetchedCommodity, dispensingItem.getActivitiesValues().get(0).getCommodityAction(), "3", new Date());
        commoditySnapshot.setSynced(true);
        snapshotDao.create(commoditySnapshot);
        commoditySnapshotService.add(dispensingItem);

        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(1));
        assertThat(commoditySnapshots.get(0).isSynced(), is(false));
    }


    @Test
    public void shouldGetCommoditySnapshotsWithSyncedAsFalse() {

        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);
        DispensingItem dispensingItem = new DispensingItem(fetchedCommodity1, 3);
        Dispensing dispensing = new Dispensing();
        dispensingItem.setDispensing(dispensing);

        CommoditySnapshot commoditySnapshot = new CommoditySnapshot(fetchedCommodity1, dispensingItem.getActivitiesValues().get(0).getCommodityAction(), "3", new Date());
        commoditySnapshot.setSynced(true);
        snapshotDao.create(commoditySnapshot);

        DispensingItem snapshotable = new DispensingItem(fetchedCommodity2, 4);
        snapshotable.setDispensing(dispensing);
        commoditySnapshotService.add(snapshotable);

        List<CommoditySnapshot> unsynchedSnapshots = commoditySnapshotService.getUnSyncedSnapshots();
        assertThat(unsynchedSnapshots.size(), is(1));
    }


    @Test
    public void shouldConvertSnapshotsToDataValueSets() throws Exception {
        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);

        List<CommodityAction> commodityActivities = new ArrayList<>(fetchedCommodity1.getCommodityActionsSaved());
        List<CommodityAction> commodityActivities1 = new ArrayList<>(fetchedCommodity2.getCommodityActionsSaved());

        assertThat(commodityActivities.size(), is(Matchers.greaterThan(0)));
        assertThat(commodityActivities1.size(), is(Matchers.greaterThan(0)));
        CommodityAction commodityAction = commodityActivities.get(0);
        CommoditySnapshot snapshot1 = new CommoditySnapshot(fetchedCommodity1, commodityAction, "3", new Date());
        CommoditySnapshot snapshot2 = new CommoditySnapshot(fetchedCommodity2, commodityActivities1.get(0), "8", new Date());
        List<CommoditySnapshot> snapshots = newArrayList(snapshot1, snapshot2);

        DataValueSet valueSet = toDataValueSet(snapshots, "orgUnit");

        assertThat(valueSet, notNullValue());
        assertThat(valueSet.getDataValues().size(), is(2));
        for (DataValue dataValue : valueSet.getDataValues()) {
            assertThat(dataValue.getDataSet(), notNullValue());
        }

        assertThat(valueSet.getDataValues().get(0).getValue(), is("3"));
        assertThat(valueSet.getDataValues().get(1).getValue(), is("8"));
        assertThat(valueSet.getDataValues().get(0).getDataElement(), is(commodityAction.getId()));
    }


    @Test
    public void shouldMarkSnapshotsAsSyncedIfSyncIsSuccessful() throws Exception {
        setUpSuccessHttpPostRequest(200, "successfulSnapshotPush.json");
        createTwoSnapshotsInSameDataSet();

        List<CommoditySnapshot> unSyncedSnapshots = commoditySnapshotService.getUnSyncedSnapshots();
        assertThat(unSyncedSnapshots.size(), is(2));
        assertThat(unSyncedSnapshots.get(0).getCommodityAction().getCommodityActionDataSets(), notNullValue());

        commoditySnapshotService.syncWithServer(new User("user", "user"));

        assertThat(commoditySnapshotService.getUnSyncedSnapshots().size(), is(0));
    }


    @Test
    public void shouldNotMarkSnapshotsAsSyncedIfSyncFails() throws Exception {

        setUpSuccessHttpPostRequest(200, "failureSnapshotPush.json");
        createTwoSnapshotsInSameDataSet();

        assertThat(commoditySnapshotService.getUnSyncedSnapshots().size(), is(2));

        commoditySnapshotService.syncWithServer(new User("user", "user"));

        assertThat(commoditySnapshotService.getUnSyncedSnapshots().size(), is(2));
    }


    @Test
    public void shouldSyncThroughSms() throws Exception {
        createTwoSnapshotsInSameDataSet();

        assertThat(commoditySnapshotService.getSmsReadySnapshots().size(), is(2));

        commoditySnapshotService.syncWithServerThroughSms(new User("user", "user"));

        verify(mockSmsSyncService, times(1)).send(any(DataValueSet.class));
        assertThat(commoditySnapshotService.getSmsReadySnapshots().size(), is(0));
        assertThat(commoditySnapshotService.getUnSyncedSnapshots().size(), is(2));
    }


    @Test
    public void shouldNotSendSmsIfSnapshotsAreAlreadySentBySms() throws Exception {
        createTwoSnapshotsInSameDataSet();

        assertThat(commoditySnapshotService.getSmsReadySnapshots().size(), is(2));

        commoditySnapshotService.syncWithServerThroughSms(new User("user", "user"));
        commoditySnapshotService.syncWithServerThroughSms(new User("user", "user"));

        verify(mockSmsSyncService, times(1)).send(any(DataValueSet.class));
    }

    private List<CommoditySnapshot> createTwoSnapshotsInSameDataSet() {
        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);

        List<CommodityAction> commodityActivities = newArrayList(fetchedCommodity1.getCommodityActionsSaved());
        List<CommodityAction> commodityActivities1 = newArrayList(fetchedCommodity2.getCommodityActionsSaved());
        CommoditySnapshot snapshot1 = new CommoditySnapshot(fetchedCommodity1, commodityActivities.get(0), "3", new Date());
        CommoditySnapshot snapshot2 = new CommoditySnapshot(fetchedCommodity2, commodityActivities1.get(0), "8", new Date());
        snapshotDao.create(snapshot1);
        snapshotDao.create(snapshot2);

        return newArrayList(snapshot1, snapshot2);
    }


    @Test
    public void shouldSetAttributeAllocationNumberIfAvailable() throws Exception {
//        String testAttributeOptionCombo = "12asdjkla";
//        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
//        Dispensing dispensing = new Dispensing();
//
//        DispensingItem snapshotable = spy(new DispensingItem(fetchedCommodity1, 3));
//        snapshotable.setDispensing(dispensing);
//        doReturn(testAttributeOptionCombo).when(snapshotable).getAttributeOptionCombo();
//        commoditySnapshotService.add(snapshotable);
//        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
//        assertThat(commoditySnapshots.size(), is(1));
//        assertThat(commoditySnapshots.get(0).getAttributeOptionCombo(), is(testAttributeOptionCombo));
    }


    @Test
    public void shouldCreateOrUpdateSnapshotForEachActivityValue() throws Exception {
        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Dispensing dispensing = new Dispensing();
        assertThat(fetchedCommodity1.getCommodityActionsSaved().size(), is(greaterThan(1)));
        CommoditySnapshotValue activityValue = new CommoditySnapshotValue(fetchedCommodity1.getCommodityAction(DataElementType.DISPENSED.getActivity()), 1);
        CommoditySnapshotValue otherActivityValue = new CommoditySnapshotValue(fetchedCommodity1.getCommodityAction(DataElementType.ADJUSTMENTS.getActivity()), 2);
        List<CommoditySnapshotValue> values = new ArrayList<>(Arrays.asList(activityValue, otherActivityValue));
        DispensingItem snapshotable = spy(new DispensingItem(fetchedCommodity1, 3));
        snapshotable.setDispensing(dispensing);
        doReturn(values).when(snapshotable).getActivitiesValues();
        commoditySnapshotService.add(snapshotable);
        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(2));
    }


    @Test
    public void shouldReplaceValueForNonIntegerValues() throws Exception {
        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Dispensing dispensing = new Dispensing();
        String reasonForUnexpectedQuantity = "work";
        CommoditySnapshotValue activityValue = new CommoditySnapshotValue(
                fetchedCommodity1.getCommodityAction(DataElementType.DISPENSED.getActivity()), reasonForUnexpectedQuantity);

        List<CommoditySnapshotValue> values = new ArrayList<>(Arrays.asList(activityValue));
        DispensingItem snapShotable = spy(new DispensingItem(fetchedCommodity1, 3));
        snapShotable.setDispensing(dispensing);
        doReturn(values).when(snapShotable).getActivitiesValues();
        commoditySnapshotService.add(snapShotable);
        List<CommoditySnapshot> commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(1));
        assertThat(commoditySnapshots.get(0).getValue(), is(reasonForUnexpectedQuantity));

        String otherReason = "other reason";
        activityValue = new CommoditySnapshotValue(fetchedCommodity1.getCommodityAction(DataElementType.DISPENSED.getActivity()), otherReason);
        values = new ArrayList<>(Arrays.asList(activityValue));
        doReturn(values).when(snapShotable).getActivitiesValues();
        commoditySnapshotService.add(snapShotable);
        commoditySnapshots = snapshotDao.queryForAll();
        assertThat(commoditySnapshots.size(), is(1));
        assertThat(commoditySnapshots.get(0).getValue(), is(otherReason));
    }


    private void generateTestCommodities() {
        DataSet dataSet = new DataSet("123123");
        dataSet.setPeriodType("Daily");
        dataSetGenericDao.createOrUpdate(dataSet);
        Category category = new Category("commodities");
        categoryDao.create(category);

        generateTestCommodity(dataSet, category, "Panado");
        generateTestCommodity(dataSet, category, "other drug");
    }

    private void generateTestCommodity(DataSet dataSet, Category category, String commodityName) {
        Commodity commodity = new Commodity(commodityName, category);
        commodityDao.create(commodity);

        generateCommodityAction(dataSet, commodity, "DISPENSING", DataElementType.DISPENSED.getActivity());
        generateCommodityAction(dataSet, commodity, "ADJUSTMENTS", DataElementType.ADJUSTMENTS.getActivity());
        generateCommodityAction(dataSet, commodity, "RECEIVED", DataElementType.RECEIVED.getActivity());
        generateCommodityAction(dataSet, commodity, "RECEIVE_DATE", DataElementType.RECEIVE_DATE.getActivity());
        generateCommodityAction(dataSet, commodity, "RECEIVE_SOURCE", DataElementType.RECEIVE_SOURCE.getActivity());
    }

    private void generateCommodityAction(DataSet dataSet, Commodity commodity, String nameTag, String type) {
        CommodityAction activity = new CommodityAction(commodity, getID(), commodity.getName() + " " + nameTag, type);
        activity.addTransientCommodityActionDataSets(generateCommodityActionDataSets(activity, newArrayList(dataSet)));
        commodityActivityGenericDao.create(activity);
        for (CommodityActionDataSet caDataSet : activity.getTransientCommodityActionDataSets()) {
            commodityActionDataSetGenericDao.create(caDataSet);
        }
    }

}
