package com.thoughtworks.dhis.configurations;

import com.thoughtworks.dhis.models.CategoryCombo;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private void assertKeyIsSetAndNotEmpty(LMISConfiguration configuration, String key) throws IOException {
        Map<String, Object> objectMap = configuration.generateMetaData();

        assertThat(objectMap, hasKey(key));
        assertThat(objectMap.get(key), is(notNullValue()));
    }

    private LMISConfiguration getLmisConfiguration() {
        return new LMISConfiguration(CategoryCombo.builder().id("12").build());
    }
}