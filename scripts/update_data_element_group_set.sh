#!/bin/sh
export API_URL="http://lmisdev.dhis2.org/api/systemSettings/data_element_group_set_id"
export USER="tw_test"
export PASSWORD="Secret123"
curl ${API_URL} -d 'OvBXLc9jKsE' -H "Content-Type: text/plain" -u ${USER}:${PASSWORD} -v
