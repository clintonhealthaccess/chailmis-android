package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.List;

@Getter
@Setter
@Builder
public class CategoryCombo {

    private String id;

    private String name;

    private String created;

    private String lastUpdated;

    private String href;

    private List<Category> categories;

    private List<CategoryOptionCombo> categoryOptionCombos;

    private String dimensionType;
}
