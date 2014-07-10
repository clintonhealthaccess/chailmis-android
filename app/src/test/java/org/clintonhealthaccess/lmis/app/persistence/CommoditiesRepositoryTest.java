package org.clintonhealthaccess.lmis.app.persistence;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestFixture.initialiseDefaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditiesRepositoryTest {
    @Inject
    private CommoditiesRepository commoditiesRepository;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);
    }

    @Test
    public void testShouldLoadAllCommodityCategories() throws Exception {
        initialiseDefaultCommodities(application);

        List<Category> allCategories = commoditiesRepository.allCategories();

        assertThat(allCategories.size(), is(6));
        Category antiMalarialCategory = allCategories.get(0);
        assertThat(antiMalarialCategory.getName(), equalTo("Anti Malarials"));
        assertThat(antiMalarialCategory.getCommodities().size(), is(6));
        assertThat(antiMalarialCategory.getCommodities().get(0).getName(), equalTo("Coartem"));
    }

    @Test
    public void testShouldSaveAllCommodityCategories() throws Exception {
        List<Category> categories = defaultCommodities(application);
        commoditiesRepository.save(categories);

        List<Category> allCategories = commoditiesRepository.allCategories();
        assertThat(allCategories.size(), is(6));
        assertThat(allCategories.get(0).getCommodities().size(), is(6));
    }
}