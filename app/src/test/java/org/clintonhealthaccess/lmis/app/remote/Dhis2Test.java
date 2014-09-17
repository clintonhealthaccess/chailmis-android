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

package org.clintonhealthaccess.lmis.app.remote;

import com.google.inject.Inject;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.getSentHttpRequest;

@RunWith(RobolectricGradleTestRunner.class)
public class Dhis2Test extends LMISTestCase {

    @Inject
    private Dhis2 dhis2;

    @Inject
    private CommodityService commodityService;

    @Inject
    private CategoryService categoryService;

    @Before
    public void setUp() throws Exception {
        setUpInjection(this);
    }


    @Test
    public void testShouldValidateUserLogin() throws Exception {
        setUpSuccessHttpGetRequest(200, "userResponse.json");

        User user = new User("test", "pass");
        dhis2.validateLogin(user);

        HttpRequest lastSentHttpRequest = getSentHttpRequest(0);
        assertThat(lastSentHttpRequest.getRequestLine().getUri(), equalTo(dhis2BaseUrl + "/api/me"));
        Header authorizationHeader = lastSentHttpRequest.getFirstHeader("Authorization");
        assertThat(authorizationHeader.getValue(), equalTo("Basic dGVzdDpwYXNz"));
    }

    @Test
    public void testShouldFetchReasonsForOrder() throws Exception {
        setUpSuccessHttpGetRequest(200, "systemSettingForReasonsForOrder.json");
        List<String> reasons = dhis2.fetchOrderReasons(new User("test", "pass"));
        assertThat(reasons.size(), is(3));
        assertThat(reasons, contains(OrderReason.HIGH_DEMAND, OrderReason.LOSSES, OrderReason.EXPIRIES));
    }


    @Test
    public void testShouldFetchOrderTypes() throws Exception {
        setUpSuccessHttpGetRequest(200, "orderTypes.json");
        List<OrderType> orderTypes = dhis2.fetchOrderTypes(new User("test", "pass"));
        assertThat(orderTypes.size(), is(2));
//        assertThat(orderTypes, contains(OrderType.ROUTINE, OrderType.EMERGENCY));
    }

    @Test
    public void shouldFetchCommoditiesFromAPIServiceEndPoint() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        List<Category> categories = dhis2.fetchCommodities(new User());
        String commodityName = "Cotrimoxazole_suspension";
        assertThat(categories.size(), is(10));
        Category category = categories.get(0);
        assertThat(category.getNotSavedCommodities().size(), is(greaterThan(1)));
        assertThat(category.getName(), is("Antibiotics"));
        assertThat(category.getNotSavedCommodities().get(0).getName(), is(commodityName));
    }

    @Test
    public void shouldGetLatestDataValueFromResultForEachDataElement() throws Exception {
        String orgUnit = "orgnunit";
        String DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String end = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.MONTH, -6);
        String start = dateFormat.format(calendar.getTime());
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "dataValues.json");
        User user = new User();
        user.setFacilityCode(orgUnit);
        commodityService.saveToDatabase(dhis2.fetchCommodities(user));
        categoryService.clearCache();
        List<Commodity> commodities = commodityService.all();
        assertThat(commodities.size(), greaterThan(0));
        List<CommodityActionValue> result = dhis2.fetchCommodityActionValues(commodities, user);
        String commodityName = "Cotrimoxazole_suspension";
        String commodityId = "877d0e9f022";
        Commodity commodity = new Commodity(commodityId, commodityName);
        assertThat(result.size(), is(210));

        //FIXME invomplete test, check the Commodity
                //get(commodity), is(271));
    }

    @Test
    public void shouldGetMostRecentDataValueForGivenActivity() throws Exception {
        List<DataValue> dataValues = new ArrayList<>();
        dataValues.add(DataValue.builder().value("10").period("20131225").dataElement("abc").build());
        dataValues.add(DataValue.builder().value("11").period("201312").dataElement("abc").build());
        dataValues.add(DataValue.builder().value("13").period("20131226").dataElement("abc").build());
        dataValues.add(DataValue.builder().value("14").period("20131227").dataElement("abc4").build());
        dataValues.add(DataValue.builder().value("14").period("20141227").dataElement("abc3").build());
        assertThat(dhis2.findMostRecentDataValueForActivity(dataValues, "abc").getValue(), is("13"));
    }

    @Ignore("James")
    @Test
    public void shouldGetStockLevelsForCommoditiesFromDataValues() throws Exception {
        List<DataValue> dataValues = new ArrayList<>();
        String commodityActivityId = "abc";
        dataValues.add(DataValue.builder().value("10").period("20131225").dataElement(commodityActivityId).build());
        dataValues.add(DataValue.builder().value("11").period("201312").dataElement(commodityActivityId).build());
        dataValues.add(DataValue.builder().value("13").period("20131226").dataElement(commodityActivityId).build());
        dataValues.add(DataValue.builder().value("14").period("20131227").dataElement("abc4").build());
        dataValues.add(DataValue.builder().value("14").period("20141227").dataElement("abc3").build());


        List<Commodity> commodities = new ArrayList<>();
        Commodity commodity = new Commodity("commodity");
        CommodityAction activity = new CommodityAction(commodity, commodityActivityId, "commodity_receive_stock", CommodityAction.stockOnHand);
        ArrayList<CommodityAction> commodityActivities = new ArrayList<>();
        commodityActivities.add(activity);
        commodity.setCommodityActivitiesSaved(commodityActivities);
        commodities.add(commodity);
        List<CommodityActionValue> result = dhis2.convertDataValuesToCommodityActions(dataValues);
        assertThat(result.size(), is(5));
        assertThat(result.get(2).getValue(), is("13"));

    }

    @Test
    public void shouldSearchConstantsForMonthlyStockCountDay() throws Exception {
        setUpSuccessHttpGetRequest(200, "constants.json");
        Integer day = dhis2.getDayForMonthlyStockCount(new User());
        assertThat(day, is(20));
    }

    @Test
    public void shouldFallBackToDefaultIfNoContantsAreAvailable() throws Exception {
        setUpSuccessHttpGetRequest(200, "constantsEmpty.json");
        Integer day = dhis2.getDayForMonthlyStockCount(new User());
        assertThat(day, is(24));
    }
}