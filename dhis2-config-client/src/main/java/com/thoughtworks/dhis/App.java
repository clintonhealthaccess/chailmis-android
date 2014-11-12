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

package com.thoughtworks.dhis;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.thoughtworks.dhis.configurations.IConfiguration;
import com.thoughtworks.dhis.configurations.LMISConfiguration;
import com.thoughtworks.dhis.endpoints.ApiService;
import com.thoughtworks.dhis.models.AttributeValue;
import com.thoughtworks.dhis.models.CategoryCombo;
import com.thoughtworks.dhis.models.DataElement;
import com.thoughtworks.dhis.models.DataElementType;
import com.thoughtworks.dhis.models.DataSet;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;
import com.thoughtworks.dhis.models.User;
import com.thoughtworks.dhis.models.UserProfile;
import com.thoughtworks.dhis.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static java.lang.String.format;

public class App {

    public static final int randomLimit = 500;

    public static void main(String[] args) throws IOException {
        Map<String, Task> commands = setUpTasks();
        if (args.length < 2) {
            System.out.println("Usage ./configure [dev|staging|prod] task");
            System.out.println("Available commands :");
            for (String key : commands.keySet()) {
                System.out.println("\t \t" + key);
            }

        } else {
            ApiService service = getService(args[0]);
            Task taskToExcecute = commands.get(args[1]);
            taskToExcecute.operateOnService(service);
        }

    }

    private static Map<String, Task> setUpTasks() {
        Map<String, Task> commands = new HashMap<>();
        commands.put("config", configTask);
        commands.put("maxMin", submitMaxAndMinThreshold);
        commands.put("monthSOH", submitMonthsStockOnHand);
        commands.put("calculatedData", submitCalculatedData);
        commands.put("defaultData", submitDefaultData);
        commands.put("allTestData", submitAllTestData);
        return commands;
    }

    private static Task submitMonthsStockOnHand = new Task() {
        @Override
        public void operateOnService(ApiService service) throws IOException {
            UserProfile me = service.getProfile();
            DataSet set = service.getDataSetWithDetails("a5321843640", "id,name,dataElements[id,name,attributeValues]");
            System.out.println(set.getDataElements().size());
            List<String> periods = previousAndCurrentPeriods();
            final List<String> dataElementTypes = Arrays.asList(DataElementType.MONTHS_OF_STOCK_ON_HAND.toString());
            for (String period : periods) {
                DataValueSet valueSet = new DataValueSet();
                valueSet.setDataValues(new ArrayList<DataValue>());
                valueSet.setOrgUnit(me.getOrganisationUnits().get(0).getId());
                valueSet.setDataSet(set.getId());

                for (DataElement element : set.getDataElements()) {
                    List<AttributeValue> attributeValues = element.getAttributeValues();
                    if (attributeValues != null) {

                        List<AttributeValue> attributes = FluentIterable.from(attributeValues).filter(new Predicate<AttributeValue>() {
                            @Override
                            public boolean apply(AttributeValue input) {
                                return dataElementTypes.contains(input.getValue());
                            }
                        }).toList();
                        if (attributes.size() > 0) {
                            int n = randInt(1, 6);

                            DataValue value = DataValue.builder()
                                    .dataElement(element.getId()).value(String.valueOf(n))
                                    .period(period).build();

                            valueSet.getDataValues().add(value);
                        }
                    }

                }

                Object data = service.submitValueSet(valueSet);

                System.out.println(data);

            }
        }

    };

    private static Task submitAllTestData = new Task() {
        @Override
        public void operateOnService(ApiService service) throws IOException {

            try {
                submitDefaultData.operateOnService(service);
            } catch (Exception ex) {
                System.err.println("Submitting Default Data timed out");
                ex.printStackTrace();
            }

            try {
                submitCalculatedData.operateOnService(service);
            } catch (Exception ex) {
                System.err.println("Submitting Calculated Data timed out");
                ex.printStackTrace();
            }

            try {
                submitMaxAndMinThreshold.operateOnService(service);
            } catch (Exception ex) {
                System.err.println("Submitting Minimum and Maximum Threshold Data timed out");
                ex.printStackTrace();
            }

            try {
                submitMonthsStockOnHand.operateOnService(service);
            } catch (Exception ex) {
                System.err.println("Submitting MonthsStockOnHand Data timed out");
                ex.printStackTrace();
            }

        }
    };


    private static ApiService getService(String env) throws IOException {
        System.out.println(env);
        Properties dhis2Properties = new Properties();
        dhis2Properties.load(App.class.getClassLoader().getResourceAsStream("dhis2." + env + ".properties"));
        String dhis2BaseUrl = dhis2Properties.getProperty("dhis2.base_url");
        String dhis2Username = dhis2Properties.getProperty("dhis2.username");
        String dhis2Password = dhis2Properties.getProperty("dhis2.password");
        String dhis2ApiUrl = dhis2BaseUrl + "/api/";

        System.out.println(format("Accessing API at %s with login - %s/%s", dhis2ApiUrl, dhis2Username, dhis2Password));
        return new Client(dhis2ApiUrl, new User(dhis2Username, dhis2Password)).getService();
    }

