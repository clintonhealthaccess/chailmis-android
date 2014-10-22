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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.Getter;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;

@Getter
public class SmsValueSet implements Serializable {
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
