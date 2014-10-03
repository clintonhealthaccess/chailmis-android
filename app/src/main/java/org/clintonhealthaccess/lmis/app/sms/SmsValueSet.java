package org.clintonhealthaccess.lmis.app.sms;

import com.google.common.base.Function;

import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.partition;

public class SmsValueSet {
    private final String dataSet;
    private final List<SmsValue> values;

    public static List<SmsValueSet> build(final DataValueSet dataValueSet) {
        List<List<DataValue>> dataValueLists = partition(dataValueSet.getDataValues(), 6);
        return from(dataValueLists).transform(new Function<List<DataValue>, SmsValueSet>() {
            @Override
            public SmsValueSet apply(List<DataValue> dataValueList) {
                return new SmsValueSet(dataValueSet.getDataSet(), dataValueList);
            }
        }).toList();
    }

    private SmsValueSet(String dataSet, List<DataValue> dataValueList) {
        this.dataSet = dataSet;
        this.values = from(dataValueList).transform(new Function<DataValue, SmsValue>() {
            @Override
            public SmsValue apply(DataValue input) {
                return new SmsValue(input);
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
