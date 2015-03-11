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
import android.util.Log;
import android.util.TimingLogger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.thoughtworks.dhis.models.Attribute;
import com.thoughtworks.dhis.models.AttributeValue;
import com.thoughtworks.dhis.models.DataElement;
import com.thoughtworks.dhis.models.DataElementGroup;
import com.thoughtworks.dhis.models.DataElementGroupSet;
import com.thoughtworks.dhis.models.DataElementType;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;
import com.thoughtworks.dhis.models.Option;
import com.thoughtworks.dhis.models.OptionSet;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionDataSet;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.ConstantSearchResponse;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSetPushResponse;
import org.clintonhealthaccess.lmis.app.models.api.OptionSetResponse;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2EndPointFactory;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;
import org.clintonhealthaccess.lmis.app.remote.responses.DataElementGroupSetSearchResponse;
import org.clintonhealthaccess.lmis.app.remote.responses.DataSetSearchResponse;
import org.clintonhealthaccess.lmis.app.services.CommodityActionService;
import org.clintonhealthaccess.lmis.app.services.DataSetService;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.InjectResource;

import static android.util.Log.e;
import static android.util.Log.i;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.models.CommodityActionDataSet.generateCommodityActionDataSets;
import static org.clintonhealthaccess.lmis.app.utils.Helpers.isEmpty;

public class Dhis2 implements LmisServer {
    public static final String SYNC = "SYNC";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    @Inject
    private Dhis2EndPointFactory dhis2EndPointFactory;

    @InjectResource(R.integer.monthly_stock_count_day)
    Integer defaultValue;

    @Inject
    Context context;

    @Inject
    DataSetService dataSetService;

    @Inject
    CommodityActionService commodityActionService;

    @Override
    public UserProfile validateLogin(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.validateLogin();
    }

    @Override
    public List<Category> fetchCommodities(User user) {
        TimingLogger timingLogger = new TimingLogger("TIMER", "fetchCommodities");
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataSetSearchResponse response = service.searchDataSets("LMIS", "id,name,periodType,description,dataElements[name,id,attributeValues[value,attribute[id,name]],dataElementGroups[id,name,dataElementGroupSet[id,name],attributeValues[value,attribute[id,name]]");
        timingLogger.addSplit("fetch data");
        timingLogger.dumpToLog();
        return getCategoriesFromDataSets(response.getDataSets());
    }

    public void writeJson(User user) throws JSONException {
        TimingLogger timingLogger = new TimingLogger("TIMER", "fetchCommodities");
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataSetSearchResponse response = service.searchDataSets("LMIS", "id,name,periodType,description,dataElements[name,id,attributeValues[value,attribute[id,name]],dataElementGroups[id,name,dataElementGroupSet[id,name],attributeValues[value,attribute[id,name]]");
        timingLogger.addSplit("fetch data");
        timingLogger.dumpToLog();
        List<DataElementGroupSet> dataElementGroupSets = getGroupSets(response.getDataSets());

        String s = "";
        for (DataElementGroupSet t : dataElementGroupSets) {
            if (!s.isEmpty()) {
                s += ",";
            }
            s += "{" + t.jsonString() + "}";
        }

        System.out.println("{ \"dataElementGroupSets\": [ " + s + " ] }");


    }

    private List<DataElementGroupSet> getGroupSets(List<DataSet> dataSets) {
        List<DataElementGroupSet> groupSets = new ArrayList<>();
        for (DataSet ds : dataSets) {
            for (DataElement de : ds.getDataElements()) {
                for (DataElementGroup group : de.getDataElementGroups()) {
                    if (!groupSets.contains(group.getDataElementGroupSet())) {
                        DataElementGroupSet set = group.getDataElementGroupSet();
                        set.setDataElementGroups(getGroups(dataSets, set));
                        groupSets.add(set);
                    }
                }
            }
        }
        return groupSets;
    }

    private List<DataElementGroup> getGroups(List<DataSet> dataSets, DataElementGroupSet groupSet) {
        List<DataElementGroup> groups = new ArrayList<>();
        for (DataSet ds : dataSets) {
            for (DataElement de : ds.getDataElements()) {
                for (DataElementGroup group : de.getDataElementGroups()) {
                    if (!groups.contains(group) && group.getDataElementGroupSet().equals(groupSet)) {
                        group.setDataElements(getElements(dataSets, group));
                        groups.add(group);
                    }
                }
            }
        }
        return groups;
    }

