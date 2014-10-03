package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Builder
@Data
public class AttributeValue {
    private String value;
    private Attribute attribute;
}
