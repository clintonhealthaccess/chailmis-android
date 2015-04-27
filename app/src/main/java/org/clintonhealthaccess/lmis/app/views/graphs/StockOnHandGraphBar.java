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
import android.graphics.Typeface;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import lombok.Getter;

import static java.lang.Math.abs;

@Getter
public class StockOnHandGraphBar {
    private int actualStockOnHand;
    private String commodityName;
    private int maximumThreshold, minimumThreshold, monthsOfStockOnHand, color;
    private int heightOfColorHolder;
    private int heightForMinThreshold;
    private int heightForMaxThreshold;

    public StockOnHandGraphBar(String commodityName, int min, int max,
                               int monthsOfStockOnHand, int color, int actualStockOnHand) {
        this.commodityName = commodityName;
        this.minimumThreshold = min;
        this.maximumThreshold = max;
        this.monthsOfStockOnHand = monthsOfStockOnHand;
        this.color = color;
        this.actualStockOnHand = actualStockOnHand;
    }

    public StockOnHandGraphBar() {

    }


    public RelativeLayout getView(Context applicationContext, int biggestValue, int height) {
        int barWidth = Math.round(applicationContext.getResources().getDimension(R.dimen.bar_width));
        int barLegendHeight = 70;
        int barHeight = height - barLegendHeight - 50;

        heightForMinThreshold = getHeightForMinThreshold(barHeight, biggestValue);

        heightForMaxThreshold = getHeightForMaxThreshold(barHeight, biggestValue);

        RelativeLayout relativeLayout = getRelativeLayout(applicationContext);

        relativeLayout.addView(getSOHTextView(applicationContext, barWidth));

        relativeLayout.addView(getTextViewForCommodityName(applicationContext,
                barWidth, barLegendHeight));

        relativeLayout.addView(getColorViewHolder(applicationContext,
                biggestValue, barWidth, barHeight));

        if (heightForMaxThreshold > heightForMinThreshold) {
            ImageView imageViewSpaceBorderLeft = getImageViewForSpace(applicationContext, barWidth);
            relativeLayout.addView(imageViewSpaceBorderLeft);
            relativeLayout.addView(getMaxTextView(applicationContext, barWidth));

            if(actualStockOnHand < minimumThreshold){
                relativeLayout.addView(getMinimumOrdersTextView(applicationContext, barWidth, minimumThreshold - actualStockOnHand));
            }
        }

        return relativeLayout;
    }

    private ImageView getImageViewForSpace(Context applicationContext, int barWidth) {
        ImageView imageViewSpaceBorderLeft = new ImageView(applicationContext);
        imageViewSpaceBorderLeft.setBackgroundDrawable(applicationContext.getResources().getDrawable(R.drawable.graph_border));
        imageViewSpaceBorderLeft.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        int heightForBorder = heightForMaxThreshold - heightForMinThreshold;
        int widthForBorder = barWidth;
        RelativeLayout.LayoutParams maxParams = new RelativeLayout.LayoutParams(widthForBorder, heightForBorder);
        maxParams.setMargins(0, 0, 0, heightForMinThreshold);
        maxParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        maxParams.addRule(RelativeLayout.ABOVE, R.id.textViewCommodityNameInGraphBar);
        imageViewSpaceBorderLeft.setLayoutParams(maxParams);
        imageViewSpaceBorderLeft.setId(R.id.imageViewSpaceBorderLeft);
        return imageViewSpaceBorderLeft;
    }

    private RelativeLayout getRelativeLayout(Context applicationContext) {
        RelativeLayout relativeLayout = new RelativeLayout(applicationContext);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.setPadding(5, 5, 5, 5);
        return relativeLayout;
    }