    private List<DataElement> getElements(List<DataSet> dataSets, DataElementGroup group) {
        List<DataElement> dataElements = new ArrayList<>();
        for (DataSet ds : dataSets) {
            for (DataElement de : ds.getDataElements()) {
                if (de.getDataElementGroups() != null && de.getDataElementGroups().size() > 0) {
                    if (!dataElements.contains(de)
                            && de.getDataElementGroups().get(0).equals(group)) {
                        dataElements.add(de);
                    }
                }
            }
        }
        return dataElements;
    }

    @Override
    public List<Category> fetchCategories(User user) {
        TimingLogger timingLogger = new TimingLogger("TIMER", "fetchCommodities");
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataElementGroupSetSearchResponse response = service.getDataElementGroupSets("id,name, dataElementGroups[id,name, dataElements[name,id,attributeValues[value,attribute[id,name]]]");
        timingLogger.addSplit("fetch data");
        timingLogger.dumpToLog();
        List<DataSet> dataSets = fetchDataSets(user);
        List<DataElementGroupSet> androidDataElementGroupSets = getAndroidDataElementGroupSets(response.getDataElementGroupSets());
        return getCategoriesFromDataElementGroupSets(androidDataElementGroupSets, dataSets);
    }

    public List<DataSet> fetchDataSets(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataSetSearchResponse datasets = service.searchDataSets("LMIS", "id,name,periodType,description,dataElements[name,id,attributeValues[value,attribute[id,name]],dataElementGroups[id,name,dataElementGroupSet[id,name],attributeValues[value,attribute[id,name]]");
        return datasets.getDataSets();
    }