    private static Task configTask = new Task() {
        @Override
        public void operateOnService(ApiService service) throws IOException {
            CategoryCombo categoryCombo = service.searchCategoryCombos("default").getCategoryCombos().get(0);
            categoryCombo = service.getCombo(categoryCombo.getId());
            categoryCombo.setHref(null);
            categoryCombo.setCreated(null);
            categoryCombo.setLastUpdated(null);
            categoryCombo.setCategories(null);
            categoryCombo.setDimensionType(null);
            categoryCombo.setCategoryOptionCombos(null);

            IConfiguration lmisConfig = new LMISConfiguration(categoryCombo);
            Map<String, Object> data = lmisConfig.generateMetaData();

            service.updateMetaData(data);
        }
    };

    private static Task submitMaxAndMinThreshold = new Task() {
        @Override
        public void operateOnService(ApiService service) throws IOException {
            String maxType = DataElementType.MAXIMUM_THRESHOLD.toString();
            String minType = DataElementType.MINIMUM_THRESHOLD.toString();
            int middleValue = 250;
            int maxValue = 500;
            submitMaxMinValues(service, maxType, minType, middleValue, maxValue);

        }
    };

    private static void submitMaxMinValues(ApiService service, String maxType, String minType, int middleValue, int maxValue) {
        UserProfile me = service.getProfile();
        DataSet set = service.getDataSetWithDetails("a5321843640", "id,name,dataElements[id,name,attributeValues]");

        final List<String> dataElementTypes = Arrays.asList(maxType, minType);

        List<String> periods = previousAndCurrentPeriods();

        for (String period : periods) {
            DataValueSet valueSet = new DataValueSet();

            valueSet.setDataValues(new ArrayList<DataValue>());

            valueSet.setOrgUnit(me.getOrganisationUnits().get(0).getId());

            valueSet.setDataSet(set.getId());

            for (DataElement element : set.getDataElements()) {
                List<AttributeValue> attributeValues = element.getAttributeValues();
                if (attributeValues != null) {

                    List<AttributeValue> attributes = FluentIterable.from(attributeValues).filter(new Predicate<AttributeValue>() {
                        @Override
                        public boolean apply(AttributeValue input) {
                            return dataElementTypes.contains(input.getValue());
                        }
                    }).toList();

                    if (attributes.size() > 0) {

                        int n = randInt(0, middleValue);
                        if (attributes.get(0).getValue().equalsIgnoreCase(maxType)) {
                            n = randInt(middleValue, maxValue);
                        }
                        DataValue value = DataValue.builder()
                                .dataElement(element.getId()).value(String.valueOf(n)).period(period).build();
                        valueSet.getDataValues().add(value);
                    }
                }

            }
            try {
                Object data = service.submitValueSet(valueSet);
                System.out.println(data);
            } catch (Exception exception) {
                System.out.println("Timed out");
            }


        }
    }


    private static Task submitCalculatedData = new Task() {

        @Override
        public void operateOnService(ApiService service) throws IOException {
            UserProfile me = service.getProfile();
            DataSet set = service.getDataSet("a5321843640");
            System.out.println(set.getDataElements().size());

            List<String> periods = previousAndCurrentPeriods();

            for (String period : periods) {
                for (DataElement element : set.getDataElements()) {

                    DataValueSet valueSet = new DataValueSet();
                    valueSet.setDataValues(new ArrayList<DataValue>());
                    valueSet.setOrgUnit(me.getOrganisationUnits().get(0).getId());

                    Random rand = new Random();
                    int n = rand.nextInt(randomLimit) + 1;
                    DataValue value = DataValue.builder()
                            .dataElement(element.getId()).value(String.valueOf(n)).period(period).build();
                    valueSet.getDataValues().add(value);

                    Object data = service.submitValueSet(valueSet);
                    System.out.println(data);
                }

            }
        }
    };


    private static Task submitDefaultData = new Task() {
        @Override
        public void operateOnService(ApiService service) throws IOException {
            UserProfile me = service.getProfile();
            DataSet set = service.getDataSet("a1ce7aa8c65");
            System.out.println(set.getDataElements().size());

            DataValueSet valueSet = new DataValueSet();
            valueSet.setDataValues(new ArrayList<DataValue>());

            Date currentDate = Calendar.getInstance().getTime();
            String period = getYearMonthDayString(currentDate);
            valueSet.setOrgUnit(me.getOrganisationUnits().get(0).getId());
            for (DataElement element : set.getDataElements()) {
                Random rand = new Random();
                int n = rand.nextInt(randomLimit) + 1;
                DataValue value = DataValue.builder()
                        .dataElement(element.getId()).value(String.valueOf(n)).period(period).build();
                valueSet.getDataValues().add(value);
            }
            Object data = service.submitValueSet(valueSet);
            System.out.println(data);
        }
    };


    public App() {

    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static String getYearMonthString(Date date) {
        return new SimpleDateFormat("yyyyMM").format(date);
    }

    public static String getYearMonthDayString(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    private static List<String> previousAndCurrentPeriods() {
        Calendar calender = Calendar.getInstance();
        String currentPeriod = getYearMonthString(calender.getTime());

        calender.add(Calendar.MONTH, -1);
        String previousMonthDate = getYearMonthString(calender.getTime());

        return Arrays.asList(previousMonthDate, currentPeriod);
    }

}
