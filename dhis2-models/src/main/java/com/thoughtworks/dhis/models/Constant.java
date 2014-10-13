package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Constant {
    private String id, name, displayName;
    private Double value;
}
