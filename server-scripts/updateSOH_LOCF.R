require(RCurl)
require(httr)
require(RPostgreSQL)
require(lubridate)
require(dplyr)
require(zoo)
require(XML)
require(rjson)
#Read the global and local configuration
source("globalConfig.properties")
numDays<-30
#Handle to a database connection
m<-dbDriver("PostgreSQL")
con <- dbConnect(PostgreSQL(), user= dbUserName, password=dbPassword,host=dbHost, port=dbPort,dbname=dbName)

#This should be a command line argument, corresponding to the effective date of execution of this script.  
cmd_args<-commandArgs();
#TODO make this more robust
reportDate<-as.Date(cmd_args[6])
#reportDate<-as.Date(cmd_args[6])
if ( is.na(reportDate) ) {reportDate<-as.Date(Sys.time())}

#Start date for the calculation is going to be numDays in the past
getStartDate<-function(this.date=now()) { 
  this.date-days(numDays)
}


getDailyTimeSeries<-function(this.date=now()) {
  foo<-seq(as.Date(getStartDate(this.date)),as.Date(this.date),by="days") 
  data.frame(startdate=foo,daily=gsub("-","",as.character(foo)))
}


getPeriods<-function(con,this.date,periodtype="Daily"){
  foo<-seq(as.Date(getStartDate(this.date)),as.Date(this.date),by="days")
  sql<-paste0("SELECT periodid,startdate,enddate from period where startdate in ('",paste(foo,sep="",collapse="','"),"')
              and periodtypeid = (SELECT periodtypeid from periodtype where name = '",periodtype,"')
              ;")
  dbGetQuery(con,sql)
}


updateSOHData<-function(con,this.date,base.url,username,password){
  periods<-getPeriods(con,this.date,"Daily")
  sql<-paste0("SELECT p.startdate,de.uid as de_uid,coc.uid as coc_uid,ou.uid as ou_uid,dv.value from datavalue dv
              INNER JOIN period p on dv.periodid = p.periodid
              INNER JOIN categoryoptioncombo coc on dv.categoryoptioncomboid = coc.categoryoptioncomboid
              INNER JOIN dataelement de on dv.dataelementid = de.dataelementid
              INNER JOIN organisationunit ou on dv.sourceid = ou.organisationunitid
              WHERE dv.periodid IN (",paste(periods$periodid,sep="",collapse=","),")
              and dv.dataelementid in (
              SELECT DISTINCT dataelementid from dataelement where name ~('STOCK[ _]ON[ _]HAND') and name !~('MONTH')
              )")
  #Get the existing data
  d<-dbGetQuery(con,sql)
  if (nrow(d) == 0 ) { print("No data. Aborting"); stop() }
  #We need to create an empty data frame of the data elements, cateogry option combos and orgunits
  foo<-unique(d[,c("de_uid","coc_uid","ou_uid")])
  #Cross join this with the dates
  bar<-data.frame(startdate=seq(as.Date(getStartDate(this.date)),as.Date(this.date),by="days"))a
  #A full merge of the data
  d.full<-merge(foo,bar,all=T)
  d.full<-merge(d.full,d,by=c("startdate","de_uid","coc_uid","ou_uid"),all.x=T)
  #Be sure these are not factors, as we could have empty/missing factors
  d.full<-plyr::colwise(as.character)(d.full)
  #Perform the na.locf function  over each group
  d.full<-d.full %>%
    arrange(ou_uid,de_uid,coc_uid,startdate)%>%
    mutate(value = na.locf(value, na.rm = F))
  #Take advantage of the fact that any NAs will correspond to facilities not having reported yet
  d.full<-d.full[complete.cases(d.full),]
  #Assemble as XML
  #Post this payload back to the API for import
  soh<-paste0('<dataValueSet xmlns="http://dhis2.org/schema/dxf/2.0">',
              paste('<dataValue dataElement="',as.character(d.full$de_uid),'" period="',
                    gsub("-","",as.character(d.full$startdate)),'" orgUnit="',
                    as.character(d.full$ou_uid),'" value="',as.character(d.full$value),'"/>',sep="",collapse=""),"</dataValueSet>")
  #Save the file in case we need to analyze it
  #cat(soh,file="soh.xml") 
  #Post it back
  h = basicTextGatherer()
  curlPerform(url=paste0(base.url,"api/dataValueSets?preheatCache=false"),userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/xml", Accept="multipart/*", 'Content-Type' = "application/xml"),
              postfields= soh,
              writefunction = h$update,
              verbose = TRUE )
  
  #Send the response as a system notification to know what actually happened
  payload<-list(subject="Stock on hand LOCF",text=saveXML(xmlParse(h$value())))
  url<-paste0(base.url,"api/email/notification")
  
  curlPerform(url=url,userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/json", Accept="multipart/*", 'Content-Type' = "application/json"),
              postfields= toJSON(payload),
              verbose = TRUE )
}


updateSOHData(con,reportDate,base.url,username,password)

POST(paste0(base.url,"api/resourceTables/analytics?skipResourceTables=true&lastYears=1"),authenticate(username, password))
