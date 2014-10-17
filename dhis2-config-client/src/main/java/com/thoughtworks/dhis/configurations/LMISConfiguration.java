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

package com.thoughtworks.dhis.configurations;

import com.thoughtworks.dhis.models.Attribute;
import com.thoughtworks.dhis.models.AttributeValue;
import com.thoughtworks.dhis.models.CategoryCombo;
import com.thoughtworks.dhis.models.CategoryOptionCombo;
import com.thoughtworks.dhis.models.Constant;
import com.thoughtworks.dhis.models.DataElement;
import com.thoughtworks.dhis.models.DataElementGroup;
import com.thoughtworks.dhis.models.DataElementGroupSet;
import com.thoughtworks.dhis.models.DataElementType;
import com.thoughtworks.dhis.models.DataSet;
import com.thoughtworks.dhis.models.ExcelCategory;
import com.thoughtworks.dhis.models.ExcelCommodity;
import com.thoughtworks.dhis.models.Indicator;
import com.thoughtworks.dhis.models.IndicatorType;
import com.thoughtworks.dhis.models.OptionSet;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class LMISConfiguration implements IConfiguration {


    private static final String DAILY = "Daily";
    private static final String MONTHLY = "Monthly";
    public static final String DATA_ELEMENTS = "dataElements";
    public static final String DATA_ELEMENT_GROUPS = "dataElementGroups";
    public static final String DATA_ELEMENT_GROUP_SETS = "dataElementGroupSets";
    public static final String DATA_SETS = "dataSets";
    private static final String AGGREGATE = "AGGREGATE";
    private static final String LMIS_KEY_WORD = "LMIS ";
    private static final String LMIS_COMMODITIES_DEFAULT = LMIS_KEY_WORD + "Commodities Default";
    private static final String LMIS_COMMODITIES_ALLOCATED = LMIS_KEY_WORD + "Commodities Allocated";
    private static final String LMIS_ACTIVITY = LMIS_KEY_WORD + "Activity";
    public static final String OPTION_SETS = "optionSets";
    public static final String INDICATOR_TYPES = "indicatorTypes";
    public static final String INDICATORS = "indicators";

    private static final String LMIS_COMMODITIES_LOSSES = LMIS_KEY_WORD + "Commodities Losses";
    private static final String LOSSES_INDICATOR_TYPE = "Losses Indicator Type";
    private static final String LMIS_COMMODITIES_CALCULATED = LMIS_KEY_WORD + "Commodities Calculated";
    public static final String ATTRIBUTES = "attributes";
    public static final String CONSTANTS = "constants";
    private final Attribute lgaAttribute;


    private CategoryCombo defaultCategoryCombo;
    private List<DataElementGroup> dataElementGroups;
    private List<DataElementGroupSet> dataElementGroupSets;
    private List<DataElement> dataElements;
    private List<DataSet> dataSets;
    private List<Indicator> indicators;
    private List<OptionSet> optionSets;
    private List<Constant> constants;
    private List<String> shortNames;
    private OptionSet orderReasonOptionSet;
    private IndicatorType indicatorType;
    private Attribute actionAttribute;

    public LMISConfiguration(CategoryCombo defaultCategoryCombo) {
        this.defaultCategoryCombo = defaultCategoryCombo;
        actionAttribute = createAttribute("string", LMISConfiguration.LMIS_ACTIVITY, true, false);
        lgaAttribute = createAttribute("string", Attribute.LMIS_NON_LGA, false, true);
        dataElementGroups = new ArrayList<DataElementGroup>();
        dataElementGroupSets = new ArrayList<DataElementGroupSet>();
        dataElements = new ArrayList<DataElement>();
        dataSets = new ArrayList<DataSet>();
        indicators = new ArrayList<Indicator>();
        optionSets = new ArrayList<OptionSet>();
        constants = new ArrayList<Constant>();

        orderReasonOptionSet = createOptionSet("Reasons For Order", "HIGH DEMAND", "LOSSES", "EXPIRIES");
        optionSets.add(orderReasonOptionSet);

        indicatorType = IndicatorType.builder().id(generateID(LOSSES_INDICATOR_TYPE)).name(LOSSES_INDICATOR_TYPE).factor(1).number(true).build();
        shortNames = new ArrayList<String>();
    }

    private OptionSet createOptionSet(String optionSetName, String... optionSetValues) {
        return OptionSet.builder().id(generateID(optionSetName)).name(optionSetName).options(newArrayList(optionSetValues)).build();
    }

    @Override
    public Map<String, Object> generateMetaData() throws IOException {
        return buildConfigurationFromFile();
    }

    private HashMap<String, Object> buildConfigurationFromFile() throws IOException {
        List<ExcelCategory> categories = getCategories(new BufferedReader(new FileReader("commodities.csv")));

        setUpRoutineOrder(categories);

        setupEmergencyOrder(categories);

        setupDefaultDataElements(categories);

        setupConstants();
        return buildMetaData();
    }

    private void setupConstants() {
        setupConstant("Monthly Stock Count Day", 24d);
        setupConstant("Delivery Lead Time", 0.5);
        setupConstant("SMS_GATEWAY_NUMBER", 256785111222d);
    }

    private void setupConstant(String name, double value) {
        Constant constant = Constant.builder().id(generateID(name)).name(name).displayName(name).value(value).build();
        constants.add(constant);
    }

    private HashMap<String, Object> buildMetaData() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put(DATA_SETS, dataSets);
        data.put(CONSTANTS, constants);
        data.put(OPTION_SETS, optionSets);
        data.put(INDICATORS, indicators);
        data.put(INDICATOR_TYPES, Arrays.asList(indicatorType));
        data.put(DATA_ELEMENTS, dataElements);
        data.put(DATA_ELEMENT_GROUPS, dataElementGroups);
        data.put(DATA_ELEMENT_GROUP_SETS, dataElementGroupSets);
        data.put(ATTRIBUTES, Arrays.asList(actionAttribute,lgaAttribute));

        return data;
    }

    private void setupDefaultDataElements(List<ExcelCategory> categories) {
        List<DataElementType> activitiesDaily = new ArrayList<DataElementType>(Arrays.asList(DataElementType.DISPENSED, DataElementType.STOCK_ON_HAND, DataElementType.ADJUSTMENTS, DataElementType.ADJUSTMENT_REASON));
        List<DataElementType> activitiesLosses = new ArrayList<DataElementType>(Arrays.asList(DataElementType.EXPIRED, DataElementType.WASTED, DataElementType.MISSING));
        List<DataElementType> activitiesLossesForVaccine = new ArrayList<DataElementType>(Arrays.asList(
                DataElementType.EXPIRED, DataElementType.VVM_CHANGE, DataElementType.BREAKAGE, DataElementType.FROZEN,
                DataElementType.LABEL_REMOVED, DataElementType.OTHERS));

        List<DataElementType> activitiesAllocated = new ArrayList<DataElementType>(Arrays.asList(DataElementType.RECEIVED, DataElementType.ALLOCATED, DataElementType.RECEIVE_DATE, DataElementType.RECEIVE_SOURCE));
        List<DataElementType> activitiesCalculated = new ArrayList<DataElementType>(Arrays.asList(DataElementType.MAXIMUM_THRESHOLD, DataElementType.MINIMUM_THRESHOLD, DataElementType.AMC,
                DataElementType.TMC, DataElementType.BUFFER_STOCK, DataElementType.SAFETY_STOCK, DataElementType.NUMBER_OF_STOCK_OUT_DAYS, DataElementType.MONTHS_OF_STOCK_ON_HAND, DataElementType.PROJECTED_ORDER_AMOUNT, DataElementType.MAXIMUM_STOCK_LEVEL, DataElementType.MINIMUM_STOCK_LEVEL));

        DataSet main = createDataSet(LMIS_COMMODITIES_DEFAULT, DAILY);
        DataSet losses = createDataSet(LMIS_COMMODITIES_LOSSES, DAILY);
        DataSet allocated = createDataSet(LMIS_COMMODITIES_ALLOCATED, MONTHLY);
        DataSet calculated = createDataSet(LMIS_COMMODITIES_CALCULATED, MONTHLY);


        Map<DataSet, List<DataElementType>> dataSetMapping = new HashMap<DataSet, List<DataElementType>>();
        dataSetMapping.put(main, activitiesDaily);
        dataSetMapping.put(losses, activitiesLosses);
        dataSetMapping.put(allocated, activitiesAllocated);
        dataSetMapping.put(calculated, activitiesCalculated);

        for (DataSet dataSet : dataSetMapping.keySet()) {
            for (ExcelCategory category : categories) {
                List<DataElementType> dataElementTypes = dataSetMapping.get(dataSet);

                if (category.getName().contains("Vaccines") && dataSet.equals(losses)) {
                    dataElementTypes = activitiesLossesForVaccine;
                }

                for (ExcelCommodity commodity : category.getCommodityList()) {
                    List<DataElement> elementsInIndicator = new ArrayList<DataElement>();
                    for (DataElementType type : dataElementTypes) {
                        String element_name = commodity.getName() + " " + type.getActivity();
                        DataElement dataElement = createDataElement(actionAttribute, element_name, type);
                        DataElementGroup group = getOrCreateDataElementGroup(commodity.getName(), dataElementGroups, commodity.isNonLGA());
                        group.getDataElements().add(dataElement);
                        dataSet.getDataElements().add(dataElement);
                        dataElements.add(dataElement);

                        if (dataSet.equals(losses)) {
                            elementsInIndicator.add(dataElement);
                        }
                    }
                    if (elementsInIndicator.size() > 0) {
                        String numerator = "";
                        for (DataElement element : elementsInIndicator) {
                            String format = "%s#{%s.%s}";
                            if (numerator.length() > 0) {
                                format = "%s+#{%s.%s}";
                            }
                            List<CategoryOptionCombo> categoryOptionCombos = element.getCategoryCombo().getCategoryOptionCombos();
                            if (categoryOptionCombos != null && !categoryOptionCombos.isEmpty()) {
                                CategoryOptionCombo categoryOptionCombo = categoryOptionCombos.get(0);
                                numerator = String.format(format, numerator, element.getId(), categoryOptionCombo.getId());
                            }

                        }
                        String groupName = commodity.getName();
                        String indicatorName = groupName + "_LOSSES";
                        Indicator indicator = Indicator.builder().id(generateID(indicatorName)).name(indicatorName).indicatorType(indicatorType).denominator("1").numeratorDescription("").numerator(numerator).shortName(getShortName(indicatorName)).build();
                        indicators.add(indicator);
                        losses.getIndicators().add(indicator);
                    }

                }
            }
            if (dataSet == allocated) {
                createSpecialDataElements(actionAttribute, dataElements, dataSet, DataElementType.ALLOCATION_ID, "", "ALLOCATION_ID");
            }

            dataSets.add(dataSet);
        }
    }

    private void setupEmergencyOrder(List<ExcelCategory> categories) {
        DataSet dataSetEmergencyOrder = createDataSet(LMIS_KEY_WORD + "Emergency Order", DAILY);
        for (ExcelCategory category : categories) {
            for (ExcelCommodity commodity : category.getCommodityList()) {
                List<DataElementType> dataElementTypes = new ArrayList<DataElementType>(Arrays.asList(DataElementType.EMERGENCY_ORDERED_AMOUNT, DataElementType.EMERGENCY_REASON_FOR_ORDER));
                for (DataElementType type : dataElementTypes) {
                    String element_name = commodity.getName() + " " + type.getActivity();
                    DataElement dataElement = createDataElement(actionAttribute, element_name, type);
                    if (type.getActivity().equals(DataElementType.EMERGENCY_REASON_FOR_ORDER.getActivity())) {
                        dataElement.setOptionSet(orderReasonOptionSet);
                    }
                    DataElementGroup group = getOrCreateDataElementGroup(commodity.getName(), dataElementGroups, commodity.isNonLGA());
                    group.getDataElements().add(dataElement);
                    dataSetEmergencyOrder.getDataElements().add(dataElement);
                    dataElements.add(dataElement);
                }


            }
        }
        createSpecialDataElements(actionAttribute, dataElements, dataSetEmergencyOrder, DataElementType.ORDER_ID, "Emergency", "ORDER_ID");
        dataSets.add(dataSetEmergencyOrder);
    }

    private void setUpRoutineOrder(List<ExcelCategory> categories) {
        actOnCommodity(categories, new IActor() {

            private DataSet dataSet;
            private DataElementGroupSet groupSet;

            @Override
            public void beforeEachCategory(ExcelCategory category) {
                String name = LMIS_KEY_WORD + category.getName() + " ORDERED ROUTINE";
                dataSet = createDataSet(name, MONTHLY);
                groupSet = createDataElementGroupSet(category.getName());
            }

            @Override
            public void afterEachCategory(ExcelCategory category) {
                createSpecialDataElements(actionAttribute, dataElements, dataSet, DataElementType.ORDER_ID, category.getName(), "ORDER_ID");
                dataElementGroupSets.add(groupSet);
                dataSets.add(dataSet);
            }

            @Override
            public void onEachCommodity(ExcelCommodity commodity) {
                DataElementGroup group = getOrCreateDataElementGroup(commodity.getName(), dataElementGroups, commodity.isNonLGA());
                List<DataElementType> dataElementTypes = new ArrayList<DataElementType>(Arrays.asList(DataElementType.ORDERED_AMOUNT, DataElementType.REASON_FOR_ORDER));
                for (DataElementType type : dataElementTypes) {
                    String element_name = commodity.getName() + " " + type.getActivity();
                    DataElement dataElement = createDataElement(actionAttribute, element_name, type);
                    if (type.getActivity().equals(DataElementType.REASON_FOR_ORDER.getActivity())) {
                        dataElement.setOptionSet(orderReasonOptionSet);
                    }
                    group.getDataElements().add(dataElement);
                    dataSet.getDataElements().add(dataElement);

                    dataElements.add(dataElement);
                }
                groupSet.getDataElementGroups().add(group);
                dataElementGroups.add(group);
            }
        });
    }

    private void createSpecialDataElements(Attribute attribute, List<DataElement> dataElements, DataSet dataSet, DataElementType orderId, String categoryName, String order_id) {
        DataElement dataElement = createDataElement(attribute, String.format("%s " + order_id, categoryName), orderId);
        dataSet.getDataElements().add(dataElement);
        dataElements.add(dataElement);
    }

    private List<ExcelCategory> getCategories(BufferedReader bufferedReader) throws IOException {
        List<ExcelCategory> categories = new ArrayList<ExcelCategory>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] parts = line.split(",");
            String categoryName = parts[0];
            String commodityName = parts[1];
            String isNonLGAText = parts[2];
            boolean isNonLGA = isNonLGAText.equalsIgnoreCase("1");


            ExcelCategory category = new ExcelCategory(categoryName);
            if (!categories.contains(category)) {
                categories.add(category);
            }
            categories.get(categories.indexOf(category)).getCommodityList().add(new ExcelCommodity(commodityName, isNonLGA));
        }

        return categories;
    }

    private DataElementGroupSet createDataElementGroupSet(String name) {
        DataElementGroupSet dataElementGroupSet = DataElementGroupSet.builder().name(name).id(generateID(name)).shortName(getShortName(name)).build();
        dataElementGroupSet.setDataElementGroups(new ArrayList<DataElementGroup>());
        return dataElementGroupSet;
    }

    private DataElementGroup getOrCreateDataElementGroup(String groupName, List<DataElementGroup> groups, boolean nonLGA) {
        String value = "0";
        if (nonLGA) {
            value = "1";
        }
        AttributeValue attributeValue = AttributeValue.builder().attribute(lgaAttribute).value(value).build();
        DataElementGroup build = DataElementGroup.builder().
                name(groupName)
                .id(generateID(groupName))
                .shortName(getShortName(groupName))
                .attributeValues(Arrays.asList(attributeValue))
                .dataElements(new ArrayList<DataElement>()).
                        build();
        if (groups.contains(build)) {
            return groups.get(groups.indexOf(build));
        }
        return build;
    }

    private String getShortName(String name) {
        String shortName = name;
        if (name.length() > 50) {
            shortName = name.substring(0, 49);
        }
        if (shortNames.contains(shortName)) {
            shortName = name.substring(0, shortName.length() - 4);
            shortName = shortName.concat(" " + RandomStringUtils.randomAlphabetic(3));
        }
        shortNames.add(shortName);
        return shortName;
    }

    private DataSet createDataSet(String name, String periodType) {
        DataSet dataSet = DataSet.builder().name(name).id(generateID(name)).shortName(getShortName(name)).periodType(periodType).build();
        dataSet.setDataElements(new ArrayList<DataElement>());
        dataSet.setIndicators(new ArrayList<Indicator>());
        return dataSet;
    }

    private Attribute createAttribute(String type, String name, boolean dataElementAttribute, boolean dataElementGroupAttribute) {
        return Attribute.builder().id(generateID(name)).name(name).code(name).dataElementAttribute(dataElementAttribute).dataElementGroupAttribute(dataElementGroupAttribute).valueType(type).build();
    }

    private DataElement createDataElement(Attribute attribute, String dataElementName, DataElementType dataElementType) {
        AttributeValue expired = AttributeValue.builder().attribute(attribute).value(dataElementType.getActivity()).build();

        return DataElement.builder()
                .id(generateID(dataElementName))
                .name(dataElementName)
                .attributeValues(Arrays.asList(expired))
                .zeroIsSignificant(true)
                .categoryCombo(defaultCategoryCombo)
                .displayName(dataElementName)
                .shortName(getShortName(dataElementName))
                .type(dataElementType.getType())
                .domainType(AGGREGATE)
                .aggregationOperator("sum").build();
    }

    String generateHashOfString(String text) {
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(text.getBytes(Charset.forName("UTF8")));
            final byte[] resultByte = messageDigest.digest();
            return new String(Hex.encodeHex(resultByte));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    String generateID(String text) {
        return generateHashOfString(text).substring(0, 11);
    }

    void actOnCommodity(List<ExcelCategory> categories, IActor actor) {
        for (ExcelCategory category : categories) {
            actor.beforeEachCategory(category);
            for (ExcelCommodity commodity : category.getCommodityList()) {
                actor.onEachCommodity(commodity);
            }
            actor.afterEachCategory(category);
        }
    }

}
