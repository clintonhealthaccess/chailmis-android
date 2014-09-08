import json
import uuid

file_path = "../app/src/main/assets/default_commodities.json"
file_handle = open(file_path,"r")
data = json.load(file_handle)
data_set = data[0]['commodities']

for commodity in data_set:
    for activity in ["EXPIRED","WASTE","MISSING"]:
        commodity['commodityActivities'].append({"activityType":activity,"id":str(uuid.uuid4()),"name":str(uuid.uuid4()),"dataSet":str(uuid.uuid4())})
print json.dumps(data)
