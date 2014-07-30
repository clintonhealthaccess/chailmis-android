package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LossesCommodityViewModelTest {

    @Test
    public void shouldSumAllLosses() {
        LossesCommodityViewModel viewModel = new LossesCommodityViewModel(new Commodity("Food"));
        viewModel.setExpiries(10);
        viewModel.setWastages(20);
        viewModel.setDamages(30);

        assertThat(viewModel.totalLosses(), is(60));

        viewModel.setMissing(2);
        assertThat(viewModel.totalLosses(), is(62));
    }
}
