package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.AggregationField;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestFixture.getDefaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class CommodityServiceTest {
    @Inject
    private CategoryService categoryService;

    @Inject
    private CommodityService commodityService;
    @Inject
    private DbUtil dbUtil;

    @Before
    public void setUp() throws Exception {
        final LmisServer mockLmisServer = mock(LmisServer.class);
        when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(application));

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });
    }

    @Test
    public void testShouldLoadAllCommodityCategories() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        verifyAllCommodityCategories();
    }

    @Test
    public void shouldLoadAllCommodities() throws IOException {
        commodityService.initialise(new User("test", "pass"));
        List<Commodity> expectedCommodities = getDefaultCommodities(application);

        List<Commodity> commodities = commodityService.all();

        assertThat(commodities.size(), is(7));
        for (Commodity commodity : expectedCommodities) {
            assertThat(expectedCommodities, contains(commodity));
        }
    }

    @Test
    public void testShouldPrepareDefaultCommodities() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        verifyAllCommodityCategories();
    }

    private void verifyAllCommodityCategories() {
        List<Category> allCategories = categoryService.all();

        assertThat(allCategories.size(), is(6));
        Category antiMalarialCategory = allCategories.get(0);
        assertThat(antiMalarialCategory.getName(), equalTo("Anti Malarials"));
        assertThat(antiMalarialCategory.getCommodities().size(), is(6));
        assertThat(antiMalarialCategory.getCommodities().get(0).getName(), equalTo("Coartem"));
    }

    @Test
    public void shouldSaveCommodityAggregations() {
        List<Category> categories = new ArrayList<>();
        Category category = new Category("named cat");
        Commodity commodity = new Commodity("cat food");

        Aggregation aggregation = new Aggregation();
        aggregation.setId("id");
        aggregation.setName("sum");

        AggregationField field = new AggregationField();
        field.setId("some id");
        field.setName("stock lost");

        aggregation.setAggregationFields(Arrays.asList(field));

        commodity.setAggregation(aggregation);

        category.addCommodity(commodity);

        categories.add(category);

        commodityService.saveToDatabase(categories);

        Long numberOfAggregations = dbUtil.withDao(Aggregation.class, new DbUtil.Operation<Aggregation, Long>() {
            @Override
            public Long operate(Dao<Aggregation, String> dao) throws SQLException {
                return dao.countOf();
            }
        });

        Long numberOfFields = dbUtil.withDao(AggregationField.class, new DbUtil.Operation<AggregationField, Long>() {
            @Override
            public Long operate(Dao<AggregationField, String> dao) throws SQLException {
                return dao.countOf();
            }
        });

        assertThat(numberOfAggregations, is(1L));
        assertThat(numberOfFields, is(1L));

    }
}