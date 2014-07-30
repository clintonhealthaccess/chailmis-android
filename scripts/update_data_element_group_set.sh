#!/bin/sh
export API_URL="http://104.131.225.22:8888/dhis2/api/systemSettings/data_element_group_set_id"
export USER="admin"
export PASSWORD="district"
curl ${API_URL} -d 'OvBXLc9jKsE' -H "Content-Type: text/plain" -u ${USER}:${PASSWORD} -v
