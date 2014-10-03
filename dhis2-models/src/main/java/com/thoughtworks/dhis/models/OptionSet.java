package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

import java.util.List;

@Data
@Builder
public class OptionSet {
    private String name;
    private String id;
    private List<String> options;
}
