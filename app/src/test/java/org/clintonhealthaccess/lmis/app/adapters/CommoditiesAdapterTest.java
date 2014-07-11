package org.clintonhealthaccess.lmis.app.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getRowFromListView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditiesAdapterTest {


    @Before
    public void setUp() {

        setUpInjection(this);
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
        CommoditiesAdapter adapter = makeAdapterWithCommodities();

        CheckBox checkboxCommoditySelectedOne = (CheckBox) getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        CheckBox checkboxCommoditySelectedTwo = (CheckBox) getViewFromListRow(1, adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);

        assertTrue(checkboxCommoditySelectedOne.isChecked());
        assertFalse(checkboxCommoditySelectedTwo.isChecked());
    }

    @Test
    public void shouldDisableRowViewIfCommodityStockIsZero() {
        CommoditiesAdapter adapter = makeAdapterWithCommodities();

        View listRow = getRowFromListView(adapter, R.layout.commodity_list_item);
//        assertThat(((ColorDrawable)listRow.getBackground()).getColor(), is(R.color.disabled));

        View checkBox = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        assertFalse(checkBox.isShown());
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
