package org.clintonhealthaccess.lmis.app.models.api;

import com.google.common.base.Function;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

public class SmsValueSet {
    private final String dataSet;
    private final List<SmsValue> values = newArrayList();

    public SmsValueSet(String dataSet) {
        this.dataSet = dataSet;
    }

    public void addValues(List<SmsValue> values) {
        this.values.addAll(values);
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
