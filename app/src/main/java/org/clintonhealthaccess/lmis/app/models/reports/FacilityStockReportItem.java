/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.app.models.reports;

public class FacilityStockReportItem {
    private String commodityName;
    private int openingStock, commoditiesReceived, commoditiesAdjusted,
            commoditiesLost, commodityAMC, commodityStockOutDays,
            commodityMaxThreshold, commodityOrderQuantity, commoditiesDispensed, stockOnHand;

    public FacilityStockReportItem(String commodityName, int openingStock, int commoditiesReceived,
                                   int commoditiesAdjusted, int commoditiesLost, int commodityAMC,
                                   int commodityStockOutDays,
                                   int commodityMaxThreshold, int commodityOrderQuantity, int commoditiesDispensed, int stockOnHand) {
        this.commodityName = commodityName;
        this.openingStock = openingStock;
        this.commoditiesReceived = commoditiesReceived;
        this.commoditiesAdjusted = commoditiesAdjusted;
        this.commoditiesLost = commoditiesLost;
        this.commodityAMC = commodityAMC;
        this.commodityStockOutDays = commodityStockOutDays;
        this.commodityMaxThreshold = commodityMaxThreshold;
        this.commodityOrderQuantity = commodityOrderQuantity;
        this.commoditiesDispensed = commoditiesDispensed;
        this.stockOnHand = stockOnHand;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public int getOpeningStock() {
        return openingStock;
    }

    public int getCommoditiesReceived() {
        return commoditiesReceived;
    }

    public int getCommoditiesAdjusted() {
        return commoditiesAdjusted;
    }

    public int getCommoditiesLost() {
        return commoditiesLost;
    }

    public int getCommodityAMC() {
        return commodityAMC;
    }

    public int getCommodityStockOutDays() {
        return commodityStockOutDays;
    }

    public int getCommodityMaxThreshold() {
        return commodityMaxThreshold;
    }

    public int getCommodityOrderQuantity() {
        return commodityOrderQuantity;
    }

    public int getCommoditiesDispensed() {
        return commoditiesDispensed;
    }

    public int getStockOnHand() {
        return stockOnHand;
    }
}
