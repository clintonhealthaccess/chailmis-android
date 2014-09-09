import json
import uuid
from random import randrange
file_path = "../app/src/main/assets/default_commodities.json"
file_handle = open(file_path,"r")
data = json.load(file_handle)
data_set = data[0]['commodities']
periodTypes = ['daily','monthly','bimonthly']
for commodity in data_set:
    for activity in commodity['commodityActivities']:
        random_index = randrange(3)
        activity['dataSet'] = {"id":str(uuid.uuid4()),"periodType":periodTypes[random_index],"name":"lmis dataset"}
print json.dumps(data)
