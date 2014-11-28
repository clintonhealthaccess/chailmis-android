/*
 * Copyright (c) 2014, Thoughtworks Inc
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

package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.DispenseActivity;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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
import static org.robolectric.Robolectric.setupActivity;
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
        when(mockStockService.getStockLevelFor((Commodity) anyObject())).thenReturn(10);

        commodityService.initialise(new User("test", "pass"));

        Category antiMalarialCategory = categoryService.all().get(0);
        itemSelectFragment = ItemSelectFragment.newInstance(getDispenseActivity(), antiMalarialCategory,
                new ArrayList<BaseCommodityViewModel>(), DISALLOW_CLICK_WHEN_OUT_OF_STOCK, getViewModelConverter(), "Activity");
        startFragment(itemSelectFragment);
    }

    public static DispenseActivity getDispenseActivity() {
        return setupActivity(DispenseActivity.class);
    }

    @Test
    public void testShouldRenderAllCategoryButtons() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        LinearLayout categoriesLayout = (LinearLayout) dialog.findViewById(R.id.itemSelectOverlayCategories);
        assertThat(categoriesLayout, not(nullValue()));

        assertThat(categoriesLayout.getChildCount(), is(7));
        for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
            Button button = (Button) categoriesLayout.getChildAt(i);
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
        ArrayList<BaseCommodityViewModel> commodities = new ArrayList<>();
        Commodity firstCommodity = commodityService.all().get(0);
        commodities.add(new BaseCommodityViewModel(firstCommodity));
        itemSelectFragment = ItemSelectFragment.newInstance(getDispenseActivity(), antiMalarials, commodities, DISALLOW_CLICK_WHEN_OUT_OF_STOCK, getViewModelConverter(), "Activity");
        startFragment(itemSelectFragment);

        Dialog dialog = ShadowDialog.getLatestDialog();
        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        BaseCommodityViewModel loadedCommodity = (BaseCommodityViewModel) commoditiesLayout.getAdapter().getItem(0);
        assertTrue(loadedCommodity.isSelected());
    }

    @Test
    public void testThatCheckboxIsCheckedWhenOrderingCommoditiesWithZeroStock() throws Exception {
        Category antiMalarials = categoryService.all().get(0);

        Commodity firstCommodity = commodityService.all().get(0);
        Commodity spyFirstCommodity = spy(firstCommodity);
        when(spyFirstCommodity.isOutOfStock()).thenReturn(true);

        ArrayList<BaseCommodityViewModel> currentlySelectedCommodities = new ArrayList<>();
        currentlySelectedCommodities.add(new BaseCommodityViewModel(spyFirstCommodity));

        itemSelectFragment = ItemSelectFragment.newInstance(getDispenseActivity(), antiMalarials, currentlySelectedCommodities, ALLOW_CLICK_WHEN_OUT_OF_STOCK, getViewModelConverter(), "Activity");
        startFragment(itemSelectFragment);

        Dialog dialog = ShadowDialog.getLatestDialog();
        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        BaseCommodityViewModel loadedCommodity = (BaseCommodityViewModel) commoditiesLayout.getAdapter().getItem(0);
        assertTrue(loadedCommodity.isSelected());
    }

    @Test
    public void testThatShouldNotShowSomeOfTheCommoditiesIfTheStrategyAllowsHiding() throws Exception {
        Category vaccines = categoryService.all().get(6);
        int commodityCount = vaccines.getCommodities().size();

        itemSelectFragment = ItemSelectFragment.newInstance(getDispenseActivity(), vaccines,
                new ArrayList<BaseCommodityViewModel>(), CommodityDisplayStrategy.ALLOW_ONLY_LGA_COMMODITIES,
                getViewModelConverter(), "Activity");
        startFragment(itemSelectFragment);

        Dialog dialog = ShadowDialog.getLatestDialog();
        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        int expectedCount = commodityCount - 1;
        assertThat(commoditiesLayout.getAdapter().getCount(), is(expectedCount));
    }

    @Test
    public void testCategoryButtonClickChangesTheEmptyViewText() throws Exception {
        Dialog dialog = ShadowDialog.getLatestDialog();
        TextView textViewEmptyText = (TextView) dialog.findViewById(R.id.editTextEmptyText);
        LinearLayout categoriesLayout = (LinearLayout) dialog.findViewById(R.id.itemSelectOverlayCategories);

        Button secondCategoryButton = (Button) categoriesLayout.getChildAt(1);
        secondCategoryButton.performClick();

        GridView commoditiesLayout = (GridView) dialog.findViewById(R.id.gridViewCommodities);
        assertThat(commoditiesLayout, not(nullValue()));
        assertThat(commoditiesLayout.getAdapter().getCount(), is(1));

        assertThat(secondCategoryButton.isSelected(), is(true));
        ANDROID.assertThat(textViewEmptyText).hasText("No Commodities Available");
    }

    private CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    viewModels.add(new BaseCommodityViewModel(commodity));
                }
                return viewModels;
            }
        };
    }
}