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

package org.clintonhealthaccess.lmis.app.views.graphs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.clintonhealthaccess.lmis.app.R;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.BaseBarChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.BaseModel;
import org.eazegraph.lib.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class StockOnHandBarChart extends BaseBarChart {


    public StockOnHandBarChart(Context context) {
        super(context);

        mShowValues = DEF_SHOW_VALUES;

        initializeGraph();
    }


    public StockOnHandBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                org.eazegraph.lib.R.styleable.BarChart,
                0, 0
        );

        try {

            mShowValues = a.getBoolean(org.eazegraph.lib.R.styleable.BarChart_egShowValues, DEF_SHOW_VALUES);

        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        initializeGraph();
    }


    public void addBar(BarModel _Bar) {
        mData.add(_Bar);
        onDataChanged();
    }


    public void addBarList(List<BarModel> _List) {
        mData = _List;
        onDataChanged();
    }

    @Override
    public List<BarModel> getData() {
        return mData;
    }


    public void setShowValues(boolean _showValues) {
        mShowValues = _showValues;
        invalidateGlobal();
    }


    public boolean isShowValues() {
        return mShowValues;
    }


    @Override
    public void clearChart() {
        mData.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void initializeGraph() {
        super.initializeGraph();
        mData = new ArrayList<BarModel>();


        mValuePaint = new Paint(mLegendPaint);
        mLinePaint = new Paint(mLegendPaint);
        mLinePaint.setColor(getResources().getColor(R.color.white));
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        if (this.isInEditMode()) {
            addBar(new BarModel(2.3f));
            addBar(new BarModel(2.f));
            addBar(new BarModel(3.3f));
            addBar(new BarModel(1.1f));
            addBar(new BarModel(2.7f));
            addBar(new BarModel(2.3f));
            addBar(new BarModel(2.f));
            addBar(new BarModel(3.3f));
            addBar(new BarModel(1.1f));
            addBar(new BarModel(2.7f));
        }
    }


    @Override
    protected void onDataChanged() {
        calculateBarPositions(mData.size());
        super.onDataChanged();
    }

    protected void calculateBounds(float _Width, float _Margin) {
        float maxValue = 0;
        int last = 0;

        for (BarModel model : mData) {
            if (model.getValue() > maxValue) {
                maxValue = model.getValue();
            }
        }

        int valuePadding = mShowValues ? (int) mValuePaint.getTextSize() + mValueDistance : 0;

        float heightMultiplier = (mGraphHeight - valuePadding) / maxValue;

        for (BarModel model : mData) {
            float height = model.getValue() * heightMultiplier;
            last += _Margin / 2;
            model.setBarBounds(new RectF(last, mGraphHeight - height, last + _Width, mGraphHeight));
            model.setLegendBounds(new RectF(last, 0, last + _Width, mLegendHeight));
            last += _Width + (_Margin / 2);
        }

        Utils.calculateLegendInformation(mData, 0, mContentRect.width(), mLegendPaint);
    }

    /**
     * Callback method for drawing the bars in the child classes.
     *
     * @param _Canvas The canvas object of the graph view.
     */
    protected void drawBars(Canvas _Canvas) {

        for (BarModel model : mData) {
            RectF bounds = model.getBarBounds();
            mGraphPaint.setColor(model.getColor());

            _Canvas.drawRect(
                    bounds.left,
                    bounds.bottom - (bounds.height() * mRevealValue),
                    bounds.right,
                    bounds.bottom, mGraphPaint);
            // Draw minimum line

            // Draw maximum line

            // Draw stock on hand
            if (mShowValues) {
                _Canvas.drawText(Utils.getFloatString(model.getValue(), mShowDecimal), model.getLegendBounds().centerX(),
                        bounds.bottom - (bounds.height() * mRevealValue) - mValueDistance, mValuePaint);

                _Canvas.drawLine(bounds.left, bounds.height()/2, bounds.right, bounds.height()/2, mLinePaint);
            }
        }
    }

    /**
     * Returns the list of data sets which hold the information about the legend boundaries and text.
     *
     * @return List of BaseModel data sets.
     */
    @Override
    protected List<? extends BaseModel> getLegendData() {
        return mData;
    }

    @Override
    protected List<RectF> getBarBounds() {
        ArrayList<RectF> bounds = new ArrayList<RectF>();
        for (BarModel model : mData) {
            bounds.add(model.getBarBounds());
        }
        return bounds;
    }

    //##############################################################################################
    // Variables
    //##############################################################################################

    private static final String LOG_TAG = BarChart.class.getSimpleName();

    public static final boolean DEF_SHOW_VALUES = true;

    private List<BarModel> mData;

    private Paint mValuePaint;
    private Paint mLinePaint;
    protected boolean mShowValues;
    private int mValueDistance = (int) Utils.dpToPx(3);
}
