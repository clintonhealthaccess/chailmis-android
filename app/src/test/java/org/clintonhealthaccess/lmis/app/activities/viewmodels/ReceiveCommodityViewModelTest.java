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

package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReceiveCommodityViewModelTest {

    public static final int QUANTITY_ALLOCATED = 4;
    public static final int QUANTITY_RECEIVED = 3;
    private static final String PANADOL = "Panadol";

    @Test
    public void shouldReturnTheDifferenceQuantity() {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity("Panadol"), QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        assertThat(viewModel.getDifference(), is(-1));
    }

    @Test
    public void shouldGenerateReceiveItem() {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL));
        viewModel.setQuantityAllocated(QUANTITY_ALLOCATED);
        viewModel.setQuantityReceived(QUANTITY_RECEIVED);

        ReceiveItem receiveItem = viewModel.getReceiveItem();

        assertThat(receiveItem.getCommodity().getName(), is(PANADOL));
        assertThat(receiveItem.getQuantityAllocated(), is(QUANTITY_ALLOCATED));
        assertThat(receiveItem.getQuantityReceived(), is(QUANTITY_RECEIVED));
    }
}