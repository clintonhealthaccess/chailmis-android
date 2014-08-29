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

import android.app.Application;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;

import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getIntFromView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getStringFromView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class ConfirmReceiveAdapterTest {

    public static final String PANADOL = "Panadol";
    public static final int QUANTITY_ALLOCATED = 3;
    public static final int QUANTITY_RECEIVED = 2;
    public static final int DIFFERENCE_QUANTITY = 1;
    private Application application;

    private ConfirmReceiveAdapter confirmReceiveAdapter;

    @Before
    public void setUp() throws Exception {
        application = Robolectric.application;
        ReceiveItem receiveItem = new ReceiveItem(new Commodity(PANADOL), QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        confirmReceiveAdapter = new ConfirmReceiveAdapter(application, R.layout.receive_confirm_list_item, Arrays.asList(receiveItem));
    }

    @Test
    public void shouldListReceiveItemsInDialog() throws Exception {

        String commodityName = getStringFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewCommodityName);
        int allocated = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityAllocated);
        int received = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityReceived);
        int difference = getIntFromView(confirmReceiveAdapter, R.layout.receive_confirm_list_item, R.id.textViewQuantityDifference);

        assertThat(commodityName, is(PANADOL));
        assertThat(allocated, is(QUANTITY_ALLOCATED));
        assertThat(received, is(QUANTITY_RECEIVED));
        assertThat(difference, is(DIFFERENCE_QUANTITY));
    }
}