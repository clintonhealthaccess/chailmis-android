# Copyright (c) 2015, University of Oslo
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
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
#This script simply carries forward all SOH values reported by the client. 
#Values are then posted back 
#to the dataValueSet API endpoint for import.
###################################################################################

require(RCurl)
require(httr)
require(RPostgreSQL)
require(jsonlite)
require(lubridate)
require(XML)
#Read the global and local configuration
source("globalConfig.properties")
numDays<-30
#Handle to a database connection
m<-dbDriver("PostgreSQL")
con <- dbConnect(PostgreSQL(), user= dbUserName, password=dbPassword,host=dbHost, port=dbPort,dbname=dbName)

#This should be a command line argument, corresponding to the effective date of execution of this script.  
#Always shift back a day, as we are never going to calculate the carry forward values for the day actually given.

cmd_args<-commandArgs();
#TODO make this more robust
reportDate<-as.Date(cmd_args[6]) - lubridate::days(1)
#reportDate<-as.Date(cmd_args[6])
if ( is.na(reportDate) ) {reportDate<-as.Date(Sys.time())}

#Start date for the calculation is going to be numDays in the past
getStartDate<-function(this.date=now()) { 
  this.date-days(numDays)
}



updateSOHData<-function(con,this.date,base.url,username,password){

  sql<-paste0("SELECT * FROM get_locf_data('",as.character(getStartDate(reportDate)),"','",as.character(reportDate),"')");
  #Get the existing data
  d<-dbGetQuery(con,sql)
  if (nrow(d) == 0 ) { print("No data. Aborting"); stop() }
  else {
  #Assemble as XML
  #Post this payload back to the API for import
  soh<-paste0('<dataValueSet xmlns="http://dhis2.org/schema/dxf/2.0">',
              paste('<dataValue dataElement="',as.character(d$de),'" period="',
                    as.character(d$pe),'" orgUnit="',
                    as.character(d$ou),'" value="',as.character(d$value),'" comment="LOCF"/>',sep="",collapse=""),"</dataValueSet>")
  #Save the file in case we need to analyze it
  write.csv(d,file="soh_locf.csv") 
  #Post it back
  h = basicTextGatherer()
  curlPerform(url=paste0(base.url,"api/dataValueSets?preheatCache=false"),userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/json", Accept="multipart/*", 'Content-Type' = "application/xml"),
              postfields= soh,
              writefunction = h$update,
              verbose = TRUE )
  
  #Send the response as a system notification to know what actually hap??pened
  resp<-xmlParse(h$value())
  status<-xmlValue(xmlRoot(resp)[["status"]])
  description<-xmlValue(xmlRoot(resp)[["description"]])
  ic<-list(t(xmlAttrs(xmlRoot(resp)[["importCount"]])))
  msg=paste0("Status:",xmlValue(xmlRoot(resp)[["status"]]),"\r\nDescription:",xmlValue(xmlRoot(resp)[["description"]]),"\r\nImported:",ic[[1]][1],"\r\n","Updated:",ic[[1]][2])
  #payload<-list(subject="Stock on hand LOCF",text=saveXML(xmlParse(h$value())))
  payload<-list(subject="Stock on hand LOCF",text=msg)
payload<-toJSON(data.frame(payload))
payload<-gsub("\\[","",payload)
payload<-gsub("\\]","",payload)
 url<-paste0(base.url,"api/email/notification")
  
  curlPerform(url=url,userpwd=paste0(username,":",password),
              httpauth = 1L,
              httpheader=c(Accept="application/json",  'Content-Type' = "application/json"),
              postfields= payload,
              verbose = TRUE ) 
  
  }
  
}


updateSOHData(con,reportDate,base.url,username,password)

POST(paste0(base.url,"api/resourceTables/analytics?skipResourceTables=true&lastYears=1"),authenticate(username, password))
