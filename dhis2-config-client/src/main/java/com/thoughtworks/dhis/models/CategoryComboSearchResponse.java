package com.thoughtworks.dhis.models;

import lombok.Data;

import java.util.List;

@Data
public class CategoryComboSearchResponse {
    private List<CategoryCombo> categoryCombos;
}
