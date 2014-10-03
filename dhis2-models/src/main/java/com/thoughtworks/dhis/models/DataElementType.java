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
    NUMBER_OF_STOCK_OUT_DAYS("NUMBER_OF_STOCK_OUT_DAYS"),
    MONTHS_OF_STOCK_ON_HAND("MONTHS_OF_STOCK_ON_HAND"),
    RECEIVE_DATE("string", "RECEIVE_DATE"),
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
