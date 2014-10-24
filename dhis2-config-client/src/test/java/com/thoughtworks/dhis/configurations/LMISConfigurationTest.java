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
import com.thoughtworks.dhis.models.CategoryCombo;
import com.thoughtworks.dhis.models.DataElementGroup;
import com.thoughtworks.dhis.models.ExcelCategory;
import com.thoughtworks.dhis.models.ExcelCommodity;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.dhis.configurations.LMISConfiguration.getCategories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class LMISConfigurationTest {
    @Test
    public void shouldHaveKeyDataSets() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        assertKeyIsSetAndNotEmpty(configuration, LMISConfiguration.DATA_SETS);
    }

    @Test
    public void shouldHaveKeyDataElements() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        assertKeyIsSetAndNotEmpty(configuration, LMISConfiguration.DATA_ELEMENTS);
    }

    @Test
    public void shouldHaveKeyDataElementsGroups() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        assertKeyIsSetAndNotEmpty(configuration, LMISConfiguration.DATA_ELEMENT_GROUPS);
    }

    @Test
    public void shouldHaveKeyDataElementsGroupsSets() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        assertKeyIsSetAndNotEmpty(configuration, LMISConfiguration.DATA_ELEMENT_GROUP_SETS);
    }

    @Test
    public void shouldHaveAllTheRequiredKeys() throws Exception {
        List<String> keys = Arrays.asList(LMISConfiguration.INDICATOR_TYPES, LMISConfiguration.INDICATORS, LMISConfiguration.OPTION_SETS, LMISConfiguration.ATTRIBUTES, "constants");
        LMISConfiguration configuration = getLmisConfiguration();
        for (String key : keys) {
            assertKeyIsSetAndNotEmpty(configuration, key);
        }
    }

    @Test
    public void shouldHaveTwoAttributesForEachDataElementGroup() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        List<DataElementGroup> groups = (List<DataElementGroup>) configuration.generateMetaData().get(LMISConfiguration.DATA_ELEMENT_GROUPS);

        for (DataElementGroup group : groups) {
            assertThat(group.getAttributeValues().size(), is(2));
            assertThat(group.getAttributeValues().get(0).getAttribute().getDataElementGroupAttribute(), is(true));
            assertThat(group.getAttributeValues().get(0).getAttribute().getDataElementAttribute(), is(false));
            assertThat(group.getAttributeValues().get(0).getAttribute().getName(), is(Attribute.LMIS_NON_LGA));
            assertThat(group.getAttributeValues().get(1).getAttribute().getDataElementAttribute(), is(false));
            assertThat(group.getAttributeValues().get(1).getAttribute().getName(), is(Attribute.LMIS_DEVICE));
        }

    }

    @Test
    public void shouldHaveSomeGroupsConfiguredAsDevices() throws Exception {
        LMISConfiguration configuration = getLmisConfiguration();
        List<DataElementGroup> groups = (List<DataElementGroup>) configuration.generateMetaData().get(LMISConfiguration.DATA_ELEMENT_GROUPS);

        for (DataElementGroup group : groups) {
            if (group.getName().toLowerCase().contains("syringe")) {
                assertThat(group.getName(),group.getAttributeValues().size(), is(2));
                assertThat(group.getName(),group.getAttributeValues().get(0).getAttribute().getDataElementGroupAttribute(), is(true));
                assertThat(group.getName(),group.getAttributeValues().get(0).getAttribute().getDataElementAttribute(), is(false));
                assertThat(group.getName(),group.getAttributeValues().get(0).getAttribute().getName(), is(Attribute.LMIS_NON_LGA));
                assertThat(group.getName(),group.getAttributeValues().get(1).getAttribute().getDataElementAttribute(), is(false));
                assertThat(group.getName(),group.getAttributeValues().get(1).getAttribute().getName(), is(Attribute.LMIS_DEVICE));
                assertThat(group.getName(),group.getAttributeValues().get(1).getValue(), is("1"));
            }

        }

    }

    @Test
    public void shouldHaveSomeDevices() throws Exception {
        List<ExcelCategory> categories = getCategories(new BufferedReader(new FileReader("commodities.csv")));
        for (ExcelCategory category : categories) {
            for (ExcelCommodity commodity : category.getCommodityList()) {
                if (commodity.getName().toLowerCase().contains("syringe")) {
                    assertThat(commodity.isDevice(), is(true));
                } else {
                    assertThat(commodity.getName(), commodity.isDevice(), is(false));
                }
            }
        }

    }

    private void assertKeyIsSetAndNotEmpty(LMISConfiguration configuration, String key) throws IOException {
        Map<String, Object> objectMap = configuration.generateMetaData();

        assertThat(objectMap, hasKey(key));
        assertThat(objectMap.get(key), is(notNullValue()));
    }

    private LMISConfiguration getLmisConfiguration() {
        return new LMISConfiguration(CategoryCombo.builder().id("12").build());
    }
}