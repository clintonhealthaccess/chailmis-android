package org.clintonhealthaccess.lmis.app.persistence;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.io.ByteStreams.copy;
import static org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository.COMMODITIES_FILE;
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
    public void testShouldLoadAllCommodityCategoriesFromJsonFile() throws Exception {
        InputStream src = application.getAssets().open("default_commodities.json");
        FileOutputStream dest = application.openFileOutput(COMMODITIES_FILE, MODE_PRIVATE);
        copy(src, dest);

        List<Category> allCategories = commoditiesRepository.allCategories();

        assertThat(allCategories.size(), is(6));
        Category antiMalarialCategory = allCategories.get(0);
        assertThat(antiMalarialCategory.getName(), equalTo("Anti Malarials"));
        assertThat(antiMalarialCategory.getCommodities().size(), is(6));
        assertThat(antiMalarialCategory.getCommodities().get(0).getName(), equalTo("Coartem"));
    }

    @Test(expected = LmisException.class)
    public void testShouldRaiseLmisExceptionIfJsonFileDoesNotExist() throws Exception {
        commoditiesRepository.allCategories();
    }

    @Test(expected = LmisException.class)
    public void testShouldRaiseLmisExceptionIfJsonFormatIsInvalid() throws Exception {
        Writer dest = new OutputStreamWriter(application.openFileOutput(COMMODITIES_FILE, MODE_PRIVATE));
        dest.write("invalid json");

        commoditiesRepository.allCategories();
    }

    @Test
    public void testShouldSaveAllCommodityCategories() throws Exception {
        List<Category> categories = ImmutableList.of(new Category("Test Category"));
        commoditiesRepository.save(categories);

        List<Category> allCategories = commoditiesRepository.allCategories();
        assertThat(allCategories.size(), is(1));
    }
}