"""
Created by: Esteban Castillo Juarez

Program that gets the localization of the majority of tweets using a Python module named geopy
https://pypi.python.org/pypi/geopy

To install geopy introduce the following on a command line (Windows) or bash terminal (Unix/Linux):
pip install geopy

note: needs to install first pip, check for more information:
https://pip.pypa.io/en/stable/installing/

Input: the name of the input file in json format (.json)

Output: the name of the output file in json format (.json)

For more information about the json obtained check:
1. twitter stream\ metadataTwitter: a short description of json main properties
2. twitter stream\ metadataTwitterExample: an example of a json obtained using the program 
"""

import codecs
import json
import io
from geopy.geocoders import Nominatim

#IMPORTANT, introduce a input and output file
inputFileName="14-07-2016.json"
outputFileName="geo14-07-2016.json"

#In case of any error
try:
    #Open the input file and check if geopy can find the lat/lon of the tweet
    with codecs.open(inputFileName,"r","UTF-8") as file:
         for line in file:
             #Parse each line in the file to a json format(python dict)
             jsonDict = json.loads(line)
             geolocator = Nominatim()
             text = jsonDict["user"]["location"]
             if text != None:
                 text = (jsonDict["user"]["location"]).encode('utf8')
                 #Search for the lat/lon of a tweet
                 location = geolocator.geocode(jsonDict["user"]["location"])
                 #If there is information, the lat/lon are adeed to the existing json
                 if location != None:
                     location=str(location.latitude)+"/"+str(location.longitude)
                     jsonDict["coordinates"]=location
                     print text+": "+ location
                     with io.open(outputFileName,'a',encoding='utf-8') as f:
                            #Save the json of the tweet with the lat/lon to output file
                            f.write(unicode(json.dumps(jsonDict, ensure_ascii=False))+"\n")                          
except BaseException as e:
    #Print error
    print("Error on data: %s" % str(e))
    pass
