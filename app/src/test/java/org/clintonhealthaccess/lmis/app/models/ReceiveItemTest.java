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

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReceiveItemTest {
    @Test
    public void shouldSelectCorrectActivity() throws Exception {
        Commodity commodity = mock(Commodity.class);
        CommodityAction activity = new CommodityAction(commodity, "12", "12", DataElementType.RECEIVED.getActivity());
        when(commodity.getCommodityActionsSaved()).thenReturn(newArrayList(activity));

        Receive receive = new Receive("LGA");
        ReceiveItem item = new ReceiveItem(commodity, 10, 20);
        item.setReceive(receive);
        assertThat(item.getActivitiesValues().get(0).getValue(), is("20"));
        assertThat(item.getActivitiesValues().get(0).getCommodityAction(), is(activity));
    }

    @Test
    public void shouldConvertFieldsToCommoditySnapshotValues() throws Exception {
        Commodity commodity = mock(Commodity.class);
        CommodityAction receivedActivity = new CommodityAction(commodity, "1", "0.05ml Syringe x 1 RECEIVED", DataElementType.RECEIVED.getActivity());
        CommodityAction receiveDateActivity = new CommodityAction(commodity, "2", "0.05ml Syringe x 1 RECEIVE_DATE", DataElementType.RECEIVE_DATE.getActivity());
        when(commodity.getCommodityActionsSaved()).thenReturn(newArrayList(receivedActivity, receiveDateActivity));

        Receive receive = new Receive("LGA");
        ReceiveItem item = new ReceiveItem(commodity, 10, 20);
        item.setReceive(receive);

        assertThat(item.getActivitiesValues().size(), is(2));
        CommoditySnapshotValue receivedValue = item.getActivitiesValues().get(0);
        assertThat(receivedValue.getCommodityAction(), is(receivedActivity));
        assertThat(receivedValue.getValue(), is("20"));

        CommoditySnapshotValue receiveDateValue = item.getActivitiesValues().get(1);
        assertThat(receiveDateValue.getCommodityAction(), is(receiveDateActivity));
        assertThat(receiveDateValue.getValue(), is(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
    }
}