#! /bin/bash
curl "http://104.131.225.22:8888/dhis2/api/systemSettings/reasons_for_order" -d "['out of stock','low stock']" -H "Content-Type: text/plain" -u admin:district -v
