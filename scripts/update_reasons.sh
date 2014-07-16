#! /bin/bash
curl "http://104.131.225.22:8888/dhis2/api/systemSettings/reasons_for_order" -d '{"order_reasons":["Emergency","Routine"],"unexpected_quantity_reasons":["High Demand","Losses","Expirations","Adjustments"]}' -H "Content-Type: text/plain" -u admin:district -v
