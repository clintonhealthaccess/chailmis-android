package org.clintonhealthaccess.lmis.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import org.robolectric.Robolectric;

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
}
