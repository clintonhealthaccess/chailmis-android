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

package org.clintonhealthaccess.lmis.app.models;

import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class AdjustmentTest {

    @Test
    public void shouldAddToStockOnHandIfPositive() {
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(10);
        Adjustment adjustment = new Adjustment(commodity, 20, true, "Count");
        assertThat(adjustment.getNewStockOnHand(), is(30));

    }

    @Test
    public void shouldSubtractFromStockOnHandIfNegative() {
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(20);
        Adjustment adjustment = new Adjustment(commodity, 10, false, "Count");
        assertThat(adjustment.getNewStockOnHand(), is(10));

    }

    @Test
    public void shouldReturnPlusIfPositive() throws Exception {
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(20);
        Adjustment adjustment = new Adjustment(commodity, 10, true, "Count");
        assertThat(adjustment.getType(), is("+"));

    }

    @Test
    public void shouldReturnMinusIfNotPositive() throws Exception {
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(20);
        Adjustment adjustment = new Adjustment(commodity, 10, false, "Count");
        assertThat(adjustment.getType(), is("-"));

    }

    @Test
    public void shouldSelectCorrectCommodityActions() throws Exception {
        Commodity commodity = mock(Commodity.class);
        CommodityAction adjustmentReasonsAction = new CommodityAction(commodity, "12", "12", DataElementType.ADJUSTMENT_REASON.toString());
        CommodityAction adjustmentAmountAction = new CommodityAction(commodity, "12", "12", DataElementType.ADJUSTMENTS.toString());
        when(commodity.getCommodityAction(DataElementType.ADJUSTMENTS.toString())).thenReturn(adjustmentAmountAction);
        when(commodity.getCommodityAction(DataElementType.ADJUSTMENT_REASON.toString())).thenReturn(adjustmentReasonsAction);
        Adjustment adjustment = new Adjustment(commodity, 10, true, "Count");
        assertThat(adjustment.getActivitiesValues().size(), is(2));
        assertThat(adjustment.getActivitiesValues().get(0).getValue(), is("10"));
        assertThat(adjustment.getActivitiesValues().get(1).getValue(), is("Count"));
        assertThat(adjustment.getActivitiesValues().get(0).getCommodityAction(), is(notNullValue()));
        assertThat(adjustment.getActivitiesValues().get(1).getCommodityAction(), is(notNullValue()));

    }
}