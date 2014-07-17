#! /bin/bash
curl "http://lmisdev.dhis2.org/api/systemSettings/reasons_for_order" -d '{"order_reasons":["Emergency","Routine"],"unexpected_quantity_reasons":["High Demand","Losses","Expirations","Adjustments"]}' -H "Content-Type: text/plain" -u tw_test:Secret123 -v
