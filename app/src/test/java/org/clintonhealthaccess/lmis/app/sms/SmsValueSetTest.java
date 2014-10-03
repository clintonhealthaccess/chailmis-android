package org.clintonhealthaccess.lmis.app.sms;

import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
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
        DataValue dataValue1 = DataValue.builder().dataElement("element_1").value("11").build();
        DataValue dataValue2 = DataValue.builder().dataElement("element_2").value("22").build();
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataSet("set_1");
        dataValueSet.setDataValues(newArrayList(dataValue1, dataValue2));

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);
        assertThat(smsValueSets.get(0).toString(), is("set_1 element_1.11.element_2.22"));
    }

    @Test
    public void shouldSplitDataValueSetIfThereAreTooManyDataValues() throws Exception {
        List<DataValue> dataValues = newArrayList();
        for(int i = 0; i < 50; i++) {
            DataValue dataValue = DataValue.builder().dataElement("element_" + i).value(valueOf(i)).build();
            dataValues.add(dataValue);
        }
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataSet("set_1");
        dataValueSet.setDataValues(dataValues);

        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);

        assertThat(smsValueSets.size(), is(9));
        for (SmsValueSet smsValueSet : smsValueSets) {
            assertThat(smsValueSet.toString().length(), lessThan(140));
        }
        assertThat(smsValueSets.get(0).toString(), is("set_1 element_0.0.element_1.1.element_2.2.element_3.3.element_4.4.element_5.5"));
    }
}