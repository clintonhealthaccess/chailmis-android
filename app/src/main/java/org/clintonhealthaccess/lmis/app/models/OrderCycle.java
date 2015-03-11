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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum OrderCycle {
    Daily(0, 0, "yyyyMMdd"),
    Monthly(1, 1, "yyyyMM"),
    BiMonthly(1, 2),
    Quarterly(1, 3),
    SixMonthly(1, 6),
    Yearly(1, 12, "yyyy");

    OrderCycle(int startMonths, int endMonths) {
        this.startMonths = startMonths;
        this.endMonths = endMonths;
    }

    OrderCycle(int startMonths, int endMonths, String format) {
        this.startMonths = startMonths;
        this.endMonths = endMonths;
        this.dateFormat = new SimpleDateFormat(format);
    }

    public OrderCycle getDefaultCycle() {
        return Daily;
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

    public String getPeriod(Date date) {
        switch (this) {
            case BiMonthly:
                return getBiMonthlyPeriod(date);
            case SixMonthly:
                return getSixMonthlyPeriod(date);
            case Quarterly:
                return getQuarterlyPeriod(date);
            default:
                return dateFormat.format(date);
        }
    }

    public Date getDate(String period) throws ParseException {
        return this.dateFormat.parse(period);
    }

    private String getBiMonthlyPeriod(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if ((calendar.get(Calendar.MONTH) + 1) % 2 == 0) {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        }
        return new SimpleDateFormat("yyyyMM").format(calendar.getTime()) + "B";
    }

    private String getSixMonthlyPeriod(Date date) {
        Calendar calendar = Calendar.getInstance();
        String number = "2";
        calendar.setTime(date);
        if (((calendar.get(Calendar.MONTH) + 1) / 6) == 0) {
            number = "1";
        }
        return new SimpleDateFormat("yyyy").format(calendar.getTime()) + "S" + number;
    }

    private String getQuarterlyPeriod(Date date) {
        String month = new SimpleDateFormat("MM").format(date);

        Map<String, String> dates = new HashMap<>();
        dates.put("01", "1");
        dates.put("02", "1");
        dates.put("03", "1");
        dates.put("04", "2");
        dates.put("05", "2");
        dates.put("06", "2");
        dates.put("07", "3");
        dates.put("08", "3");
        dates.put("09", "3");
        dates.put("10", "4");
        dates.put("11", "4");
        dates.put("12", "4");
        return new SimpleDateFormat("yyyy").format(date) + "Q" + dates.get(month);
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
    private DateFormat dateFormat;
}
