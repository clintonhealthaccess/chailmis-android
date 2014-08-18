/*
 * Copyright (c) 2014, Clinton Health Access Initiative
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.AggregationField;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
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

    @Ignore("Work In progress...")
    @Test
    public void shouldSaveCommodityAggregationFields() {

    }

    @Ignore("Work In progress...")
    @Test
    public void shouldSaveCommodityDataSet() {
        List<Category> categories = getTestCategories();
        commodityService.saveToDatabase(categories);
    }

    @Test
    public void shouldSaveCommodityAggregations() {
        List<Category> categories = getTestCategories();

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

    private List<Category> getTestCategories() {

        DataSet dataSet = new DataSet();
        dataSet.setDataSetId(12324L);
        dataSet.setDescription("consumption data set");
        dataSet.setName("consumption");

        List<Category> categories = new ArrayList<>();
        Category category = new Category("named cat");
        category.setDataSet(dataSet);
        Commodity commodity = new Commodity("cat food");
        Commodity commodityDogFood = new Commodity("dog food");


        Aggregation aggregation = new Aggregation();
        aggregation.setId("id");
        aggregation.setName("sum");

        AggregationField field = new AggregationField();
        field.setId("some id");
        field.setName("stock lost");

        aggregation.setAggregationFields(Arrays.asList(field));

        commodity.setAggregation(aggregation);
        commodityDogFood.setAggregation(aggregation);

        category.addCommodity(commodity);
        category.addCommodity(commodityDogFood);

        categories.add(category);
        return categories;
    }
}