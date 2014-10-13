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

package org.clintonhealthaccess.lmis.app.models.api;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

import static java.lang.String.format;

@Getter
@Setter
@Builder
@ToString
public class DataValue {
    private static final SimpleDateFormat MONTHLY_PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMM");
    private static final SimpleDateFormat DAILY_PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    // FIXME: categoryOptionCombo and attributeOptionCombo are not used - and they shouldn't be, right?
    private String value, period, orgUnit, dataSet, dataElement, categoryOptionCombo, attributeOptionCombo, storedBy;
    private Boolean followUp;

    public Date periodAsDate() {
        try {
            return DAILY_PERIOD_DATE_FORMAT.parse(period);
        } catch (ParseException e) {
            try {
                return MONTHLY_PERIOD_DATE_FORMAT.parse(period);
            } catch (ParseException e1) {
                throw new LmisException(format("Invalid period date from DataValue [%s]", this), e);
            }
        }
    }

    public int getPeriodInt() {
        return Integer.parseInt(period);
    }
}
