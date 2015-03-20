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
import com.thoughtworks.dhis.models.DataElementType;
import com.thoughtworks.dhis.models.Indicator;
import com.thoughtworks.dhis.models.IndicatorGroup;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionDataSet;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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

    @Ignore
    @Test
    public void testWriteOutNewJson() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        dhis2.writeJson(new User());
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


    @Ignore@Test
    public void testShouldFetchReasonsForOrder() throws Exception {
        setUpSuccessHttpGetRequest(200, "systemSettingForReasonsForOrder.json");
        List<String> reasons = dhis2.fetchOrderReasons(new User("test", "pass"));
        assertThat(reasons.size(), is(3));
        assertThat(reasons, contains(OrderReason.HIGH_DEMAND, OrderReason.LOSSES, OrderReason.EXPIRIES));
    }

    @Ignore@Test
    public void testShouldFetchOrderTypes() throws Exception {
        setUpSuccessHttpGetRequest(200, "orderTypes.json");
        List<OrderType> orderTypes = dhis2.fetchOrderTypes(new User("test", "pass"));
        assertThat(orderTypes.size(), is(2));
    }

    @Ignore@Test
    public void shouldFetchCategoriesFromAPIServiceEndPoint() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataElementGroupSets.json");
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        List<Category> categories = dhis2.fetchCategories(new User());
        String commodityName = "Cotrimoxazole_suspension";
        assertThat(categories.size(), is(7));

        Category category = categories.get(0);
        assertThat(category.getTransientCommodities().size(), is(greaterThan(1)));
        assertThat(category.getName(), is("Essential Medicines"));

        Commodity commodity = category.getTransientCommodities().get(0);
        assertThat(commodity.getName(), is(commodityName));
        assertThat(commodity.getCommodityActions().size(), is(10));

        CommodityAction commodityAction = commodity.getCommodityActions().get(0);
        assertThat(commodityAction.getCommodity(), is(commodity));
        assertThat(commodityAction.getName(), is("Cotrimoxazole_suspension_ALLOCATED"));
        assertThat(commodityAction.getActivityType(), is("ALLOCATED"));
        assertThat(commodityAction.getTransientCommodityActionDataSets().size(), is(1));

        CommodityActionDataSet commodityActionDataSet = commodityAction.getTransientCommodityActionDataSets().get(0);
        assertThat(commodityActionDataSet.getCommodityAction(), is(commodityAction));
        assertThat(commodityActionDataSet.getDataSet().getName(), is("LMIS Commodities Allocated"));
    }

    @Test
    public void shouldFetchDataSetsFromAPIServiceEndPoint() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        List<DataSet> dataSets = dhis2.fetchDataSets(new User());
        assertThat(dataSets.size(), is(3));
        DataSet dataSet = dataSets.get(0);
        assertThat(dataSet.getName(), is("LMIS Commodities Allocated"));
    }

    @Test
    public void shouldFetchCommoditiesFromAPIServiceEndPoint() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataElementGroupSets.json");
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        List<Category> categories = dhis2.fetchCategories(new User());
        String commodityName = "Cotrimoxazole_suspension";
        assertThat(categories.size(), is(7));
        Category category = categories.get(0);
        assertThat(category.getTransientCommodities().size(), is(greaterThan(1)));
        assertThat(category.getName(), is("Essential Medicines"));
        assertThat(category.getTransientCommodities().get(0).getName(), is(commodityName));
    }

    @Test
    public void shouldFetchNonLGAInformation() throws Exception {
        setUpSuccessHttpGetRequest(200, "dataElementGroupSets.json");
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        List<Category> categories = dhis2.fetchCategories(new User());
        assertThat(categories.size(), is(7));
        Category category = categories.get(0);
        assertThat(category.getTransientCommodities().get(0).isNonLGA(), is(false));
        category = categories.get(6);
        assertThat(category.getTransientCommodities().get(0).isNonLGA(), is(true));
    }

    @Test
    public void shouldCreateCommodityActionValueForEachDataValue() throws Exception {
        String orgUnit = "orgnunit";
        User user = new User();
        user.setFacilityCode(orgUnit);

        setUpSuccessHttpGetRequest(200, "dataElementGroupSets.json");
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        setUpSuccessHttpGetRequest(200, "dataValues.json");

        commodityService.saveToDatabase(dhis2.fetchCategories(user));
        categoryService.clearCache();
        List<CommodityActionValue> result = dhis2.fetchCommodityActionValues(user);
        assertThat(result.size(), is(210));
        assertThat(result.get(0).getValue(), is("469"));
        assertThat(result.get(0).getPeriod(), is("20131229"));
        assertThat(result.get(0).getCommodityAction().getId(), is("f5edb97ceca"));
    }

    @Test
    public void shouldSearchConstantsForMonthlyStockCountDay() throws Exception {
        setUpSuccessHttpGetRequest(200, "constants.json");
        Integer day = dhis2.fetchIntegerConstant(new User(), "");
        assertThat(day, is(20));
    }

    @Test
    public void shouldFallBackToDefaultIfNoContantsAreAvailable() throws Exception {
        setUpSuccessHttpGetRequest(200, "constantsEmpty.json");
        Integer day = dhis2.fetchIntegerConstant(new User(), "");
        assertThat(day, is(24));
    }

    @Test
    public void shouldFetchAllAllocationsInLastTwoMonths() throws Exception {
        String orgUnit = "orgnunit";
        User user = new User();
        user.setFacilityCode(orgUnit);

        setUpSuccessHttpGetRequest(200, "dataElementGroupSets.json");
        setUpSuccessHttpGetRequest(200, "dataSets.json");
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");

        commodityService.saveToDatabase(dhis2.fetchCategories(user));
        categoryService.clearCache();
        setUpSuccessHttpGetRequest(200, "allocations.json");
        List<CommodityActionValue> allocationActionValues = dhis2.fetchAllocations(user);
        assertThat(allocationActionValues.size(), is(7));
        for (CommodityActionValue allocationActionValue : allocationActionValues) {
            assertThat(allocationActionValue.getCommodityAction().getActivityType(), is(DataElementType.ALLOCATED.getActivity()));
        }
    }

    @Test
    public void shouldFetchIndicatorGroups() throws Exception {
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        List<IndicatorGroup> indicatorGroups = dhis2.fetchIndicatorGroups(new User());
        assertThat(indicatorGroups.size(), is(4));

        IndicatorGroup indicatorGroup = indicatorGroups.get(0);
        assertThat(indicatorGroup.getName(), is("BUFFER STOCK"));
        assertThat(indicatorGroup.getId(), is("VYnBpONirHy"));

        Indicator indicator = indicatorGroup.getIndicators().get(0);
        assertThat(indicator.getName(), is("Cotrimoxazole_suspension  BUFFER_STOCK"));
        assertThat(indicator.getId(), is("bufferStock1"));

         indicatorGroup = indicatorGroups.get(2);
        assertThat(indicatorGroup.getName(), is("MIN_STOCK_QUANTITY"));
        assertThat(indicatorGroup.getId(), is("v2t0cGTiWBZ"));

         indicator = indicatorGroup.getIndicators().get(0);
        assertThat(indicator.getId(), is("minStockQuantity1"));
        assertThat(indicator.getName(), is("Cotrimoxazole_suspension  MIN_STOCK_QUANTITY"));
    }

    @Test
    public void shouldFetchClientIndicators() throws Exception {
        setUpSuccessHttpGetRequest(200, "indicatorGroups.json");
        List<Indicator> indicators = dhis2.fetchClientIndicators(new User());
        assertThat(indicators.size(), is(66));
    }

}