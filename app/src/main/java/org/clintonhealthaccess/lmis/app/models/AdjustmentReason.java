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

public class AdjustmentReason {
    public static final String PHYSICAL_COUNT_TEXT = "Physical Count";
    public static final String RECEIVED_FROM_ANOTHER_FACILITY_TEXT = "Received from another facility";
    public static final String SENT_TO_ANOTHER_FACILITY_TEXT = "Sent to another facility";
    public static final String SELECT_REASON = "--Select reason--";
    public static final String RETURNED_TO_LGA_TEXT = "Returned to LGA";

    public static final AdjustmentReason PHYSICAL_COUNT = new AdjustmentReason(PHYSICAL_COUNT_TEXT, true, true);
    public static final AdjustmentReason RECEIVED_FROM_ANOTHER_FACILITY = new AdjustmentReason(RECEIVED_FROM_ANOTHER_FACILITY_TEXT, true, false);
    public static final AdjustmentReason SENT_TO_ANOTHER_FACILITY = new AdjustmentReason(SENT_TO_ANOTHER_FACILITY_TEXT, false, true);
    public static final AdjustmentReason RETURNED_TO_LGA = new AdjustmentReason(RETURNED_TO_LGA_TEXT, false, true);
    private String name;
    private boolean allowsPostive, allowsNegative;

    public AdjustmentReason(String name, boolean allowsPostive, boolean allowsNegative) {
        this.name = name;
        this.allowsPostive = allowsPostive;
        this.allowsNegative = allowsNegative;
    }

    public boolean allowsBoth() {
        return allowsNegative && allowsPostive;
    }

    public String getName() {
        return name;
    }

    public boolean allowsPostive() {
        return allowsPostive;
    }

    public boolean allowsNegative() {
        return allowsNegative;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdjustmentReason)) return false;

        AdjustmentReason that = (AdjustmentReason) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isPhysicalCount() {
        return name.equalsIgnoreCase(PHYSICAL_COUNT_TEXT);
    }

    public boolean isSentToAnotherFacility() {
        return name.equalsIgnoreCase(SENT_TO_ANOTHER_FACILITY_TEXT);
    }


}
