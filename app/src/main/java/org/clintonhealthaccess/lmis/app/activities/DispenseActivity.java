package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.base.Predicate;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.fragments.DispenseConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;

import java.util.Collection;
import java.util.List;

import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class DispenseActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitDispense)
    Button buttonSubmitDispense;

    @InjectView(R.id.checkboxDispenseToFacility)
    CheckBox checkboxCommoditySelected;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dispense;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitDispense.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!dispensingItemsHaveNoZeroQuantities()) {
                            showToastMessage(getString(R.string.dispense_submit_validation_message_zero));
                            return;
                        }
                        if (!dispensingItemsHaveValidQuantities()) {
                            showToastMessage(getString(R.string.dispense_submit_validation_message_filled));
                            return;
                        }

                        if (!dispensingItemsHaveNoErrors()) {
                            showToastMessage(getString(R.string.dispense_submit_validation_message_errors));
                            return;
                        }

                        FragmentManager fm = getSupportFragmentManager();
                        DispenseConfirmationFragment dialog = DispenseConfirmationFragment.newInstance(getDispensing());
                        dialog.show(fm, "confirmDispensing");
                    }

                }
        );
    }


    @Override
    protected int getSelectedCommoditiesAdapterId() {
        return R.layout.commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<CommodityViewModel> selectedCommodities) {
        if (selectedCommodities.size() > 0) {
            buttonSubmitDispense.setVisibility(View.VISIBLE);
        } else {
            buttonSubmitDispense.setVisibility(View.INVISIBLE);
        }
    }

    private boolean dispensingItemsHaveValidQuantities() {
        Collection<View> commoditiesWithoutAmount = filter(wrap(listViewSelectedCommodities), new Predicate<View>() {
            @Override
            public boolean apply(View view) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                return editTextQuantity.getText().toString().isEmpty();
            }
        });
        return commoditiesWithoutAmount.size() == 0;
    }

    private boolean dispensingItemsHaveNoZeroQuantities() {
        Collection<View> commoditiesWithoutAmount = filter(wrap(listViewSelectedCommodities), new Predicate<View>() {
            @Override
            public boolean apply(View view) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                return editTextHasNumberLessThanEqualToZero(editTextQuantity);
            }
        });
        return commoditiesWithoutAmount.size() == 0;
    }

    private boolean editTextHasNumberLessThanEqualToZero(EditText editTextQuantity) {
        try {
            return Integer.parseInt(editTextQuantity.getText().toString()) <= 0;
        } catch (NumberFormatException ex) {
            return false;
        }

    }

    private boolean dispensingItemsHaveNoErrors() {
        Collection<View> commoditiesWithoutAmount = filter(wrap(listViewSelectedCommodities), new Predicate<View>() {
            @Override
            public boolean apply(View view) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                return editTextQuantity.getError() != null;
            }
        });
        return commoditiesWithoutAmount.size() == 0;
    }

    protected Dispensing getDispensing() {
        final Dispensing dispensing = new Dispensing();
        dispensing.setDispenseToFacility(checkboxCommoditySelected.isChecked());
        onEachSelectedCommodity(new SelectedCommodityHandler() {
            @Override
            public void operate(View view, CommodityViewModel commodityViewModel) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                int quantity = parseInt(editTextQuantity.getText().toString());
                dispensing.getDispensingItems().add(new DispensingItem(commodityViewModel.getCommodity(), quantity));
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
