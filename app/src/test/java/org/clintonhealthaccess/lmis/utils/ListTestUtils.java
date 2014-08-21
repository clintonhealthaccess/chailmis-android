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

package org.clintonhealthaccess.lmis.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.robolectric.Robolectric;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class ListTestUtils {
    public static View getViewFromListRow(ArrayAdapter adapter, int row_layout, int viewId) {
        return getViewFromListRow(0, adapter, row_layout, viewId);
    }

    public static View getViewFromListRow(int index, ArrayAdapter adapter, int row_layout, int viewId) {
        View row = getRowFromListView(index, adapter, row_layout);
        return row.findViewById(viewId);
    }

    public static View getRowFromListView(ArrayAdapter adapter, int row_layout) {
        return getRowFromListView(0, adapter, row_layout);
    }

    public static View getRowFromListView(int index, ArrayAdapter adapter, int row_layout) {
        ViewGroup genericLayout = new LinearLayout(Robolectric.application);
        View convertView = LayoutInflater.from(Robolectric.application).inflate(row_layout, null);
        return adapter.getView(index, convertView, genericLayout);
    }

    public static String getStringFromView(ArrayAdapter adapter, int row_layout, int viewId) {
        return ((TextView) getViewFromListRow(adapter, row_layout, viewId)).getText().toString();
    }

    public static int getIntFromView(ArrayAdapter adapter, int row_layout, int viewId) {
        return getIntFromString(((TextView) getViewFromListRow(adapter, row_layout, viewId)).getText().toString());
    }
}
