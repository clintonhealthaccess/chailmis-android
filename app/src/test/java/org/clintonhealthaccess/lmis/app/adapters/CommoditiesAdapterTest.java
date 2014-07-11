package org.clintonhealthaccess.lmis.app.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getRowFromListView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditiesAdapterTest {

    private StockService mockStockService;

    @Before
    public void setUp() {

        mockStockService = mock(StockService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(mockStockService);
            }
        });
    }

    @Test
    public void shouldPopulateTextViewWithCommodityName() {
        ArrayList<Commodity> commodities = new ArrayList<>();
        String commodityName = "game";
        commodities.add(new Commodity(commodityName));
        CommoditiesAdapter adapter = new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities);

        TextView textViewCommodityName = (TextView) getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.textViewCommodityName);
        assertThat(textViewCommodityName.getText().toString(), is(commodityName));
    }

    @Test
    public void shouldCheckTheCheckBoxIfCommodityIsSelected() {
        fixStockLevelTo(10);
        CommoditiesAdapter adapter = makeAdapterWithCommodities();

        CheckBox checkboxCommoditySelectedOne = (CheckBox) getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        CheckBox checkboxCommoditySelectedTwo = (CheckBox) getViewFromListRow(1, adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);

        assertTrue(checkboxCommoditySelectedOne.isChecked());
        assertFalse(checkboxCommoditySelectedTwo.isChecked());
    }

    @Test
    public void shouldDisableRowViewIfCommodityStockIsZero() {
        fixStockLevelTo(0);
        CommoditiesAdapter adapter = makeAdapterWithCommodities();

        View listRow = getRowFromListView(adapter, R.layout.commodity_list_item);
//        assertThat(((ColorDrawable)listRow.getBackground()).getColor(), is(R.color.disabled));

        View checkBox = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        assertFalse(checkBox.isShown());
    }

    private void fixStockLevelTo(int level) {
        when(mockStockService.getStockLevelFor((Commodity)anyObject())).thenReturn(level);
    }

    private CommoditiesAdapter makeAdapterWithCommodities() {
        ArrayList<Commodity> commodities = new ArrayList<>();
        Commodity commodityOne = new Commodity("game");
        commodityOne.toggleSelected();
        commodities.add(commodityOne);
        commodities.add(new Commodity("other game"));

        return new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities);
    }

}