    private List<DataElementGroupSet> getAndroidDataElementGroupSets(List<DataElementGroupSet> sets) {

        List<DataElementGroupSet> androidGroupSets = new ArrayList<>();

        try {
            List<String> categoryNames = Arrays.asList("Essential Medicines",
                    "EM-Child Health", "EM-Maternal Health", "EM-Neonatal Health",
                    "Malaria", "Family Planning", "Vaccines");
            for (DataElementGroupSet set : sets) {
                if (categoryNames.contains(set.getName().trim())) {
                    androidGroupSets.add(set);
                }
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e("Error", e.getMessage());
        }

        return androidGroupSets;
    }

    private List<Category> getCategoriesFromDataElementGroupSets(List<DataElementGroupSet> dataElementGroupSets, List<DataSet> dataSets) {

        List<Category> categories = newArrayList();

        for (DataElementGroupSet groupSet : dataElementGroupSets) {
            Category category = new Category(groupSet.getId(), groupSet.getName());

            for (DataElementGroup group : groupSet.getDataElementGroups()) {
                Commodity commodity = new Commodity(group.getId(), group.getName(), category, false, false, false);

                if (group.getAttributeValues() != null && group.getAttributeValues().size() > 0) {
                    for (AttributeValue value : group.getAttributeValues()) {

                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_NON_LGA)) {
                            commodity.setNonLGA(value.getValue().equalsIgnoreCase("1"));
                        }
                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_DEVICE)) {
                            commodity.setIsDevice(value.getValue().equalsIgnoreCase("1"));
                        }
                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_VACCINE)) {
                            commodity.setIsVaccine(value.getValue().equalsIgnoreCase("1"));
                        }
                    }
                }

                for (DataElement element : group.getDataElements()) {
                    if (element.getAttributeValues().size() > 0) {
                        AttributeValue attributeValue = element.getAttributeValues().get(0);

                        if (!DataElementType.activityExists(attributeValue.getValue())) {
                            Log.e("Missing Activity Name Error ", attributeValue.getValue());
                            continue;
                        }

                        CommodityAction commodityAction = new CommodityAction(commodity,
                                element.getId(), element.getName(), attributeValue.getValue());

                        List<DataSet> elementDataSets = getElementDataSets(dataSets, element.getId());
                        if (elementDataSets != null) {
                            commodityAction.addTransientCommodityActionDataSets(
                                    generateCommodityActionDataSets(commodityAction, elementDataSets));
                            commodity.getCommodityActions().add(commodityAction);
                        } else {
                            e("Error Null Dataset", commodity.getName() + " " + commodityAction.getName());
                        }
                    }
                }

                category.addTransientCommodity(commodity);
            }

            categories.add(category);
        }

        Log.e("CHECK", "Categories are " + categories.size() + " " + categories);
        for (Category category : categories) {
            Log.e("CHECK", category.getName() + " has " + category.getTransientCommodities().size() + " not saved commodity actions");

            for (Commodity commodity : category.getTransientCommodities()) {
                Log.e("CHECK", commodity.getName() + " has " + commodity.getCommodityActions().size() + " not saved commodity actions");

                for (CommodityAction commodityAction : commodity.getCommodityActions()) {
                    Log.e("CHECK", commodityAction.getName() + " has " + commodityAction.getTransientCommodityActionDataSets().size() + " not saved commodityActionDataSets");

                    for (CommodityActionDataSet caDataSet : commodityAction.getTransientCommodityActionDataSets()) {
                        if (!dataSets.contains(caDataSet.getDataSet())) {
                            Log.e("CHECK", "Oooooooooooooops we are here");
                        }
                    }
                }
            }
        }
        return categories;
    }

    private List<DataSet> getElementDataSets(List<DataSet> dataSets, String elementId) {
        List<DataSet> elementDataSets = new ArrayList<>();
        for (DataSet dataSet : dataSets) {
            for (DataElement dataElement : dataSet.getDataElements()) {
                if (dataElement.getId().equalsIgnoreCase(elementId)) {
                    elementDataSets.add(dataSet);
                }
            }
        }
        return elementDataSets;
    }

    private List<Category> getCategoriesFromDataSets(List<DataSet> dataSets) {
        List<Category> categories = newArrayList();
        List<Commodity> commodities = newArrayList();
        List<DataElement> elements = newArrayList();

        for (DataSet dataSet : dataSets) {
            i(SYNC, String.format("DataSet: %s", dataSet.getName()));
            if (dataSet.getDataElements() != null) {
                for (DataElement elm : dataSet.getDataElements()) {
                    elm.setDataSets(newArrayList(dataSet.toRawDataSet()));
                    elements.add(elm);
                }
            }
        }

        for (DataElement element : elements) {
            i(SYNC, String.format("DatElement: %s", element.getName()));
            getOrCreateCommodity(element, commodities, categories);

        }

        return categories;
    }

    private void getOrCreateCommodity(DataElement element, List<Commodity> commodities, List<Category> categories) {
        Commodity commodity = new Commodity();
        Commodity actualCommodity;
        if (element.getDataElementGroups().size() > 0) {
            if (element.getDataElementGroups().size() > 1) {
                System.out.println("Offending guy caught " + element.getName() + " in "
                        + element.getDataElementGroups().size() + " groups: " + element.getDataElementGroups());
            }
            DataElementGroup dataElementGroup = element.getDataElementGroups().get(0);
            DataElementGroupSet dataElementGroupSet = dataElementGroup.getDataElementGroupSet();
            if (dataElementGroup.getAttributeValues() != null) {
                if (dataElementGroup.getAttributeValues().size() > 0) {
                    for (AttributeValue value : dataElementGroup.getAttributeValues()) {
                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_NON_LGA)) {
                            commodity.setNonLGA(value.getValue().equalsIgnoreCase("1"));
                        }

                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_DEVICE)) {
                            commodity.setIsDevice(value.getValue().equalsIgnoreCase("1"));
                        }

                        if (value.getAttribute().getName().equalsIgnoreCase(Attribute.LMIS_VACCINE)) {
                            commodity.setIsVaccine(value.getValue().equalsIgnoreCase("1"));
                        }
                    }
                } else {
                    commodity.setNonLGA(false);
                    commodity.setIsDevice(false);
                    commodity.setIsVaccine(false);
                }
            }
            commodity.setId(dataElementGroup.getId());

            if (commodities.contains(commodity)) {
                actualCommodity = commodities.get(commodities.indexOf(commodity));
            } else {
                commodity.setName(dataElementGroup.getName());
                Category category = new Category();
                if (dataElementGroupSet != null) {
                    category.setName(dataElementGroupSet.getName());
                    category.setLmisId(dataElementGroupSet.getId());
                    if (categories.contains(category)) {
                        List<Commodity> updatedCommodities = categories.get(categories.indexOf(category)).getTransientCommodities();
                        updatedCommodities.add(commodity);
                        category.setCommodities(updatedCommodities);
                        int location = categories.indexOf(category);
                        categories.set(location, category);
                    } else {
                        category.setCommodities(newArrayList(commodity));
                        categories.add(category);
                    }
                }
                commodities.add(commodity);
                actualCommodity = commodity;
            }
            if (element.getAttributeValues().size() > 0) {
                AttributeValue attributeValue = element.getAttributeValues().get(0);
                CommodityAction commodityAction = new CommodityAction(actualCommodity,
                        element.getId(), element.getName(), attributeValue.getValue());
                //commodityAction.addTransientDataSets(convertDataSetsToLmisDataSets(element.getDataSets()));

                actualCommodity.getCommodityActions().add(commodityAction);
            }
        }

    }

    private List<DataSet> convertDataSetsToLmisDataSets(List<com.thoughtworks.dhis.models.DataSet> dataSets) {
        List<DataSet> lmisDataSets = new ArrayList<>();
        for (com.thoughtworks.dhis.models.DataSet d : dataSets) {
            lmisDataSets.add(new DataSet(d));
        }
        return lmisDataSets;
    }

    @Override
    public List<String> fetchOrderReasons(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        OptionSetResponse optionSetResponse = service.searchOptionSets("order", "name,id,options");
        List<String> optionSets = new ArrayList<>();
        List<OptionSet> optionSetList = optionSetResponse.getOptionSets();
        if (!isEmpty(optionSetList)) {

            optionSets = from(optionSetList.get(0).getOptions()).transform(new Function<Option, String>() {
                @Override
                public String apply(Option input) {
                    return input.getName();
                }
            }).toList();
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
    public List<CommodityActionValue> fetchCommodityActionValues(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataValueSet valueSet = new DataValueSet();
        try {
            String dataSet2 = getDataSetId(DataSet.CALCULATED);
            String dataSetId = getDataSetId(DataSet.DEFAULT);
            valueSet = service.fetchDataValuesEx(dataSetId, user.getFacilityCode(),
                    threeMonthsAgo(), today(), dataSet2);
        } catch (LmisException exception) {
            e(SYNC, "error syncing stock levels");
        }
        return convertDataValuesToCommodityActions(valueSet.getDataValues());
    }

    @Override
    public List<CommodityActionValue> fetchAllocations(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataValueSet valueSet = new DataValueSet();
        try {
            String dataSetId = getDataSetId(DataSet.ALLOCATED);
            valueSet = service.fetchDataValues(dataSetId, user.getFacilityCode(),
                    threeMonthsAgo(), today());
        } catch (LmisException exception) {
            e(SYNC, "error syncing allocations");
        }

        List<CommodityActionValue> commodityActionValues = convertDataValuesToCommodityActions(valueSet.getDataValues());
        return from(commodityActionValues).filter(new Predicate<CommodityActionValue>() {
            @Override
            public boolean apply(CommodityActionValue input) {
                return input.getCommodityAction().getActivityType().equals(DataElementType.ALLOCATED.getActivity());
            }
        }).toList();
    }

    private String getDataSetId(String dataSetName) {
        List<DataSet> dataSets = dataSetService.all();
        for (DataSet dataSet : dataSets) {
            if (dataSet.getName().contains(dataSetName)) {
                return dataSet.getId();
            }
        }
        return "";
    }

    private String threeMonthsAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        return SIMPLE_DATE_FORMAT.format(calendar.getTime());
    }

    private String today() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return SIMPLE_DATE_FORMAT.format(calendar.getTime());
    }

    @Override
    public DataValueSetPushResponse pushDataValueSet(DataValueSet valueSet, User user) {
        i("pushing DataValueSet", valueSet.toString());
        for (DataValue dataValue : valueSet.getDataValues()) {
            i("DataValue", dataValue.getDataElement() + " : " + dataValue.getValue());
            i("more info", "org unit: " + dataValue.getOrgUnit() + ", " + "period: " + dataValue.getPeriod());
        }
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.pushDataValueSet(valueSet);
    }

    @Override
    public Integer fetchIntegerConstant(User user, String constantKey) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        ConstantSearchResponse response = service.searchConstants(constantKey, "id,name,value");
        if (!response.getConstants().isEmpty()) {
            return response.getConstants().get(0).getValue().intValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public String fetchPhoneNumberConstant(User user, String constantKey, String defaultValue) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        ConstantSearchResponse response = service.searchConstants(constantKey, "id,name,value");
        if (!response.getConstants().isEmpty()) {
            return "+" + String.valueOf(response.getConstants().get(0).getValue().longValue());
        } else {
            return defaultValue;
        }
    }

    public List<CommodityActionValue> convertDataValuesToCommodityActions(List<DataValue> values) {
        List<String> ids = from(values).transform(new Function<DataValue, String>() {
            @Override
            public String apply(DataValue input) {
                return input.getDataElement();
            }
        }).toList();
        List<CommodityAction> actions = commodityActionService.getAllById(ids);
        final Map<String, CommodityAction> actionMap = new HashMap<>();
        for (CommodityAction action : actions) {
            actionMap.put(action.getId(), action);
        }
        ImmutableList<CommodityActionValue> commodityActionValues = from(values).transform(new Function<DataValue, CommodityActionValue>() {
            @Override
            public CommodityActionValue apply(DataValue input) {

                CommodityAction commodityAction = actionMap.get(input.getDataElement());

                if (commodityAction == null) {
                    commodityAction = new CommodityAction(null, input.getDataElement(), DataElementType.ALLOCATION_ID.getActivity(), DataElementType.ALLOCATED.getActivity());
                }
                return new CommodityActionValue(commodityAction, input.getValue(), input.getPeriod());
            }
        }).toList();

        return commodityActionValues;

    }

}
