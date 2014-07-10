package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.fragments.DispenseConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.services.CategoryService;

import java.util.Collection;
import java.util.List;

import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class DispenseActivity extends CommoditySelectableActivity {
    @Inject
    private CategoryService categoriesService;

    @InjectView(R.id.buttonSubmitDispense)
    Button buttonSubmitDispense;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dispense;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitDispense.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dispensingIsValid()) {
                    FragmentManager fm = getSupportFragmentManager();
                    DispenseConfirmationFragment dialog = DispenseConfirmationFragment.newInstance(getDispensing());
                    dialog.show(fm, "confirmDispensing");
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.dispense_submit_validation_message), LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected int getSelectedCommoditiesAdapterId() {
        return R.layout.commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<Commodity> selectedCommodities) {
        if (selectedCommodities.size() > 0) {
            buttonSubmitDispense.setVisibility(View.VISIBLE);
        } else {
            buttonSubmitDispense.setVisibility(View.INVISIBLE);
        }
    }

    // FIXME: These methods should be private. Can we find a better way to test them?
    protected boolean dispensingIsValid() {
        Collection<View> commoditiesWithoutAmount = filter(wrap(listViewSelectedCommodities), new Predicate<View>() {
            @Override
            public boolean apply(View view) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                return editTextQuantity.getText().toString().isEmpty() || editTextQuantity.getError() != null;
            }
        });
        return commoditiesWithoutAmount.size() == 0;
    }

    protected Dispensing getDispensing() {
        final Dispensing dispensing = new Dispensing();
        onEachSelectedCommodity(new SelectedCommodityHandler() {
            @Override
            public void operate(View view, Commodity commodity) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                int quantity = parseInt(editTextQuantity.getText().toString());
                dispensing.getDispensingItems().add(new DispensingItem(commodity, quantity));
            }
        });
        Log.e("DDnn", format(" dispensing items %d", dispensing.getDispensingItems().size()));
        return dispensing;
    }

    private List<View> wrap(ListView listView) {
        List<View> result = newArrayList();
        for (int i = 0; i < listView.getChildCount(); i++) {
            result.add(listView.getChildAt(i));
        }
        return result;
    }
}
