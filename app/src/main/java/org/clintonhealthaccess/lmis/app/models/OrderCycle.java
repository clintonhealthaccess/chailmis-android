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

import java.util.Calendar;
import java.util.Date;

public enum OrderCycle {
    Monthly(1, 1),
    Bimonthly(1, 2),
    Quarterly(1, 3),
    Six_monthly(1, 6),
    Yearly(1, 12);

    OrderCycle(int startMonths, int endMonths) {
        this.startMonths = startMonths;
        this.endMonths = endMonths;
    }

    public Date startDate(Date now) {
        Calendar calendar = getCalendarFromDate(now);
        calendar.add(Calendar.MONTH, startMonths);
        setDateToFirstDayOfMonth(calendar);
        return calendar.getTime();
    }

    public Date endDate(Date now) {
        Calendar calendar = getCalendarFromDate(now);
        calendar.add(Calendar.MONTH, endMonths);
        setDateToLastDayOfMonth(calendar);
        return calendar.getTime();
    }

    private static void setDateToFirstDayOfMonth(Calendar calendar) {
        int firstDayOfMonth = calendar.getActualMinimum(Calendar.DATE);
        calendar.set(Calendar.DAY_OF_MONTH, firstDayOfMonth);
    }

    private static void setDateToLastDayOfMonth(Calendar calendar) {
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
    }

    private static Calendar getCalendarFromDate(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar;
    }

    private int startMonths;
    private int endMonths;
}
