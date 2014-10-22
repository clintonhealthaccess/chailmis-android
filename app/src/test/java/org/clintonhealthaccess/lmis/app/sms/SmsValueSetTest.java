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

package org.clintonhealthaccess.lmis.app.sms;

import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class SmsValueSetTest {
    @Test
    public void shouldConvertDataValueSetToSmsValueSet() throws Exception {
        DataValue dataValue1 = buildDataValue("set_1", "element_1", "20141008", "11");
        DataValue dataValue2 = buildDataValue("set_1", "element_2", "20141008", "22");
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataValues(newArrayList(dataValue1, dataValue2));

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);

        assertThat(smsValueSets.get(0).toString(), is("set_1 0810 element_1.11.element_2.22"));
    }

    @Test
    public void shouldSplitDataValueSetBasedOnDataSetsAndPeriods() throws Exception {
        DataValue dataValue1 = buildDataValue("set_1", "element_1", "20141008", "11");
        DataValue dataValue2 = buildDataValue("set_2", "element_2", "20141008", "22");
        DataValue dataValue3 = buildDataValue("set_1", "element_3", "20141007", "33");
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataValues(newArrayList(dataValue1, dataValue2, dataValue3));

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);

        assertThat(smsValueSets.size(), is(3));
        assertThat(smsValueSets.get(0).toString(), is("set_1 0810 element_1.11"));
        assertThat(smsValueSets.get(1).toString(), is("set_2 0810 element_2.22"));
        assertThat(smsValueSets.get(2).toString(), is("set_1 0710 element_3.33"));
    }

    @Test
    public void shouldAcceptMonthlyPeriod() throws Exception {
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataValues(newArrayList(buildDataValue("set_1", "element_1", "201410", "11")));

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);

        assertThat(smsValueSets.size(), is(1));
        assertThat(smsValueSets.get(0).toString(), is("set_1 0110 element_1.11"));
    }

    @Test
    public void shouldSplitDataValueSetIfThereAreTooManyDataValues() throws Exception {
        List<DataValue> dataValues = newArrayList();
        for(int i = 0; i < 50; i++) {
            DataValue dataValue = buildDataValue("set_1", "element_" + i, "20141008", valueOf(i));
            dataValues.add(dataValue);
        }
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataValues(dataValues);

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);

        assertThat(smsValueSets.size(), is(9));
        for (SmsValueSet smsValueSet : smsValueSets) {
            assertThat(smsValueSet.toString().length(), lessThan(140));
        }
        String expectedSms = "set_1 0810 element_0.0.element_1.1.element_2.2.element_3.3.element_4.4.element_5.5";
        assertThat(smsValueSets.get(0).toString(), is(expectedSms));
    }

    private DataValue buildDataValue(String dataSetId, String dataElementId, String period, String value) {
        return DataValue.builder().dataSet(dataSetId).dataElement(dataElementId).period(period).value(value).build();
    }

}