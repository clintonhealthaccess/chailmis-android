#
# Copyright (c) 2014, University of Oslo
# All rights reserved.
#
#Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer.
#
# Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
# Neither the name of the HISP project nor the names of its contributors may
# be used to endorse or promote products derived from this software without
# specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
###################################################################################
#This script should be once a month. It calculates the 
#average monthly consumption based on the previous three months. 
#This script is going to rewind to the previous month, so if it is executed on the 
#1st of April, then it will calculate average monthly consumption 
#Based on Jan, Feb and Mar
###################################################################################

require(RCurl)
require(httr)
require(rjson)
require(RPostgreSQL)
require(lubridate)
require(Hmisc)
require(XML)

source("globalConfig.properties")
ou.level<-4

m<-dbDriver("PostgreSQL")
con <- dbConnect(PostgreSQL(), user= dbUserName, password=dbPassword,host=dbHost, port=dbPort,dbname=dbName)

#The period of interest refers to the month which we want to generate the MSB for
#This is based on the previous MSB from the month before. 
cmd_args<-commandArgs();
#TODO make this more robust
reportDate<-as.Date(cmd_args[6])
#reportDate<-as.Date(cmd_args[6])
if ( is.na(reportDate) ) {reportDate<-as.Date(Sys.date())}

#Return the previous POI, based on the current one which we are calculating the 
#MSB for. If the POI is 201408, the previous POI would be 201407

getLastThreeMonthlyPeriods<-function(reportDate) {
  foo<-floor_date(reportDate,"month")-months(1:3)
  paste0(year(foo),sprintf("%02d",month(foo)))
}

getCurrentMonthPeriod<-function(this.date=now()) {
  require(lubridate)
  foo<-paste0(year(this.date),sprintf("%02d",month(this.date)))
  return(foo)
}

