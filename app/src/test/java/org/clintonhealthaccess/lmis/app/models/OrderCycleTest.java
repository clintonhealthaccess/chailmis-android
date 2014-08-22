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

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrderCycleTest {
    @Test
    public void monthlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("3/08/2012");
        Date expectedDate = sdf.parse("1/09/2012");
        assertThat(OrderCycle.Monthly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void monthlyOrderCycleShouldEndOnTheLastDayOfNextMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("22/08/2012");
        Date expectedDate = sdf.parse("30/09/2012");
        assertThat(OrderCycle.Monthly.endDate(currentDate), is(expectedDate));
    }

    @Test
    public void bimonthlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("3/08/2012");
        Date expectedDate = sdf.parse("1/09/2012");
        assertThat(OrderCycle.Bimonthly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void bimonthlyOrderCycleShouldEndOnTheLastDayOfTwoMonthsAfter() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("3/08/2012");
        Date expectedDate = sdf.parse("31/10/2012");
        assertThat(OrderCycle.Bimonthly.endDate(currentDate), is(expectedDate));
    }

    @Test
    public void quarterlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("3/08/2012");
        Date expectedDate = sdf.parse("1/09/2012");
        assertThat(OrderCycle.Quarterly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void quarterlyOrderCycleShouldEndOnTheLastDayOfFourMonthsAfter() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = sdf.parse("3/08/2012");
        Date expectedDate = sdf.parse("30/11/2012");
        assertThat(OrderCycle.Quarterly.endDate(currentDate), is(expectedDate));
    }
}