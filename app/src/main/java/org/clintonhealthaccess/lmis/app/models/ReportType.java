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

import org.clintonhealthaccess.lmis.app.activities.HomeActivity;
import org.clintonhealthaccess.lmis.app.activities.reports.FacilityConsumptionReportRH1Activity;
import org.clintonhealthaccess.lmis.app.activities.reports.FacilityConsumptionReportRH2Activity;
import org.clintonhealthaccess.lmis.app.activities.reports.FacilityStockReportActivity;
import org.clintonhealthaccess.lmis.app.activities.reports.MonthlyVaccineUtilizationReportActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ReportType {

    FacilityStockReport("Facility Stock Report", FacilityStockReportActivity.class),
    FacilityRequsitionIssueReportForm("Facility Requsition, Issue and Report Form", FacilityStockReportActivity.class),
    FacilityConsumptionReportRH1("Family Planning Commodity Facility Consumption Report RH1", FacilityConsumptionReportRH1Activity.class),
    FacilityConsumptionReportRH2("Reproductory Health Commodity Facility Consumption Report RH2", FacilityConsumptionReportRH2Activity.class),
    MonthlyHealthFacilityVaccinesUtilizationReport("MONTHLY HEALTH FACILITY VACCINES UTILIZATION REPORT", MonthlyVaccineUtilizationReportActivity.class),
    MonthlyHealthFacilityDevicesUtilizationReport("MONTHLY HEALTH FACILITY DEVICES/ OTHER MATERIALS UTILIZATION REPORT", HomeActivity.class);
    public static final String VACCINE = "vaccine";
    public static final String MALARIA = "malaria";
    public static final String FAMILY = "family";


    ReportType(String name, Class reportActivity) {
        this.name = name;
        this.reportActivity = reportActivity;
    }

    private String name;
    private Class reportActivity;

    public Class getReportActivity() {
        return reportActivity;
    }

    public String getName() {
        return name;
    }

    public static List<ReportType> getReportTypesForCategory(String categoryName) {
        Map<String, List<ReportType>> stringListHashMap = new HashMap<>();
        stringListHashMap.put(VACCINE, Arrays.asList(MonthlyHealthFacilityDevicesUtilizationReport, MonthlyHealthFacilityVaccinesUtilizationReport));
        stringListHashMap.put(MALARIA, Arrays.asList(FacilityStockReport, FacilityConsumptionReportRH1));
        stringListHashMap.put(FAMILY, Arrays.asList(FacilityStockReport, FacilityConsumptionReportRH1, ReportType.FacilityRequsitionIssueReportForm, ReportType.FacilityConsumptionReportRH2));

        for (String key : stringListHashMap.keySet()) {
            if (selectTypes(categoryName, key)) return stringListHashMap.get(key);
        }
        return stringListHashMap.get(MALARIA);
    }

    private static boolean selectTypes(String categoryName, String key) {
        return categoryName.toLowerCase().contains(key);
    }
}
