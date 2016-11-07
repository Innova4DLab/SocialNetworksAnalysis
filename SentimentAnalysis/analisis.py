import sys
import requests
import demjson

from pymongo import MongoClient#Libreria Mongodb

#Coneccion a MongoDB
cliente = MongoClient()#Inicializar objeto
cliente = MongoClient('127.0.0.1', 27017)#Indicar parametros del servidor
bd = cliente.twitter#Seleccionar Schema
tweets = bd.tweets#Seleccionar Coleccion

def sentimiento(tweet):
    url = "http://api.meaningcloud.com/sentiment-2.1"
    headers = {'content-type': 'application/x-www-form-urlencoded'}
    payload = "key=meaningCloudKey&lang=es&txt="+tweet+"&model="
    response = requests.request("POST", url, data=payload, headers=headers)
    return response

for tweet in tweets.find({"$where": "this.sentimiento.length == 0"}):#Seleccionar tweets que no tienen sentimiento
    print "Tweet:"+tweet['tweet']
    while True:#Analizar sentimiento de tweet
        resultado = sentimiento(tweet['tweet'].encode('utf-8'))#Analizar tweet con el API
        json = demjson.decode(resultado.text)#Decodificar la respuesta JSON
        estado = json['status']['code'];#Obtener el codigo de estado de la respuesta
        if estado == '104':
            print "Error 104 se excedio el limite de 2 solicitudes/segundo, esperando 5 segundos..."
            time.sleep( 5 )
        elif estado != '0':#Ocurrio otro tipo de error
            print "Ocurrio un error "+str(estado)+", saliendo..."
            sys.exit(0);
        else:#Respuesta correcta
            print "Sentimiento:"+json['score_tag']
            tweets.update({"idt":tweet['idt']}, {'$set':{'sentimiento':json['score_tag']}})
            break

        