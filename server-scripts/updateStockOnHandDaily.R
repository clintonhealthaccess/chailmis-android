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
#This script should be run daily after midnight, the latest analytics run.
#The script should accept a single parameter, which is the reportDate, or
#If left blank, will default to the current ex
#Stock on hand values are calculated numDays back in time.
#Stock on hand can be negative in this calculation.
#Stock out days are then calculated from the resulting set of data.
#Zero stock out days are excluded from the import.
#The basic approach is that we retrieve all data values which could contribute to 
#Stock movement. The script looks back a number of days to get the initial 
#Stock on hand value, and then retrieves the stock movement to the initial value
#for each day, commodity and organisationunit, Values are then posted back 
#to the dataValueSet API endpoint for import.
###################################################################################
require(RCurl)
require(httr)
require(rjson)
require(RPostgreSQL)
require(lubridate)
require(dplyr)
require(XML)

#Start date for the calculation is going to be numDays in the past
getStartDate<-function(this.date=now()) { 
  this.date-days(numDays)
}

#Construct a daily time series from the startdate to the report date
getDailyTimeSeries<-function(this.date=now()) {
  foo<-seq(as.Date(getStartDate(this.date)),as.Date(this.date),by="days") 
  data.frame(startdate=foo,daily=gsub("-","",as.character(foo)))
}

