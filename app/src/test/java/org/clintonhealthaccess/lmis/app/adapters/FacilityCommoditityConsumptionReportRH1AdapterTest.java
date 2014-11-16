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

package org.clintonhealthaccess.lmis.app.adapters;

import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.reports.ConsumptionValue;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.google.inject.internal.util.$Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class FacilityCommoditityConsumptionReportRH1AdapterTest {

    @Test
    public void shouldGenerateTextViewForEachConsumptionValueAndAddItToTheLinearLayout() throws Exception {
        ArrayList<FacilityCommodityConsumptionRH1ReportItem> reportItems = new ArrayList<>();
        FacilityCommodityConsumptionRH1ReportItem reportItem = new FacilityCommodityConsumptionRH1ReportItem(new Commodity("food"));
        reportItem.setValues(newArrayList(Arrays.asList(new ConsumptionValue(new Date(), 5), new ConsumptionValue(new Date(), 5))));
        reportItems.add(reportItem);
        FacilityCommodityConsumptionReportRH1Adapter adapter = new FacilityCommodityConsumptionReportRH1Adapter(Robolectric.application,
                R.layout.facility_commodity_consumption_report_rh1_item, reportItems);
        LinearLayout view = (LinearLayout) adapter.getView(0, null, null);
        assertThat(view.getChildCount(), is(3));
    }

    @Test
    public void shouldSetCommodityName() throws Exception {
        ArrayList<FacilityCommodityConsumptionRH1ReportItem> reportItems = new ArrayList<>();
        String commodityName = "food";
        FacilityCommodityConsumptionRH1ReportItem reportItem = new FacilityCommodityConsumptionRH1ReportItem(new Commodity(commodityName));
        reportItem.setValues(newArrayList(Arrays.asList(new ConsumptionValue(new Date(), 5), new ConsumptionValue(new Date(), 5))));
        reportItems.add(reportItem);
        FacilityCommodityConsumptionReportRH1Adapter adapter = new FacilityCommodityConsumptionReportRH1Adapter(Robolectric.application,
                R.layout.facility_commodity_consumption_report_rh1_item, reportItems);
        LinearLayout view = (LinearLayout) adapter.getView(0, null, null);
        assertThat(view.getChildCount(), is(3));

        TextView textViewName = (TextView) view.getChildAt(0);
        ANDROID.assertThat(textViewName).hasText(commodityName);

    }

    @Test
    public void shouldSetFirstConsumptionValue() throws Exception {
        ArrayList<FacilityCommodityConsumptionRH1ReportItem> reportItems = new ArrayList<>();
        String commodityName = "food";
        FacilityCommodityConsumptionRH1ReportItem reportItem = new FacilityCommodityConsumptionRH1ReportItem(new Commodity(commodityName));
        int firstConsumptionValue = 20;
        reportItem.setValues(newArrayList(Arrays.asList(new ConsumptionValue(new Date(), firstConsumptionValue), new ConsumptionValue(new Date(), 5))));
        reportItems.add(reportItem);
        FacilityCommodityConsumptionReportRH1Adapter adapter = new FacilityCommodityConsumptionReportRH1Adapter(Robolectric.application,
                R.layout.facility_commodity_consumption_report_rh1_item, reportItems);
        LinearLayout view = (LinearLayout) adapter.getView(0, null, null);
        assertThat(view.getChildCount(), is(3));

        TextView textViewName = (TextView) view.getChildAt(1);
        ANDROID.assertThat(textViewName).hasText(String.valueOf(firstConsumptionValue));

    }

    @Test
    public void shouldSetSecondConsumptionValue() throws Exception {
        ArrayList<FacilityCommodityConsumptionRH1ReportItem> reportItems = new ArrayList<>();
        String commodityName = "food";
        FacilityCommodityConsumptionRH1ReportItem reportItem = new FacilityCommodityConsumptionRH1ReportItem(new Commodity(commodityName));
        int firstConsumptionValue = 20;
        int secondConsumptionValue = 5;
        reportItem.setValues(newArrayList(Arrays.asList(new ConsumptionValue(new Date(), firstConsumptionValue), new ConsumptionValue(new Date(), secondConsumptionValue))));
        reportItems.add(reportItem);
        FacilityCommodityConsumptionReportRH1Adapter adapter = new FacilityCommodityConsumptionReportRH1Adapter(Robolectric.application,
                R.layout.facility_commodity_consumption_report_rh1_item, reportItems);
        LinearLayout view = (LinearLayout) adapter.getView(0, null, null);
        assertThat(view.getChildCount(), is(3));

        TextView textViewName = (TextView) view.getChildAt(2);
        ANDROID.assertThat(textViewName).hasText(String.valueOf(secondConsumptionValue));

    }
}