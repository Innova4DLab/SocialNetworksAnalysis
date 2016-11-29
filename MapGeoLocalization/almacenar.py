import sys
import tweepy
import requests
import codecs
from pymongo import MongoClient#Libreria Mongodb
reload(sys)
sys.setdefaultencoding('utf8')

#Conexion a MongoDB
cliente = MongoClient()#Inicializar objeto
cliente = MongoClient('127.0.0.1', 27017)#Indicar parametros del servidor
bd = cliente.twitter#Seleccionar Schema
tweets = bd.tweets#Seleccionar Coleccion

#llaves del API Twitter
consumer_key = '#'
consumer_secret = '#'
access_token = '#'
access_secret = '#'

#Count all tweets that not are analyzed
#db.tweets.find( {$where: "this.sentimiento.length == 0"} ).count();

#
#db.tweets.find( { sentimiento: { $exists: true } } ).count();

class MyStreamListener(tweepy.StreamListener): #Extender la clase streamListener
    def on_status(self, status):#Procesar nuevos tweets|
        if not hasattr(status, 'retweeted_status'):#Ignorar retweets
            #print status.source.encode('utf-8')+"<>"+status.text.encode('utf-8')#Codificar en UTF-8 el mensaje antes de imprimirlo
            print "Tweet:"+status.text.decode('utf-8')
            location = status.user.location.encode('utf-8') if status.user.location  else ''
            print "Location:"+location
            #Crear Objeto
            tweet = {
                    "idt": str(status.id),
                    "tweet": status.text,
                    "fecha_creacion": status.created_at,
                    "sentimiento": "",
                    "location": location,
                    "lat": '',
                    "lng": ''
                    };
            tweets.insert_one(tweet)#Almacenar tweet
            
    def on_error(self, status_code): 
        if status_code == 420:
            print "Numero de intentos excesivos de conectarse al streaming API, esperar y ejecutar de nuevo..."
        elif status_code == 401:#
            print "Credenciales de API incorrectas."
        else:
            print "Ocurrio un error"
        return False #Cancelar la ejecucion.

    def on_timeout(self):
        print "Timeout..."
        return False #Cancelar la ejecucion.

tauth = tweepy.OAuthHandler(consumer_key, consumer_secret)#Autenticar
tauth.set_access_token(access_token, access_secret)#Autenticar

streamListen = MyStreamListener()#Instanciar clase "MyStreamListener"
twStream = tweepy.Stream(auth = tauth, listener = streamListen )#Crear stream
#.encode('utf-8', errors = 'ignore')
#twStream.filter(track=['#Puebla, #EPN, #Mexico, #Telcel, @joseluisvzg'], async=False)
#twStream.filter(track=['@Udelaruy', '@FicUdelar', '#uruguay', '#montevideo'], async=False)#Especificar filtro & Iniciar Stream
twStream.filter(track=['fidel castro'], async=False)#Especificar filtro & Iniciar Stream

