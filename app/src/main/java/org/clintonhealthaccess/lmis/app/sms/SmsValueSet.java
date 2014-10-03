package org.clintonhealthaccess.lmis.app.sms;

import com.google.common.base.Function;

import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

public class SmsValueSet {
    private final String dataSet;
    private final List<SmsValue> values;

    public static List<SmsValueSet> build(DataValueSet dataValueSet) {
        return newArrayList(new SmsValueSet(dataValueSet));
    }

    private SmsValueSet(DataValueSet dataValueSet) {
        this.dataSet = dataValueSet.getDataSet();
        values = from(dataValueSet.getDataValues()).transform(new Function<DataValue, SmsValue>() {
            @Override
            public SmsValue apply(DataValue dataValue) {
                return new SmsValue(dataValue);
            }
        }).toList();
    }

    @Override
    public String toString() {
        Collection<String> smsValues = transform(values, new Function<SmsValue, String>() {
            @Override
            public String apply(SmsValue input) {
                return input.toString();
            }
        });
        return dataSet + " " + on(".").join(smsValues);
    }
}
