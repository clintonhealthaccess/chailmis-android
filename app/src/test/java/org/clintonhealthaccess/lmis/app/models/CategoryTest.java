package org.clintonhealthaccess.lmis.app.models;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CategoryTest {
    @Test
    public void testShouldBeAbleToGetAllCategories() throws Exception {
        List<Category> allCategories = Category.all();
        assertThat(allCategories.size(), equalTo(6));
        Category firstCategory = allCategories.get(0);
        assertThat(firstCategory.getName(), equalTo("Anti Malarials"));

        List<Commodity> commodities = firstCategory.getCommodities();
        assertThat(commodities.size(), equalTo(6));
        assertThat(commodities.get(0).getName(), equalTo("Coartem"));
    }
}
