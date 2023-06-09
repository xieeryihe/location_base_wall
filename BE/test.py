
import urllib2
import urllib
import json
 
def http_post(url,data_json):
    #jdata = json.dumps(data_json)
    data_json = urllib.urlencode(data_json)
    req = urllib2.Request(url, data_json)
    response = urllib2.urlopen(req)
    return response.read()
 
login_url = 'http://127.0.0.1:8008/login/'
data_json = {'username' : 'rng', 'password' : 'uziuziuzi'}
resp = http_post(login_url, data_json)
print(resp)

test_func = "get_all_projects"
url = 'http://127.0.0.1:8008/{}/'.format(test_func)
#data_json = {'username': 'gnr`r','password':'uzi', 'email':'fuck@that.shit'}
data_json = {'project_name' : 'msi_championship'}
resp = http_post(url,data_json)
print(resp)

logout_url = 'http://127.0.0.1:8008/logout/'
data_json = {'username' : 'rng', 'password' : 'uzi'}
resp = http_post(logout_url, data_json)
print(resp)