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

package org.clintonhealthaccess.lmis.app.models.reports;

/**
 * Created by jafari on 10/20/14.
 */
public class FacilityConsumptionReportRH2Item {
    private String commodityName;
    private int openingStock;
    private int commoditiesReceived;
    private int commoditiesDispensedToClients;
    private int commoditiesDispensedToFacilities;
    private int commoditiesAdjusted;
    private int commoditiesLost;
    private int closingStock;

    public FacilityConsumptionReportRH2Item(String commodityName, int openingStock, int commoditiesReceived,
                                   int commoditiesDispensedToClients, int commoditiesAdjusted, int commoditiesDispensedToFacilities,
                                   int commoditiesLost, int closingStock) {
        this.commodityName = commodityName;
        this.openingStock = openingStock;
        this.commoditiesReceived = commoditiesReceived;
        this.commoditiesDispensedToClients = commoditiesDispensedToClients;
        this.commoditiesAdjusted = commoditiesAdjusted;
        this.commoditiesDispensedToFacilities = commoditiesDispensedToFacilities;
        this.commoditiesLost = commoditiesLost;
        this.closingStock = closingStock;
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

    public int getCommoditiesLost() {
        return commoditiesLost;
    }

    public int getCommoditiesDispensedToFacilities() {
        return commoditiesDispensedToFacilities;
    }

    public int getClosingStock() {
        return closingStock;
    }

    public int getCommoditiesDispensedToClients() {
        return commoditiesDispensedToClients;
    }

    public int getCommoditiesAdjusted(){
        return commoditiesAdjusted;
    }

    public int totalDispensed() {
        return commoditiesDispensedToClients + commoditiesDispensedToFacilities;
    }
}
