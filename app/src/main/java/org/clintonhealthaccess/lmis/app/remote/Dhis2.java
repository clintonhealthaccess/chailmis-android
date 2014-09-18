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

import android.content.Context;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.AttributeValue;
import org.clintonhealthaccess.lmis.app.models.api.ConstantSearchResponse;
import org.clintonhealthaccess.lmis.app.models.api.DataElement;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroup;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroupSet;
import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSetPushResponse;
import org.clintonhealthaccess.lmis.app.models.api.OptionSet;
import org.clintonhealthaccess.lmis.app.models.api.OptionSetResponse;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2EndPointFactory;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;
import org.clintonhealthaccess.lmis.app.remote.responses.DataSetSearchResponse;
import org.clintonhealthaccess.lmis.app.services.CommodityActionService;
import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.inject.InjectResource;

import static android.util.Log.e;

public class Dhis2 implements LmisServer {
    public static final String SYNC = "SYNC";
    @Inject
    private Dhis2EndPointFactory dhis2EndPointFactory;

    @InjectResource(R.string.monthly_stock_count_search_key)
    private String monthlyStockCountSearchKey;

    @InjectResource(R.integer.monthly_stock_count_day)
    Integer monthlyStockCountDay;

    @Inject
    Context context;

    @Inject
    CommodityActionService commodityActionService;

    @Override
    public UserProfile validateLogin(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.validateLogin();
    }

    @Override
    public List<Category> fetchCommodities(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataSetSearchResponse response = service.searchDataSets("LMIS", "id,name,periodType,description,dataElements[name,id,attributeValues[value,attribute[id,name]],dataElementGroups[id,name,dataElementGroupSet[id,name]");
        return getCategoriesFromDataSets(response.getDataSets());
    }

    private List<Category> getCategoriesFromDataSets(List<DataSet> dataSets) {
        List<Category> categories = new ArrayList<>();
        List<Commodity> commodities = new ArrayList<>();
        List<DataElement> elements = new ArrayList<>();

        for (DataSet dataSet : dataSets) {
            e(SYNC, String.format("DataSet: %s", dataSet.getName()));
            if (dataSet != null && dataSet.getDataElements() != null) {
                for (DataElement elm : dataSet.getDataElements()) {
                    elm.setDataSets(new ArrayList<DataSet>(Arrays.asList(dataSet)));
                    elements.add(elm);
                }
            }
        }

        for (DataElement element : elements) {
            e(SYNC, String.format("DatElement: %s", element.getName()));
            getOrCreateCommodity(element, commodities, categories);

        }

        return categories;
    }

    private void getOrCreateCommodity(DataElement element, List<Commodity> commodities, List<Category> categories) {
        Commodity commodity = new Commodity();
        Commodity actualCommodity;
        if (element.getDataElementGroups().size() > 0) {
            DataElementGroup dataElementGroup = element.getDataElementGroups().get(0);
            DataElementGroupSet dataElementGroupSet = dataElementGroup.getDataElementGroupSet();
            commodity.setId(dataElementGroup.getId());

            if (commodities.contains(commodity)) {
                actualCommodity = commodities.get(commodities.indexOf(commodity));
            } else {
                commodity.setName(dataElementGroup.getName());
                Category category = new Category();
                category.setName(dataElementGroupSet.getName());
                category.setLmisId(dataElementGroupSet.getId());
                if (categories.contains(category)) {
                    List<Commodity> updatedCommodities = categories.get(categories.indexOf(category)).getNotSavedCommodities();
                    updatedCommodities.add(commodity);
                    category.setCommodities(updatedCommodities);
                    int location = categories.indexOf(category);
                    categories.set(location, category);
                } else {
                    category.setCommodities(new ArrayList<Commodity>(Arrays.asList(commodity)));
                    categories.add(category);
                }
                commodity.setCommodityActions(new ArrayList<CommodityAction>());
                commodities.add(commodity);
                actualCommodity = commodity;
            }
            if (element.getAttributeValues().size() > 0) {
                AttributeValue attributeValue = element.getAttributeValues().get(0);
                CommodityAction commodityAction = new CommodityAction(actualCommodity, element.getId(), element.getName(), attributeValue.getValue());
                if (element.getDataSets() != null && element.getDataSets().size() > 0) {
                    commodityAction.setDataSet(element.getDataSets().get(0));
                }
                actualCommodity.getCommodityActions().add(commodityAction);
            }
        }


    }

    @Override
    public List<String> fetchOrderReasons(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        OptionSetResponse optionSetResponse = service.searchOptionSets("order", "name,id,options");
        List<String> optionSets = new ArrayList<>();
        List<OptionSet> optionSetList = optionSetResponse.getOptionSets();
        if (Helpers.collectionIsNotEmpty(optionSetList)) {
            optionSets = optionSetList.get(0).getOptions();
        }
        return optionSets;
    }

    @Override
    public List<OrderType> fetchOrderTypes(User user) {
        List<OrderType> types = new ArrayList<>();
        types.add(new OrderType(OrderType.ROUTINE));
        types.add(new OrderType(OrderType.EMERGENCY));
        return types;
    }


    @Override
    public List<CommodityActionValue> fetchCommodityActionValues(List<Commodity> commodities, User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        Calendar calendar = Calendar.getInstance();
        DataValueSet valueSet = new DataValueSet();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dataSet2 = getDataSetId(commodities, CommodityAction.AMC);
            String dataSetId = getDataSetId(commodities, CommodityAction.stockOnHand);
            valueSet = service.fetchDataValues(dataSetId, user.getFacilityCode(), getStartDate(calendar, simpleDateFormat), getEndDate(calendar, simpleDateFormat), dataSet2);
        } catch (LmisException exception) {
            e(SYNC, "error syncing stock levels");
        }
        return convertDataValuesToCommodityActions(valueSet.getDataValues());
    }

    private String getDataSetId(List<Commodity> commodities, String activityType) {
        CommodityAction commodityAction = commodities.get(0).getCommodityAction(activityType);
        if (commodityAction != null) {
            return commodityAction.getDataSet().getId();
        } else {
            return "";
        }
    }

    private String getStartDate(Calendar calendar, SimpleDateFormat simpleDateFormat) {
        calendar.add(Calendar.MONTH, -2);
        return simpleDateFormat.format(calendar.getTime());
    }

    private String getEndDate(Calendar calendar, SimpleDateFormat simpleDateFormat) {
        calendar.setTime(new Date());
        return simpleDateFormat.format(calendar.getTime());
    }

    @Override
    public DataValueSetPushResponse pushDataValueSet(DataValueSet valueSet, User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.pushDataValueSet(valueSet);
    }

    @Override
    public Integer getDayForMonthlyStockCount(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        ConstantSearchResponse response = service.searchConstants(monthlyStockCountSearchKey, "id,name,value");
        if (!response.getConstants().isEmpty()) {
            return response.getConstants().get(0).getValue().intValue();
        } else {
            return monthlyStockCountDay;
        }
    }


    public List<CommodityActionValue> convertDataValuesToCommodityActions(List<DataValue> values) {
        return FluentIterable
                .from(values).transform(new Function<DataValue, CommodityActionValue>() {
                    @Override
                    public CommodityActionValue apply(DataValue input) {
                        CommodityAction commodityAction = commodityActionService.getById(input.getDataElement());
                        return new CommodityActionValue(commodityAction, input.getValue(), input.getPeriod());
                    }
                }).toList();
    }
}
