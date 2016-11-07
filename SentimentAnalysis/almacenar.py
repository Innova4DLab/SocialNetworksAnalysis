import sys
import tweepy
import requests
import codecs
from pymongo import MongoClient#Libreria Mongodb

#Conexion a MongoDB
cliente = MongoClient()#Inicializar objeto
cliente = MongoClient('127.0.0.1', 27017)#Indicar parametros del servidor y del puerto del acceso
bd = cliente.twitter#Seleccionar Schema
tweets = bd.tweets#Seleccionar Coleccion

#llaves del API Twitter
consumer_key = ''
consumer_secret = ''
access_token = ''
access_secret = ''


class MyStreamListener(tweepy.StreamListener): #Extender la clase streamListener
    def on_status(self, status):#Procesar nuevos tweets|
        if not hasattr(status, 'retweeted_status'):#Ignorar retweets
            print status.text.encode('utf-8')#Codificar en UTF-8 el mensaje antes de imprimirlo
            #Crear Objeto
            tweet = {
                    "idt": str(status.id),
                    "tweet": status.text.encode('utf-8'),
                    "fecha_creacion": status.created_at,
                    "sentimiento": ""
                    };
            tweets.insert_one(tweet)#Almacenar tweet
            
    def on_error(self, status_code): 
        if status_code == 420:
            print "Numero de intentos excesivos de conectarse al streaming API, esperar y ejecutar de nuevo..."
        elif status_code == 401:#
            print "Credenciales de API incorrectas."
        else:
            print "Ocurrio un error "+str(status_code)
        return False #Cancelar la ejecucion del programa

    def on_timeout(self):
        print "Timeout..."
        return False #Cancelar la ejecucion del programa

tauth = tweepy.OAuthHandler(consumer_key, consumer_secret)#Autenticar
tauth.set_access_token(access_token, access_secret)#Autenticar

streamListen = MyStreamListener()#Instanciar clase "MyStreamListener"
twStream = tweepy.Stream(auth = tauth, listener = streamListen )#Crear stream

twStream.filter(track=['#Puebla, #EPN, #Mexico'], async=False)#Especificar filtro & Iniciar Stream

