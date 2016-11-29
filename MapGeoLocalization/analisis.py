import sys
import requests
import demjson
import time
from geopy.geocoders import Nominatim
from geopy.geocoders import GoogleV3
from pymongo import MongoClient#Libreria Mongodb
#Set encoding to utf-8
reload(sys)
sys.setdefaultencoding('utf8')

#Coneccion a MongoDB
cliente = MongoClient()#Inicializar objeto
cliente = MongoClient('127.0.0.1', 27017)#Indicar parametros del servidor y el puerto de acceso
bd = cliente.twitter#Seleccionar Schema
tweets = bd.tweets#Seleccionar Coleccion

def sentimiento(tweet):
    json = []
    while True:
        url = "http://api.meaningcloud.com/sentiment-2.1"
        headers = {'content-type': 'application/x-www-form-urlencoded'}
        payload = "key=keyhere&lang=es&txt="+tweet+"&model="
        response = requests.request("POST", url, data=payload, headers=headers)
        json = demjson.decode(response.text)#Decodificar la respuesta JSON
        estado = json['status']['code'];#Obtener el codigo de estado de la respuesta
        if estado == '104':
            print "Error 104 se excedio el limite de 2 solicitudes/segundo, esperando 5 segundos..."
            time.sleep( 5 )
        elif estado != '0':#Ocurrio otro tipo de error
            print "Ocurrio un error "+str(estado)+" (Meaningcloud)"
        else:#Codigo 0 -> Respuesta correcta
            break
    return json['score_tag']

def location(locationTweet):
    try:
        #geolocator = Nominatim()
        geolocator = GoogleV3()
        loc = geolocator.geocode(locationTweet, timeout=10)
        return loc
    except:# GeocoderTimedOut as e:
        print("Error: geocode failed on input %s with message %s"%(locationTweet, "e"))#e.msg))
    return None

while True:
    for tweet in tweets.find({"$where": "this.sentimiento.length == 0"}):#Seleccionar los tweets a los que no se ha realizado el analisis de sentimiento
        print "Tweet:"+tweet['tweet']+"\nLocation:"+tweet['location']
        sent = sentimiento(tweet['tweet'])#Analizar sentimiento del tweet
        print "Sentimiento:"+sent
        loc = location(tweet['location'])#Tratar de obtener coordenadas
        print "Location:"+str(loc.latitude)+","+str(loc.longitude) if loc != None else 'No located'      
        tweets.update({"_id":tweet['_id']}, {'$set':{'sentimiento':sent, 'lat': loc.latitude if loc != None else '', 'lng': loc.longitude if loc != None else ''}})#Actualizar datos del documento en Mongodb
    print "No hay datos por analizar, esperar 10 segundos..."
    time.sleep( 10 )