    private TextView getSOHTextView(Context applicationContext, int barWidth) {
        TextView textViewSOH = new TextView(applicationContext);

        RelativeLayout.LayoutParams textViewSOHParams =
                new RelativeLayout.LayoutParams(barWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewSOHParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textViewSOH.setLayoutParams(textViewSOHParams);
        textViewSOH.setText(getSOH());
        textViewSOH.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewSOH.setTextSize(12);
        textViewSOH.setId(R.id.textViewStockOnHand);
        textViewSOH.setTypeface(null, Typeface.BOLD);
        textViewSOH.setTextColor(applicationContext.getResources().getColor(R.color.black));
        return textViewSOH;
    }

    private TextView getMaxTextView(Context applicationContext, int barWidth) {
        TextView textViewMax = new TextView(applicationContext);
        RelativeLayout.LayoutParams maxTextViewParams = new RelativeLayout.LayoutParams(barWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        maxTextViewParams.addRule(RelativeLayout.ABOVE, R.id.imageViewSpaceBorderLeft);

        textViewMax.setLayoutParams(maxTextViewParams);
        textViewMax.setText("Max");
        textViewMax.setTextSize(12);
        textViewMax.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewMax.setTextColor(applicationContext.getResources().getColor(R.color.black));
        textViewMax.setTypeface(null, Typeface.BOLD);
        return textViewMax;
    }


    private TextView getMinimumOrdersTextView(Context applicationContext, int barWidth, int minimunOrders){
        TextView textViewOrders = new TextView(applicationContext);
        RelativeLayout.LayoutParams ordersViewParams = new RelativeLayout.LayoutParams(barWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ordersViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imageViewSpaceBorderLeft);

        textViewOrders.setLayoutParams(ordersViewParams);
        textViewOrders.setText(Html.fromHtml("Minimum Order: </br> " + minimunOrders));
        textViewOrders.setTextSize(12);
        textViewOrders.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewOrders.setTextColor(applicationContext.getResources().getColor(R.color.black));
        textViewOrders.setTypeface(null, Typeface.BOLD);
        return textViewOrders;
    }



    private View getColorViewHolder(Context applicationContext, int biggestValue,
                                    int barWidth, int barHeight) {
        View colorHolderView = new View(applicationContext);
        heightOfColorHolder = getHeightForHolder(barHeight, biggestValue);
        RelativeLayout.LayoutParams params1 =
                new RelativeLayout.LayoutParams(barWidth, heightOfColorHolder);
        params1.addRule(RelativeLayout.ABOVE, R.id.textViewCommodityNameInGraphBar);
        colorHolderView.setLayoutParams(params1);

        int colorBrightRed = applicationContext.getResources().getColor(R.color.alerts_bright_red);
        colorHolderView.setBackgroundColor(heightOfColorHolder < heightForMinThreshold ? colorBrightRed : color);

        return colorHolderView;
    }

    private TextView getTextViewForCommodityName(Context applicationContext, int barWidth, int barLegendHeight) {
        TextView textViewName = new TextView(applicationContext);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(barWidth,
                barLegendHeight);
        params2.addRule(RelativeLayout.ABOVE, R.id.textViewStockOnHand);
        textViewName.setLayoutParams(params2);
        textViewName.setText(commodityName.toUpperCase());
        textViewName.setId(R.id.textViewCommodityNameInGraphBar);
        textViewName.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewName.setTextColor(applicationContext.getResources().getColor(R.color.black));
        textViewName.setTextSize(9);
        textViewName.setTypeface(null, Typeface.BOLD);
        return textViewName;
    }

    private String getSOH() {
        return "SOH  " + actualStockOnHand;
    }

    private int getHeightForMinThreshold(int barHeight, int biggestValue) {
        return getRelativeHeight(minimumThreshold, biggestValue, barHeight);
    }

    private int getHeightForMaxThreshold(int barHeight, int biggestValue) {
        return getRelativeHeight(maximumThreshold, biggestValue, barHeight);
    }

    private int getHeightForHolder(int barHeight, int biggestValue) {
        return getRelativeHeight(monthsOfStockOnHand, biggestValue, barHeight);
    }

    protected int getRelativeHeight(int givenValue, int maxValue, int maxHeight) {
        try {
            return (maxHeight * givenValue) / maxValue;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "StockOnHandGraphBar{" +
                "monthsOfStockOnHand=" + monthsOfStockOnHand +
                ", minimumThreshold=" + minimumThreshold +
                ", maximumThreshold=" + maximumThreshold +
                ", commodityName='" + commodityName + '\'' +
                '}';
    }
}
