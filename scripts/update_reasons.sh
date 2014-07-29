#!/bin/sh
export API_URL="http://104.131.225.22:8888/dhis2/api/systemSettings/reasons_for_order"
export USER="android_1"
export PASSWORD="Password1"
curl ${API_URL} -d '{"order_reasons":["Emergency","Routine"],"unexpected_quantity_reasons":["High Demand","Losses","Expirations","Adjustments"]}' -H "Content-Type: text/plain" -u ${USER}:${PASSWORD} -v