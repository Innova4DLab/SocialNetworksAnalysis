import sys
import tweepy
import requests
import codecs

#llaves del API Twitter
consumer_key = ''
consumer_secret = ''
access_token = ''
access_secret = ''


class MyStreamListener(tweepy.StreamListener): #Extender la clase streamListener
    def on_status(self, status):#Procesar nuevos tweets
        if not hasattr(status, 'retweeted_status'):#Ignorar retweets
            print status.text.encode('utf-8')#Codificar en UTF-8 el mensaje antes de imprimirlo
            
    def on_error(self, status_code): 
        if status_code == 420:
            print "Numero de intentos excesivos de conectarse al streaming API, esperar y ejecutar de nuevo..."
        elif status_code == 401:#
            print "Credenciales de API incorrectas."
        else:
            print "Ocurrio un error "+str(status_code)
        return False #Seguir la ejecucion. True cancelaria la ejecucion del programa

    def on_timeout(self):
        print "Timeout..."
        return False #Seguir la ejecucion. True cancelaria la ejecucion del programa

tauth = tweepy.OAuthHandler(consumer_key, consumer_secret)#Autenticar
tauth.set_access_token(access_token, access_secret)#Autenticar

streamListen = MyStreamListener()#Instanciar clase "MyStreamListener"
twStream = tweepy.Stream(auth = tauth, listener = streamListen )#Crear stream

twStream.filter(track=['#Puebla, #EPN, #Mexico'], async=False)#Especificar filtro & Iniciar Stream