#A function which determine which analytics table we need, based on the current POI (period of interest)
#Note that this could span multiple analytics tables, but should tolerate analytics tables which do not 
#exist but are needed
#Expects an ISO month string as input, i.e. 201407
getAnalyticsTablesName<-function(this.date=reportDate,con) {
  all.years<-paste0("analytics_",unique(substring(getDailyTimeSeries(this.date)$daily,0,4)))
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

#Self destruct function
halt <- function(hint = "Process stopped.\n") {
  writeLines(hint)
  require(tools, quietly = TRUE)
  processId <- Sys.getpid() 
  pskill(processId, SIGINT)
  iddleTime <- 1.00
  Sys.sleep(iddleTime)
}




#A static map of commodity types
#and their factor contribution to the stock on hand for facility level

getConfig<-function(config,con){
  
#Facility level
#Get all the commodities from the 'All commodities' group set
config$CommoditiesGroupUID<-dbGetQuery(con,paste0("SELECT uid from dataelementgroupset where name = '",config$CommoditiesGroupName,"';"))$uid
#Get the LMIS type from the 'LMIS Activity type' group set
config$ActivitiesGroupUID<-dbGetQuery(con,paste0("SELECT uid from dataelementgroupset where name = '",config$ActivityGroupName,"';"))$uid
#Go ahead and get the map of data elements  for this group set
sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
            degs."',config$CommoditiesGroupName,'" as commodity,degs."',config$CommoditiesGroupUID,'"  as commodities_uid ,
            degs."',config$ActivityGroupName,'" as type,degs."',config$ActivitiesGroupUID,'" 
            as lmis_type_uid from _dataelementgroupsetstructure degs
            INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
            WHERE degs."',config$CommoditiesGroupName, '" IS NOT NULL
            AND degs."',config$ActivityGroupName,'" IN (\'',paste(config$type,sep="",collapse="','"),"')
            and degs.dataelementname !~(\'MONTHLY_DISPENSED\')
            ORDER BY commodity,type;")
all.commodities.des<-dbGetQuery(con,sql)
foo<-as.data.frame(cbind(config$type,config$factor))
names(foo)<-c("type","factor")
config$all.commodities.des<-merge(all.commodities.des,foo,by="type")
#Stock on hand
sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
              degs."',config$CommoditiesGroupName,'" as commodity,degs."',config$CommoditiesGroupUID,'"  as commodities_uid ,
              degs."',config$ActivityGroupName,'" as type,degs."',config$ActivitiesGroupUID,'" 
              as lmis_type_uid from _dataelementgroupsetstructure degs
              INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
              where "',config$CommoditiesGroupName,'" IS NOT NULL
              AND "',config$ActivityGroupName,'" = \'',config$soh.name,"\'
              ORDER BY commodity,type;")

config$soh.des<-dbGetQuery(con,sql) 

return(config)
}


#This function will get all stock movement at a desired OU level
#from the reportDate
getStockMovement<-function(reportDate,config,con) {
  analytics.tables<-getAnalyticsTablesName(reportDate,con)
  sql<-""
  for (i in 1:length(analytics.tables)) {
    sql<-paste0(sql," SELECT de as de_uid, daily, uidlevel",config$ou.level," as ou_uid, value as value from ",analytics.tables[i]," 
                where de in ('",paste(config$all.commodities.des[,c("de_uid")],sep="",collapse="','"),"') 
                and daily in ( '", paste(getDailyTimeSeries(reportDate)$daily,sep="",collapse="','"),"')
                ")
    if ( i < length(analytics.tables) ) { sql<-paste(sql,"\n UNION \n")}  }
  sql<-paste0(sql ," ORDER BY ou_uid,de_uid,daily")
  d<-dbGetQuery(con,sql)
  d<-merge(d,config$all.commodities.des[,c("de_uid","commodities_uid","type","factor")],by="de_uid")
  d$value<-d$value * as.numeric(as.character(d$factor))
  d<-aggregate(value ~ ou_uid + commodities_uid + daily, data=d,FUN=sumSafe)
  return(d) 
}


#Function to calculate the stock on hand
getSOH<-function(reportDate,config,con) {
  analytics.tables<-getAnalyticsTablesName(reportDate,con)
  sql<-""
  for (i in 1:length(analytics.tables)) {
    sql<-paste0(sql, "SELECT de as de_uid, daily, uidlevel",config$ou.level," as ou_uid, value as value from ",analytics.tables[i]," 
                where de in ('",paste(config$soh.des[,c("de_uid")],sep="",collapse="','"),"') 
                and daily in ( '", paste(getDailyTimeSeries(reportDate)$daily,sep="",collapse="','"),"')")
    if ( i< length(analytics.tables) ) { sql<-paste(sql,"\n UNION \n")}
  }
  #All of the stock on hand values for the period of interest
  soh.init<-dbGetQuery(con,sql)
  #Handle the case if their is no data
  if( nrow(soh.init) == 0 ) {soh.init<-data.frame(commodities_uid=character(),
                                                  ou_uid=character(),
                                                  daily=character(),
                                                  value = numeric(),stringsAsFactors=F)} else {
                                                    soh.init<-merge(soh.init,config$soh.des[,c("de_uid","commodities_uid")],by="de_uid")
                                                    #Get rid  of the data element UID, we no longer need it
                                                    soh.init<-select(soh.init,-de_uid)
                                                    #We need the first observed stock on hand value per commodity and orgunit
                                                    soh.init<-soh.init[soh.init$daily == gsub("-","",sort(getDailyTimeSeries(reportDate)$startdate)[1]),]}
  #This gives us the SOH on the first day of this interval. 
  #Get the stock movement over this time period
  sm<-getStockMovement(reportDate,config,con)
  #Bind these together. This is the total stock movement, along with the initial start balance
  d<-rbind(soh.init,sm)
  d<-arrange(d,ou_uid,commodities_uid,daily)
  
  #This data frame must be padded. Either from the initial stock movement
  #or from the initial stock on hand
  foo<-data.frame(daily=getDailyTimeSeries(reportDate)$daily)
  #This is not efficient. Should be from the initial stock movement date
  foo<-merge(foo,data.frame(ou_uid=unique(d$ou_uid)),all=T)
  #This is not efficient. Should only be for commodities which have moved
  foo<-merge(foo,data.frame(commodities_uid=unique(d$commodities_uid)),all=T)
  foo$value<-0
  
  foo<-merge(d,foo,by=c("daily","commodities_uid","ou_uid"),all.y=T)
  foo$value.x[is.na(foo$value.x)]<-0
  foo$value<-foo$value.x+foo$value.y
  foo<-select(foo,daily,commodities_uid,ou_uid,value)
  foo<-arrange(foo,ou_uid,commodities_uid,daily)
  foo<-transform(foo, value=ave(value,ou_uid, commodities_uid, FUN=cumsum))
  
  #Now we need to deal with the situation where there was no initial 
  #Stock on hand, but there was stock movement after a certain date
  sm.init<-d[!duplicated(d[,c("ou_uid","commodities_uid")], fromLast=F),c("ou_uid","commodities_uid","daily")]
  foo<-merge(foo,sm.init,by=c("commodities_uid","ou_uid"),all.x=T)
  foo$isActive<-as.Date(foo$daily.x,"%Y%m%d") >= as.Date(foo$daily.y,"%Y%m%d")
  foo<-foo[foo$isActive,]
  foo$daily<-gsub("-","",as.Date(foo$daily.x,"%Y%m%d") + days(1))
  foo<-foo[complete.cases(foo),]
  foo<-arrange(foo,ou_uid,commodities_uid,daily)
  foo<-merge(foo,config$soh.des[,c("commodities_uid","de_uid")],by="commodities_uid")
  foo<-foo[,c("ou_uid","commodities_uid","de_uid","daily","value")]
  if (config$stockout) {
  #We are going to calculate the stock out days from the Stock on hand values 
  so<-transform(foo[c("commodities_uid","ou_uid","daily","value")],value=value<=0)
  #Stock out days are reset at the beginning of the month
  so<-transform(so,monthly=substring(so$daily,0,6))
  so<-arrange(so,ou_uid,commodities_uid,monthly,daily)
  #Calculate the cumulative stock out days
  so<-transform(so, value=ave(value,ou_uid, commodities_uid,monthly, FUN=cumsum))
  #We only care about non-zero stock out days
  so<-so[so$value > 0 & !is.na(so$value),]
  #Need to remap the SOH to stock out days
  sql<-paste0('SELECT DISTINCT de.uid as de_uid,degs.dataelementid,degs.dataelementname,
              degs."',config$CommoditiesGroupName,'" as commodity,degs."',config$CommoditiesGroupUID,'"  as commodities_uid ,
              degs."',config$ActivityGroupName,'" as type,degs."',config$ActivitiesGroupUID,'" 
              as lmis_type_uid from _dataelementgroupsetstructure degs
              INNER JOIN dataelement de on degs.dataelementid = de.dataelementid
              where "',config$CommoditiesGroupName,'" IS NOT NULL
              and dataelementname ~(\'STOCK_OUT_DAYS\')
              ORDER BY commodity,type;')
  so.des<-dbGetQuery(con,sql)
  so<-merge(so,so.des[,c("commodities_uid","de_uid")],by="commodities_uid")
  #Get the columns which are required to merge with the stock on hand data elements
  so<-so[,names(foo)]
  #Row bind the Stock on hand and Stock out days
  foo<-rbind(foo,so)
  
  }
  
  
  return(foo[,c("de_uid","ou_uid","daily","value")])
}


postPayload<-function(pl,username,password) {
 write.csv(pl,file="payload.csv")
   
  #Post this payload back to the API for import
  pl<-paste0('<dataValueSet xmlns="http://dhis2.org/schema/dxf/2.0">',
              paste('<dataValue dataElement="',as.character(pl$de_uid),'" period="',
                    as.character(pl$daily),'" orgUnit="',
                    as.character(pl$ou_uid),'" value="',as.character(pl$value),'"/>',sep="",collapse=""),"</dataValueSet>")
  
  h = basicTextGatherer()
ptm <- proc.time()
  curlPerform(url=paste0(base.url,"api/dataValueSets?preheatCache=true"),userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/xml", Accept="multipart/*", 'Content-Type' = "application/xml"),
              postfields= pl,
              writefunction = h$update,
              verbose = TRUE )
 proc.time() - ptm 
  #Send the response as a system notification to know what actually happened
  payload<-list(subject="Stock on hand",text=saveXML(xmlParse(h$value())))
  
  url<-paste0(base.url,"api/email/notification")
  
  curlPerform(url=url,userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/json", Accept="multipart/*", 'Content-Type' = "application/json"),
              postfields= toJSON(payload),
              verbose = verbose ) }

#Execution logic
setwd("/home/jason/scripts")
#Read the global and local configuration
source("globalConfig.properties")
numDays<-10
numYears<-2
#Handle to a database connection
m<-dbDriver("PostgreSQL")
con <- dbConnect(PostgreSQL(), user= dbUserName, password=dbPassword,host=dbHost, port=dbPort,dbname=dbName)

#This should be a command line argument, corresponding to the effective date of execution of this script.  
cmd_args<-commandArgs();
#TODO make this more robust
reportDate<-as.Date(cmd_args[6])
#reportDate<-as.Date(cmd_args[6])
if ( is.na(reportDate) ) {reportDate<-as.Date(Sys.time())}

#Trigger analytics
POST(paste0(base.url,"api/resourceTables/analytics?skipResourceTables=false&lastYears=1"),authenticate(username, password))
startTime<-Sys.time()
completed<-FALSE
while(!completed) {
  if (as.numeric(Sys.time()-startTime) > 1200) { halt() }
  r<-GET(paste0(base.url,"api/system/tasks/DATAMART"),authenticate(username, password))
  completed<-content(r, "parsed", "application/json")[[1]]$completed == "TRUE"
  print(completed)
  Sys.sleep(5)}


#Name of the analytics table to access
analytics.table.name<-getAnalyticsTablesName(reportDate,con)

#Configs
fac.config<-list(type=c('DISPENSED','EXPIRED',
                        'WASTED','MISSING',
                        'RECEIVED', 'ADJUSTMENTS',
                        'FROZEN','LABEL_REMOVED','BREAKAGE','OTHERS','VVM_CHANGE'),
                 factor=c(-1,-1,-1,-1,1,1,-1,-1,-1,-1,-1),
                 CommoditiesGroupName="PHC Facility commodities",
                 ActivityGroupName = 'LMIS Activity Type',
                 ou.level=4,
                 soh.name="STOCK_ON_HAND",
                 stockout=TRUE)

state.config<-list(type=c('STATE STORE LOSSES','STATE STORE ADJUSTMENTS','STATE STORE ISSUED','STATE STORE RECEIVED'),
                   factor=c(-1,1,-1,1),
                   CommoditiesGroupName="State Drug Store Commodities",
                   ActivityGroupName = 'State Store LMIS Activity Types',
                   ou.level=2,
                   soh.name="STATE STORE STOCK ON HAND",
                   stockout=FALSE)

#Get the configs
fac.config<-getConfig(fac.config,con)
state.config<-getConfig(state.config,con)


#Get the stock on hand and then merge with the SOH data elements
soh.fac<-getSOH(reportDate,fac.config,con)

soh.state<-getSOH(reportDate,state.config,con)
pl<-rbind(soh.fac,soh.state)
postPayload(pl,username,password)

POST(paste0(base.url,"api/resourceTables/analytics?skipResourceTables=true&lastYears=1"),authenticate(username, password))

