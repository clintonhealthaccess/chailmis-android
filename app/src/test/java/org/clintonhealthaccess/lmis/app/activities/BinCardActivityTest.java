/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.app.activities;

import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.BinCard;
import org.clintonhealthaccess.lmis.app.models.reports.BinCardItem;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.DispensingService;
import org.clintonhealthaccess.lmis.app.services.LossService;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import java.util.Date;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.dispense;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.lose;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class BinCardActivityTest{

    @Inject
    private CommodityService commodityService;
    private UserService userService;
    private StockService stockService;
    @Inject
    private ReceiveService receiveService;
    @Inject
    private DispensingService dispensingService;
    @Inject
    private LossService lossService;
    @Inject
    private ReportsService reportsService;

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        stockService = mock(StockService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjectionWithMockLmisServer(application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(stockService);
                bind(UserService.class).toInstance(userService);
            }
        });
        commodityService.initialise(new User("test", "pass"));
    }

    public BinCardActivity getBinCardActivity() {
        return buildActivity(BinCardActivity.class).create().get();
    }

    @Test
    public void shouldEnsureSpinnerCommoditiesIsNotNull() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        assertThat(binCardActivity.spinnerCommodities, not(nullValue()));
    }

    @Test
    public void shouldEnsureSpinnerCommoditiesIsLoadedWithCommodities() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        assertThat(binCardActivity.spinnerCommodities.getAdapter(), is(notNullValue()));
        assertThat(binCardActivity.spinnerCommodities.getCount(), is(greaterThan(0)));
    }

    @Test
    public void shouldEnsureFirstCommodityNameInSpinnerIsCoartem() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        Commodity commodity = (Commodity)binCardActivity.spinnerCommodities.getSelectedItem();
        assertThat(commodity.getName(), is(commodityService.all().get(0).getName()));
    }

    @Test
    public void shouldEnsureAutoCompleteCommoditiesTextViewIsNotNull() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        assertThat(binCardActivity.autoCompleteTextViewCommodities, is(notNullValue()));
    }

    @Test
    public void shouldEnsureAutoCompleteCommoditiesTextViewContainsItems() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        AutoCompleteTextView autoCompleteTextView = binCardActivity.autoCompleteTextViewCommodities;
        assertThat(autoCompleteTextView.getAdapter().getCount(), is(greaterThan(0)));
    }

    @Test
    public void shouldSetAutoCompleteCommodityInTheSpinner() throws Exception {
        BinCardActivity binCardActivity = getBinCardActivity();
        AutoCompleteTextView autoCompleteTextView = binCardActivity.autoCompleteTextViewCommodities;
        autoCompleteTextView.setText("co");
        autoCompleteTextView.setSelection(0);
        Commodity selectedCommodity = (Commodity) autoCompleteTextView.getAdapter().getItem(0);
        assertThat((Commodity)binCardActivity.spinnerCommodities.getSelectedItem(), is(selectedCommodity));
    }
}