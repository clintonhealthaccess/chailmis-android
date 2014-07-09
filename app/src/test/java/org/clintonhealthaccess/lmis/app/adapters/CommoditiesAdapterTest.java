package org.clintonhealthaccess.lmis.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditiesAdapterTest {
    @Test
    public void shouldPopulateTextViewWithCommodityName() {
        ArrayList<Commodity> commodities = new ArrayList<>();
        String commodityName = "game";
        commodities.add(new Commodity(commodityName));
        CommoditiesAdapter adapter = new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities);

        View convertView = LayoutInflater.from(Robolectric.application).inflate(R.layout.commodity_list_item, null);
        ViewGroup parent = new LinearLayout(Robolectric.application);
        View itemAtPosition = adapter.getView(0, convertView, parent);

        TextView textViewCommodityName = (TextView) itemAtPosition.findViewById(R.id.textViewCommodityName);
        assertThat(textViewCommodityName.getText().toString(), is(commodityName));
    }

    @Test
    public void shouldCheckTheCheckBoxIfCommodityIsSelected() {
        ArrayList<Commodity> commodities = new ArrayList<>();
        Commodity commodityOne = new Commodity("game");
        commodityOne.toggleSelected();
        commodities.add(commodityOne);
        commodities.add(new Commodity("other game"));

        CommoditiesAdapter adapter = new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities);

        View convertView = LayoutInflater.from(Robolectric.application).inflate(R.layout.commodity_list_item, null);

        ViewGroup parent = new LinearLayout(Robolectric.application);

        CheckBox checkboxCommoditySelectedOne = (CheckBox) adapter.getView(0, convertView, parent).findViewById(R.id.checkboxCommoditySelected);
        CheckBox checkboxCommoditySelectedTwo = (CheckBox) adapter.getView(1, convertView, parent).findViewById(R.id.checkboxCommoditySelected);

        assertTrue(checkboxCommoditySelectedOne.isChecked());
        assertFalse(checkboxCommoditySelectedTwo.isChecked());
    }

}
