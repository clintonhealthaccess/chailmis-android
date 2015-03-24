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

package com.thoughtworks.dhis.models;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.List;

import lombok.Getter;

import static com.google.common.collect.FluentIterable.from;

@Getter
public enum DataElementType {
    ADJUSTMENTS("ADJUSTMENTS"),
    ADJUSTMENT_REASON("ADJUSTMENT_REASON"),
    ALLOCATED("ALLOCATED"),
    ALLOCATION_ID("string", "ALLOCATION_ID"),
    AMC("AMC"),
    BREAKAGE("BREAKAGE"),
    BUFFER_STOCK("BUFFER_STOCK", true),
    DISPENSED("DISPENSED"),
    EMERGENCY_ORDERED_AMOUNT("EMERGENCY_ORDERED_AMOUNT"),
    EMERGENCY_REASON_FOR_ORDER("string", "EMERGENCY_REASON_FOR_ORDER"),
    EXPIRED("EXPIRED"),
    FROZEN("FROZEN"),
    LABEL_REMOVED("LABEL_REMOVED"),
    MAX_STOCK_QUANTITY("MAX_STOCK_QUANTITY", true),
    MIN_STOCK_QUANTITY("MIN_STOCK_QUANTITY", true),
    MISSING("MISSING"),
    MONTHS_OF_STOCK_ON_HAND("MONTHS_OF_STOCK_ON_HAND"),
    NUMBER_OF_STOCK_OUT_DAYS("NUMBER_OF_STOCK_OUT_DAYS"),
    ORDERED_AMOUNT("ORDERED_AMOUNT"),
    ORDER_ID("string", "ORDER_ID"),
    OTHERS("OTHERS"),
    PROJECTED_ORDER_AMOUNT("PROJECTED_ORDER_AMOUNT"),
    REASON_FOR_ORDER("string", "REASON_FOR_ORDER"),
    RECEIVED("RECEIVED"),
    RECEIVE_DATE("string", "RECEIVE_DATE"),
    RECEIVE_SOURCE("string", "RECEIVE_SOURCE"),
    SAFETY_STOCK("SAFETY_STOCK"),
    STOCK_ON_HAND("STOCK_ON_HAND"),
    TMC("TMC"),
    VVM_CHANGE("VVM_CHANGE"),
    WASTED("WASTED");

    private String type = "int";
    private String activity;
    private boolean isIndicator = false;

    DataElementType(String activity, boolean isIndicator) {
        this.activity = activity;
        this.isIndicator = isIndicator;
    }

    DataElementType(String type, String activity) {
        this.type = type;
        this.activity = activity;
    }

    DataElementType(String activity) {
        this.activity = activity;
    }

    public static boolean dataElementActivityExists(String activityString) {
        return getDataElementStrings(false).contains(activityString.trim());
    }

    public static boolean indicatorExists(String activityString) {
        return getDataElementStrings(true).contains(activityString.trim());
    }

    public static List<String> getDataElementStrings(boolean isIndicator) {
        return from(getDataElementTypes(isIndicator)).transform(new Function<DataElementType, String>() {
            @Override
            public String apply(DataElementType input) {
                return input.getActivity();
            }
        }).toList();
    }

    private static List<DataElementType> getDataElementTypes(final boolean isIndicator) {
        List<DataElementType> types = Lists.newArrayList(DataElementType.values());
        return from(types).filter(new Predicate<DataElementType>() {
            @Override
            public boolean apply(DataElementType input) {
                return input.isIndicator() == isIndicator;
            }
        }).toList();
    }
}