#A function which determine which analytics tables we need. 
#This could span over multiple years for Nov and December
getAnalyticsTablesName<-function(reportDate,con) {
  foo<-floor_date(reportDate,"month")-months(1:3)
  all.years<-paste0("analytics_",unique(year(foo)))
  #Need to check and see if these table actually exist
  sql<-paste0("SELECT table_name FROM information_schema.tables WHERE table_schema='public'
             and table_name IN ('",paste(all.years,sep="",collapse="','"),"');")
  foo<-dbGetQuery(con,sql)
  all.years<-all.years[all.years %in% foo$table_name]
  return(all.years)
}
#Safely handle NAs in sums
sumSafe<-function(x) {
  foo<-sum(x,na.rm=TRUE)
  if (is.na(foo)) { foo<-0 }
  return(foo)
}

#Name of the analytics table to access
analytics.tables.name<-getAnalyticsTablesName(reportDate,con)
#Current monthly period
poi.current<-getCurrentMonthPeriod(reportDate)

#Get the All commodities UID
required.types<-data.frame(type=c('DISPENSED','EXPIRED',
                                  'WASTED','MISSING',
                                  'RECEIVED', 'ADJUSTMENTS',
                                  'FROZEN','LABEL_REMOVED','BREAKAGE','OTHERS','VVM_CHANGE'),
                           factor=c(-1,-1,-1,-1,1,1,-1,-1,-1,-1,-1))


all.commodities.uid<-dbGetQuery(con,"SELECT uid from dataelementgroupset where name = 'PHC Facility commodities';")$uid
lmis.type.uid<-dbGetQuery(con,"SELECT uid from dataelementgroupset where name = 'LMIS Activity Type';")$uid
last.three.months<-getLastThreeMonthlyPeriods(reportDate)
sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
            degs."PHC Facility commodities" as commodity,degs."',all.commodities.uid,'"  as commodities_uid ,
            degs."LMIS Activity Type" as type,degs."',lmis.type.uid,'" 
            as lmis_type_uid from _dataelementgroupsetstructure degs
            INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
            where "PHC Facility commodities" IS NOT NULL
            AND "LMIS Activity Type" IN (\'DISPENSED\',\'NUMBER_OF_STOCK_OUT_DAYS\')
            ORDER BY commodity,type;')
des<-dbGetQuery(con,sql)

#Need to get the dispensed, group by month for the last three month
getMonthlyDispensed<-function(reportDate,con) {
  sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
            degs."PHC Facility commodities" as commodity,degs."',all.commodities.uid,'"  as commodities_uid ,
            degs."LMIS Activity Type" as type,degs."',lmis.type.uid,'" 
            as lmis_type_uid from _dataelementgroupsetstructure degs
            INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
            where "PHC Facility commodities" IS NOT NULL
            AND "LMIS Activity Type" IN (\'DISPENSED\')
            ORDER BY commodity,type;')
  disp.des<-dbGetQuery(con,sql)
  periods<-data.frame(period=getLastThreeMonthlyPeriods(reportDate))
  periods$year<-substr(periods$period,0,4)
  periods$table_name<-paste0("analytics_",periods$year)
  analytics.tables<-getAnalyticsTablesName(reportDate,con)
  periods<-periods[periods$table_name %in% analytics.tables,]
  this.years<-unique(periods$year)
  sql<-""
  for (i in 1:length(this.years)) {
    this.year.periods<-periods[periods$year == this.years[i],]
    this.analytics.table<-this.year.periods$table_name[1]
  this.sql<-paste0("SELECT de as de_uid,uidlevel",ou.level," as ou_uid, monthly ,sum(value) as value from ",this.analytics.table," 
         where de in ('",paste(disp.des$de_uid,sep="",collapse="','"),"') 
         and monthly IN('",paste(this.year.periods$period,sep="",collapse="','"),"') 
         GROUP BY de_uid,monthly,uidlevel",ou.level)
  sql<-paste(sql,"\n",this.sql)
  if (  i ==1 & length(this.years) > 1 ) { sql<-paste(sql,"\n UNION \n")}  }
  return(dbGetQuery(con,sql))
  
}

getMonthlySOD<-function(reportDate,con) {
  sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
            degs."PHC Facility commodities" as commodity,degs."',all.commodities.uid,'"  as commodities_uid ,
              degs."LMIS Activity Type" as type,degs."',lmis.type.uid,'" 
              as lmis_type_uid from _dataelementgroupsetstructure degs
              INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
              where "PHC Facility commodities" IS NOT NULL
              AND "LMIS Activity Type" IN (\'NUMBER_OF_STOCK_OUT_DAYS\')
            ORDER BY commodity,type;')
  disp.des<-dbGetQuery(con,sql)
  periods<-data.frame(period=getLastThreeMonthlyPeriods(reportDate))
  periods$year<-substr(periods$period,0,4)
  periods$table_name<-paste0("analytics_",periods$year)
  analytics.tables<-getAnalyticsTablesName(reportDate,con)
  periods<-periods[periods$table_name %in% analytics.tables,]
  this.years<-unique(periods$year)
  sql<-""
  for (i in 1:length(this.years)) {
    this.year.periods<-periods[periods$year == this.years[i],]
    this.analytics.table<-this.year.periods$table_name[1]
    this.sql<-paste0("SELECT de as de_uid,uidlevel",ou.level," as ou_uid ,monthly,MAX(value) as value from ",this.analytics.table," 
         where de in ('",paste(disp.des$de_uid,sep="",collapse="','"),"') 
         and monthly IN('",paste(this.year.periods$period,sep="",collapse="','"),"') 
         GROUP BY de_uid,monthly,uidlevel",ou.level)
    sql<-paste(sql,"\n",this.sql)
    if (   length(this.years) > 1 & i<length(this.years)) {sql<-paste(sql,"\n UNION \n")}  }
  return(dbGetQuery(con,sql))
  
}



disp<-getMonthlyDispensed(reportDate,con)
disp<-merge(disp,des[,c("de_uid","type","commodities_uid")],by.x="de_uid",by.y="de_uid")
disp<-disp[,c("ou_uid","commodities_uid","monthly","value")]
names(disp)[names(disp) == "value"]<-"disp"
sod<-getMonthlySOD(reportDate,con)
sod<-merge(sod,des[,c("de_uid","commodities_uid")],by.x="de_uid",by.y="de_uid")
sod<-sod[,c("ou_uid","commodities_uid","monthly","value")]
names(sod)[names(sod) == "value"]<-"sod"

d<-merge(disp,sod,by=c("commodities_uid","ou_uid","monthly"),all.x=T)
if (nrow(d) > 0 ) {
d$sod[is.na(d$sod)]<-0
#Number of days in the month
d$monthDays<-monthDays(ymd(paste0(d$monthly,"01")))
d$actualDays<-d$monthDays-d$sod
d$value<-d$disp*(d$monthDays/d$actualDays)
#Exclude any thing which has a stockout greater than the SOD threshold
sod.threshold<-15
d<-d[d$sod < sod.threshold,]
#Calculate the mean
d.agg<-aggregate(value ~ commodities_uid + ou_uid,data=d,mean)
#We need to remap the data element UIDs
sql<-paste0('SELECT DISTINCT de.uid as dataelement_uid, degs."',all.commodities.uid,'"  as commodities_uid
            from _dataelementgroupsetstructure degs
            INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
            WHERE degs."LMIS Activity Type" = (\'AMC\');')
amc.des<-dbGetQuery(con,sql)
amc.des<-amc.des[complete.cases(amc.des),]
#Remap the aggregate data
d.agg<-merge(d.agg,amc.des,by="commodities_uid")

foo<-paste0('<dataValueSet xmlns="http://dhis2.org/schema/dxf/2.0">',
            paste('<dataValue dataElement="',as.character(d.agg$dataelement_uid),'" period="',
                  as.character(getCurrentMonthPeriod(reportDate)),'" orgUnit="',
                  as.character(d.agg$ou_uid),'" value="',as.character(round(d.agg$value,0)),'"/>',sep="",collapse=""),"</dataValueSet>")
cat(foo,file="foodata.xml")
h = basicTextGatherer()
curlPerform(url=paste0(base.url,"api/dataValueSets"),userpwd=paste0(username,":",password),
            httpauth = 1L,
            httpheader=c(Accept="application/xml", Accept="multipart/*", 'Content-Type' = "application/xml"),
            writefunction = h$update,
            postfields= foo) 

#We are going to post this back as a system notifcation
payload<-list(subject="Result of AMC update",text=saveXML(xmlParse(h$value())))

url<-paste0(base.url,"api/email/notification")

curlPerform(url=url,userpwd=paste0(username,":",password),
            httpauth = 1L,
            httpheader=c(Accept="application/json", Accept="multipart/*", 'Content-Type' = "application/json"),
            postfields= toJSON(payload),
            verbose = verbose )

}
