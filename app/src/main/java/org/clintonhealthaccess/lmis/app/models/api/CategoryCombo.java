package org.clintonhealthaccess.lmis.app.models.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCombo {
    private String name;
    private String id;
    private List<CategoryOptionCombos> categoryOptionCombos;

}
