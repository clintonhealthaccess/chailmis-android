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

import lombok.Getter;

@Getter
public enum DataElementType {
    ORDER_ID("string", "ORDER_ID"),
    ALLOCATION_ID("string", "ALLOCATION_ID"),
    RECEIVED("RECEIVED"),
    DISPENSED("DISPENSED"),
    EXPIRED("EXPIRED"),
    STOCK_ON_HAND("STOCK_ON_HAND"),
    WASTED("WASTED"),
    ADJUSTMENTS("ADJUSTMENTS"),
    ADJUSTMENT_REASON("ADJUSTMENT_REASON"),
    MISSING("MISSING"),
    VVM_CHANGE("VVM_CHANGE"),
    BREAKAGE("BREAKAGE"),
    FROZEN("FROZEN"),
    LABEL_REMOVED("LABEL_REMOVED"),
    OTHERS("OTHERS"),
    ALLOCATED("ALLOCATED"),
    EMERGENCY_ORDERED_AMOUNT("EMERGENCY_ORDERED_AMOUNT"),
    ORDERED_AMOUNT("ORDERED_AMOUNT"),
    PROJECTED_ORDER_AMOUNT("PROJECTED_ORDER_AMOUNT"),
    EMERGENCY_REASON_FOR_ORDER("string", "EMERGENCY_REASON_FOR_ORDER"),
    REASON_FOR_ORDER("string", "REASON_FOR_ORDER"),
    MAXIMUM_THRESHOLD("MAXIMUM_THRESHOLD"),
    MINIMUM_THRESHOLD("MINIMUM_THRESHOLD"),
    //MAXIMUM_STOCK_LEVEL("MAXIMUM_STOCK_LEVEL"),
    //MINIMUM_STOCK_LEVEL("MINIMUM_STOCK_LEVEL"),
    NUMBER_OF_STOCK_OUT_DAYS("NUMBER_OF_STOCK_OUT_DAYS"),
    MONTHS_OF_STOCK_ON_HAND("MONTHS_OF_STOCK_ON_HAND"),
    RECEIVE_DATE("string", "RECEIVE_DATE"),
    RECEIVE_SOURCE("string", "RECEIVE_SOURCE"),
    AMC("AMC"),
    TMC("TMC"),
    SAFETY_STOCK("SAFETY_STOCK"),
    BUFFER_STOCK("BUFFER_STOCK");

    private String type;
    private String activity;

    DataElementType(String type, String activity) {
        this.type = type;
        this.activity = activity;
    }

    DataElementType(String activity) {
        this.activity = activity;
        this.type = "int";
    }
}
