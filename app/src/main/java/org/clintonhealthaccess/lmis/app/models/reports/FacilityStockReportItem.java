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
    private int stockOnHand, commoditiesReceived, commoditiesAdjusted,
            commoditiesLost, openingBalance, commodityAMC, commodityStockOutDays,
            commodityMaxThreshold, commodityOrderQuantity, commoditiesDispenced;

    public FacilityStockReportItem(String commodityName, int stockOnHand, int commoditiesReceived, int commoditiesAdjusted, int commoditiesLost, int openingBalance, int commodityAMC, int commodityStockOutDays,
                                   int commodityMaxThreshold, int commodityOrderQuantity, int commoditiesDispenced) {
        this.commodityName = commodityName;
        this.stockOnHand = stockOnHand;
        this.commoditiesReceived = commoditiesReceived;
        this.commoditiesAdjusted = commoditiesAdjusted;
        this.commoditiesLost = commoditiesLost;
        this.openingBalance = openingBalance;
        this.commodityAMC = commodityAMC;
        this.commodityStockOutDays = commodityStockOutDays;
        this.commodityMaxThreshold = commodityMaxThreshold;
        this.commodityOrderQuantity = commodityOrderQuantity;
        this.commoditiesDispenced = commoditiesDispenced;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public int getStockOnHand() {
        return stockOnHand;
    }

    public void setStockOnHand(int stockOnHand) {
        this.stockOnHand = stockOnHand;
    }

    public int getCommoditiesReceived() {
        return commoditiesReceived;
    }

    public void setCommoditiesReceived(int commoditiesReceived) {
        this.commoditiesReceived = commoditiesReceived;
    }

    public int getCommoditiesAdjusted() {
        return commoditiesAdjusted;
    }

    public void setCommoditiesAdjusted(int commoditiesAdjusted) {
        this.commoditiesAdjusted = commoditiesAdjusted;
    }

    public int getCommoditiesLost() {
        return commoditiesLost;
    }

    public void setCommoditiesLost(int commoditiesLost) {
        this.commoditiesLost = commoditiesLost;
    }

    public int getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(int openingBalance) {
        this.openingBalance = openingBalance;
    }

    public int getCommodityAMC() {
        return commodityAMC;
    }

    public void setCommodityAMC(int commodityAMC) {
        this.commodityAMC = commodityAMC;
    }

    public int getCommodityStockOutDays() {
        return commodityStockOutDays;
    }

    public void setCommodityStockOutDays(int commodityStockOutDays) {
        this.commodityStockOutDays = commodityStockOutDays;
    }

    public int getCommodityMaxThreshold() {
        return commodityMaxThreshold;
    }

    public void setCommodityMaxThreshold(int commodityMaxThreshold) {
        this.commodityMaxThreshold = commodityMaxThreshold;
    }

    public int getCommodityOrderQuantity() {
        return commodityOrderQuantity;
    }

    public void setCommodityOrderQuantity(int commodityOrderQuantity) {
        this.commodityOrderQuantity = commodityOrderQuantity;
    }

    public int getCommoditiesDispenced() {
        return commoditiesDispenced;
    }

    public void setCommoditiesDispenced(int commoditiesDispenced) {
        this.commoditiesDispenced = commoditiesDispenced;
    }
}
