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

package org.clintonhealthaccess.lmis.app.activities.reports;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("ResourceType")
public abstract class SpinnerVisibilityStrategy {

    public int startingMonth() {
        return View.VISIBLE;
    }

    public int endingMonth() {
        return View.VISIBLE;
    }

    public int startingYear() {
        return View.VISIBLE;
    }

    public int endingYear() {
        return View.VISIBLE;
    }

    public static SpinnerVisibilityStrategy allVisible = new SpinnerVisibilityStrategy() {
    };

    public static SpinnerVisibilityStrategy startVisible = new SpinnerVisibilityStrategy() {
        @Override
        public int endingMonth() {
            return View.INVISIBLE;
        }

        @Override
        public int endingYear() {
            return View.INVISIBLE;
        }
    };

    public void applyVisibilityStrategy(Spinner spinnerStartingMonth, Spinner spinnerEndingMonth, Spinner spinnerStartingYear, Spinner spinnerEndingYear, TextView textViewEndingMonth, TextView textViewEndingYear) {
        spinnerStartingMonth.setVisibility(startingMonth());
        spinnerEndingMonth.setVisibility(endingMonth());
        spinnerStartingYear.setVisibility(startingYear());
        spinnerEndingYear.setVisibility(endingYear());
        textViewEndingMonth.setVisibility(endingMonth());
        textViewEndingYear.setVisibility(endingYear());
    }
}


