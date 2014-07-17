package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class ItemSelectFragmentTest {
    @Inject
    private CategoryService categoryService;
    @Inject
    private CommodityService commodityService;

    private ItemSelectFragment itemSelectFragment;
    private StockService mockStockService;

    @Before
    public void setUp() throws Exception {
        mockStockService = mock(StockService.class);
        setUpInjectionWithMockLmisServer(application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(mockStockService);
            }
        });
        when(mockStockService.getStockLevelFor((Commodity)anyObject())).thenReturn(10);

        commodityService.initialise();

        Category antiMalarialCategory = categoryService.all().get(0);
        itemSelectFragment = ItemSelectFragment.newInstance(antiMalarialCategory, new ArrayList<CommodityViewModel>(), DISALLOW_CLICK_WHEN_OUT_OF_STOCK);
        startFragment(itemSelectFragment);
    }

    @Test
    public void testShouldRenderAllCategoryButtons() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        LinearLayout categoriesLayout = (LinearLayout) dialog.findViewById(R.id.itemSelectOverlayCategories);
        assertThat(categoriesLayout, not(nullValue()));

        assertThat(categoriesLayout.getChildCount(), is(6));
        for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
            Button button = (Button)categoriesLayout.getChildAt(i);
            Category currentCategory = categoryService.all().get(i);
            assertThat(button.getText().toString(), equalTo(currentCategory.getName()));
        }
    }

    @Test
    public void testCategoryButtonClickChangesCommoditiesShowing() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        LinearLayout categoriesLayout = (LinearLayout) dialog.findViewById(R.id.itemSelectOverlayCategories);

        Button secondCategoryButton = (Button) categoriesLayout.getChildAt(1);
        secondCategoryButton.performClick();

        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        assertThat(commoditiesLayout, not(nullValue()));
        assertThat(commoditiesLayout.getAdapter().getCount(), is(1));

        assertThat(secondCategoryButton.isSelected(), is(true));
    }

    @Test
    public void testCloseButtonExists() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        Button buttonClose = (Button) dialog.findViewById(R.id.buttonClose);
        assertThat(buttonClose, not(nullValue()));
    }

    @Test
    public void testCloseButtonClosesTheDialog() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        Button buttonClose = (Button) dialog.findViewById(R.id.buttonClose);
        assertTrue(itemSelectFragment.isVisible());
        buttonClose.callOnClick();
        assertFalse(itemSelectFragment.isVisible());
    }

    @Test
    public void shouldPreCheckCommoditiesInSelectedCommoditiesListPassedByDispenseActivity() {
        Category antiMalarials = categoryService.all().get(0);
        ArrayList<CommodityViewModel> commodities = new ArrayList<>();
        Commodity firstCommodity = commodityService.all().get(0);
        commodities.add(new CommodityViewModel(firstCommodity));
        itemSelectFragment = ItemSelectFragment.newInstance(antiMalarials, commodities, DISALLOW_CLICK_WHEN_OUT_OF_STOCK);
        startFragment(itemSelectFragment);

        Dialog dialog = ShadowDialog.getLatestDialog();
        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        CommodityViewModel loadedCommodity = (CommodityViewModel)commoditiesLayout.getAdapter().getItem(0);
        assertTrue(loadedCommodity.isSelected());
    }

    @Test
    public void testThatCheckboxIsCheckedWhenOrderingCommoditiesWithZeroStock() throws Exception {
        Category antiMalarials = categoryService.all().get(0);

        Commodity firstCommodity = commodityService.all().get(0);
        Commodity spyFirstCommodity = spy(firstCommodity);
        when(spyFirstCommodity.stockIsFinished()).thenReturn(true);

        ArrayList<CommodityViewModel> currentlySelectedCommodities = new ArrayList<>();
        currentlySelectedCommodities.add(new CommodityViewModel(spyFirstCommodity));

        itemSelectFragment = ItemSelectFragment.newInstance(antiMalarials, currentlySelectedCommodities, ALLOW_CLICK_WHEN_OUT_OF_STOCK);
        startFragment(itemSelectFragment);

        Dialog dialog = ShadowDialog.getLatestDialog();
        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        CommodityViewModel loadedCommodity = (CommodityViewModel)commoditiesLayout.getAdapter().getItem(0);
        assertTrue(loadedCommodity.isSelected());
    }
}