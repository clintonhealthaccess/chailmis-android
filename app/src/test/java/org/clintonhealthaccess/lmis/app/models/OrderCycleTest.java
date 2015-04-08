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

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrderCycleTest {

    private SimpleDateFormat testDateFormat;

    @Before
    public void setUp() throws Exception {
        testDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Test
    public void monthlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        Date expectedDate = testDateFormat.parse("1/09/2012");
        assertThat(OrderCycle.Monthly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void monthlyOrderCycleShouldEndOnTheLastDayOfNextMonth() throws Exception {
        Date currentDate = testDateFormat.parse("22/08/2012");
        Date expectedDate = testDateFormat.parse("30/09/2012");
        assertThat(OrderCycle.Monthly.endDate(currentDate), is(expectedDate));
    }

    @Test
    public void bimonthlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        Date expectedDate = testDateFormat.parse("1/09/2012");
        assertThat(OrderCycle.BiMonthly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void bimonthlyOrderCycleShouldEndOnTheLastDayOfTwoMonthsAfter() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        Date expectedDate = testDateFormat.parse("31/10/2012");
        assertThat(OrderCycle.BiMonthly.endDate(currentDate), is(expectedDate));
    }

    @Test
    public void quarterlyOrderCycleShouldStartFromFirstDayOfNextMonth() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        Date expectedDate = testDateFormat.parse("1/09/2012");
        assertThat(OrderCycle.Quarterly.startDate(currentDate), is(expectedDate));
    }

    @Test
    public void quarterlyOrderCycleShouldEndOnTheLastDayOfFourMonthsAfter() throws Exception {

        Date currentDate = testDateFormat.parse("3/08/2012");
        Date expectedDate = testDateFormat.parse("30/11/2012");
        assertThat(OrderCycle.Quarterly.endDate(currentDate), is(expectedDate));
    }

    @Test
    public void shouldGenerateCorrectDailyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.Daily.getPeriod(currentDate), is("20120803"));
    }

    @Test
    public void shouldGenerateCorrectMonthlyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.Monthly.getPeriod(currentDate), is("201208"));

        currentDate = testDateFormat.parse("23/01/2012");
        assertThat(OrderCycle.Monthly.getPeriod(currentDate), is("201201"));
    }

    @Test
    public void shouldGenerateCorrectBiMonthlyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.BiMonthly.getPeriod(currentDate), is("201207B"));

        currentDate = testDateFormat.parse("3/01/2012");
        assertThat(OrderCycle.BiMonthly.getPeriod(currentDate), is("201201B"));
    }


    @Test
    public void shouldGenerateCorrectQuarterlyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.Quarterly.getPeriod(currentDate), is("2012Q3"));

        currentDate = testDateFormat.parse("3/01/2012");
        assertThat(OrderCycle.Quarterly.getPeriod(currentDate), is("2012Q1"));
    }

    @Test
    public void shouldGenerateCorrectSixMonthlyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.SixMonthly.getPeriod(currentDate), is("2012S2"));

        currentDate = testDateFormat.parse("3/01/2012");
        assertThat(OrderCycle.SixMonthly.getPeriod(currentDate), is("2012S1"));
    }

    @Test
    public void shouldGenerateCorrectYearlyPeriodFormat() throws Exception {
        Date currentDate = testDateFormat.parse("3/08/2012");
        assertThat(OrderCycle.Yearly.getPeriod(currentDate), is("2012"));

        currentDate = testDateFormat.parse("3/01/2012");
        assertThat(OrderCycle.Yearly.getPeriod(currentDate), is("2012"));
    }
}