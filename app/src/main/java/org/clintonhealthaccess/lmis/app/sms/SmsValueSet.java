package org.clintonhealthaccess.lmis.app.sms;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;

public class SmsValueSet {
    private static final SimpleDateFormat SMS_PERIOD_DATE_FORMAT = new SimpleDateFormat("ddMM");
    private final List<SmsValue> values;
    private String prefix;

    public static List<SmsValueSet> build(final DataValueSet dataValueSet) {
        Multimap<String, DataValue> dataValueGroups = groupByPrefix(dataValueSet.getDataValues());
        List<SmsValueSet> result = newArrayList();
        for (final String smsPrefix : dataValueGroups.keySet()) {
            List<List<DataValue>> dataValueLists = partition(newArrayList(dataValueGroups.get(smsPrefix)), 6);
            ImmutableList<SmsValueSet> smsValueSets = from(dataValueLists).transform(new Function<List<DataValue>, SmsValueSet>() {
                @Override
                public SmsValueSet apply(List<DataValue> dataValueList) {
                    return new SmsValueSet(smsPrefix, dataValueList);
                }
            }).toList();
            result.addAll(smsValueSets);
        }
        return result;
    }

    private static Multimap<String, DataValue> groupByPrefix(List<DataValue> dataValues) {
        return Multimaps.index(dataValues, new Function<DataValue, String>() {
            @Override
            public String apply(DataValue input) {
                Date periodDate = input.periodAsDate();
                return input.getDataSet() + " " + SMS_PERIOD_DATE_FORMAT.format(periodDate);
            }
        });
    }

    private SmsValueSet(String prefix, List<DataValue> dataValueList) {
        this.prefix = prefix;
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
        return prefix + " " + on(".").join(smsValues);
    }
}
