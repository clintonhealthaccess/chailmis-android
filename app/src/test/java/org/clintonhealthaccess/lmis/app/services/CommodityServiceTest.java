package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestFixture.initialiseDefaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class CommodityServiceTest {
    @Inject
    private CategoryService categoryService;

    @Inject
    private CommodityService commodityService;

    @Before
    public void setUp() throws Exception {
        final LmisServer mockLmisServer = mock(LmisServer.class);
        when(mockLmisServer.fetchCommodities()).thenReturn(defaultCommodities(application));

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });
    }

    @Test
    public void testShouldLoadAllCommodityCategories() throws Exception {
        initialiseDefaultCommodities(application);
        verifyAllCommodityCategories();
    }

    @Test
    public void testShouldPrepareDefaultCommodities() throws Exception {
        commodityService.initialise();
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
}