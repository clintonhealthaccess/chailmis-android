import json
import uuid
import os
from random import randrange
file_path = "./app/src/main/assets/default_commodities.json"
out_file_path = "./app/src/main/assets/default_commodities.json.temp"
file_handle = open(file_path,"r")
out_file_handle = open(out_file_path,"w")
data = json.load(file_handle)
data_set = data[0]['commodities']
actions = []
for commodity in data_set:
    for action in actions:
        activity = {}
        activity['activityType'] = action
        activity['id'] = str(uuid.uuid4())
        activity['name'] = str(uuid.uuid4())
        activity['dataSet'] = {"id":str(uuid.uuid4()),"periodType":"Daily","name":"lmis dataset"}
        commodity['commodityActions'].append(activity)
print json.dumps(data)

with out_file_handle:
    json.dump(data, out_file_handle, sort_keys=True,
              indent=4, separators=(',', ': '))

os.remove(file_path)
os.rename(out_file_path, file_path)