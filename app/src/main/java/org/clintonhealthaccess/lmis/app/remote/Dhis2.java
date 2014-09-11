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

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActivity;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.AttributeValue;
import org.clintonhealthaccess.lmis.app.models.api.CategoryCombo;
import org.clintonhealthaccess.lmis.app.models.api.CategoryComboSearchResponse;
import org.clintonhealthaccess.lmis.app.models.api.CategoryOption;
import org.clintonhealthaccess.lmis.app.models.api.DHISCategory;
import org.clintonhealthaccess.lmis.app.models.api.DHISCategoryOptionCombo;
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
import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.util.Log.e;

public class Dhis2 implements LmisServer {
    public static final String SYNC = "SYNC";
    @Inject
    private Dhis2EndPointFactory dhis2EndPointFactory;

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
                commodity.setCommodityActivities(new ArrayList<CommodityActivity>());
                commodities.add(commodity);
                actualCommodity = commodity;
            }
            if (element.getAttributeValues().size() > 0) {
                AttributeValue attributeValue = element.getAttributeValues().get(0);
                CommodityActivity commodityActivity = new CommodityActivity(actualCommodity, element.getId(), element.getName(), attributeValue.getValue());
                if (element.getDataSets() != null && element.getDataSets().size() > 0) {
                    commodityActivity.setDataSet(element.getDataSets().get(0));
                }
                actualCommodity.getCommodityActivities().add(commodityActivity);
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
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        List<OrderType> types = new ArrayList<>();
        CategoryComboSearchResponse response = service.searchCategoryCombos("order", "name,id,categories[id,name,categoryOptions],categoryOptionCombos");
        List<CategoryCombo> categoryCombos = response.getCategoryCombos();
        if (Helpers.collectionIsNotEmpty(categoryCombos)) {
            CategoryCombo combo = categoryCombos.get(0);
            List<DHISCategory> categories = combo.getCategories();
            getTypesFromCategories(types, combo, categories);
        }
        return types;
    }

    private void getTypesFromCategories(List<OrderType> types, CategoryCombo combo, List<DHISCategory> categories) {
        if (Helpers.collectionIsNotEmpty(categories)) {
            for (CategoryOption option : categories.get(0).getCategoryOptions()) {
                String name = option.getName();
                getAndSetIdFromCategoryOptionCombos(types, combo, name);
            }
        }
    }

    private void getAndSetIdFromCategoryOptionCombos(List<OrderType> types, CategoryCombo combo, String name) {
        for (DHISCategoryOptionCombo comboOption : combo.getCategoryOptionCombos()) {
            if (comboOption.getName().equalsIgnoreCase("(" + name + ")")) {
                types.add(new OrderType(comboOption.getId(), name));
            }
        }
    }

    @Override
    public Map<Commodity, Integer> fetchStockLevels(List<Commodity> commodities, User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        Calendar calendar = Calendar.getInstance();
        DataValueSet valueSet = new DataValueSet();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            valueSet = service.fetchDataValues(getDataSetId(commodities), user.getFacilityCode(), getStartDate(calendar, simpleDateFormat), getEndDate(calendar, simpleDateFormat));
        } catch (LmisException exception) {
            e(SYNC, "error syncing stock levels");
        }
        return fetchStockLevelsForCommodities(commodities, valueSet.getDataValues());
    }

    private String getDataSetId(List<Commodity> commodities) {
        return commodities.get(0).getCommodityActivity(CommodityActivity.currentStock).getDataSet().getId();
    }

    private String getStartDate(Calendar calendar, SimpleDateFormat simpleDateFormat) {
        calendar.add(Calendar.MONTH, -6);
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

    public DataValue findMostRecentDataValueForActivity(List<DataValue> dataValues, String abc) {
        DataValue mostRecentDataValue = null;
        for (DataValue dataValue : dataValues) {
            if (dataValue.getDataElement().equalsIgnoreCase(abc) && (mostRecentDataValue == null || mostRecentDataValue.getPeriod() < dataValue.getPeriod())) {
                mostRecentDataValue = dataValue;
            }
        }
        return mostRecentDataValue;
    }

    public Map<Commodity, Integer> fetchStockLevelsForCommodities(List<Commodity> commodities, List<DataValue> values) {
        Map<Commodity, Integer> result = new HashMap<>();

        for (Commodity commodity : commodities) {
            CommodityActivity stockLevelActivity = commodity.getCommodityActivity(CommodityActivity.currentStock);
            if (stockLevelActivity != null) {
                DataValue mostRecentDataValueForActivity = findMostRecentDataValueForActivity(values, stockLevelActivity.getId());
                if (mostRecentDataValueForActivity != null) {
                    result.put(commodity, Integer.parseInt(mostRecentDataValueForActivity.getValue()));
                }
            }
        }
        return result;
    }
}
