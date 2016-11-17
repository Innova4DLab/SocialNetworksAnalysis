"""
Created by: Esteban Castillo Juarez

Program that get tweets (texts) from the Twitter Streaming API using a Python 
module(version 2.7) named Tweepy

To install Tweepy introduce the following on a command line (Windows) or bash terminal (Unix/Linux):
pip install tweepy

note: needs to install first pip, check for more information:
https://pip.pypa.io/en/stable/installing/

Input: 
1. the number of tweets to retrieve (stored in tweetNumber variable)
2. a set of terms to search on Twitter (stored in setTerms variable)

Output: text document with a json for each tweet

For more information about the json obtained check:
1. twitter stream\ metadataTwitter: a short description of json main properties
2. twitter stream\ metadataTwitterExample: an example of a json obtained using the program 
"""

#Needs to install tweepy for python 2.7 (pip install tweepy)
import tweepy
import json
import time
import codecs
import io

#Main class that implements a Twitter listener
class StreamListener(tweepy.StreamListener):
    """
    Number of tweets to retrieve, it can be changed  depending of the 
    amount of tweets needed
    """
    tweetNumber = 10 
    counter = 1
	
    def on_status(self, tweet):
        print "------------------"
        
	#In case of any error finish the execution	
    def on_error(self, status_code):
        print "Error: " + repr(status_code)
        return False
		
    """
    In case that the listener find a tweet that matches with one or more 
    of the terms in setTerms variable save the tweet on a file	
    """
    def on_data(self, data):
        if (self.counter <= self.tweetNumber):
            try:
		        #Load the twitter to a json format
                d = json.loads(data)
                print "tweet number: "+str(self.counter)
		        #Creation of a json file with the current date (append mode)
                with io.open("TwitterStream-"+str(time.strftime("%d-%m-%Y"))+".json",'a',encoding='utf-8') as f:
                    #Save the tweet on a file considering the codification (Unicode)
                    f.write(unicode(json.dumps(d, ensure_ascii=False))+"\n")                    
                self. counter += 1
                return True
            except BaseException as e:
                print("Error on data: %s" % str(e))
                pass
            return True
        else:
            return False
        
    """
    In case that the listener not find any tweet on approximately 60	
    seconds finish the execution
    """
    def on_timeout(self):
        print "Time out: not information on time limit..."
        return False
		
"""	
IMPORTANT
Introduce valid oauth tokens

In order to obtain tokens create a neww app in the following URL:
http://apps.twitter.com 
"""

consumer_key = "xxxx"
consumer_secret = "xxxx"
access_token_key = "xxxx"
access_token_secret = "xxxx"


#Initialize  the variables
auth1 = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth1.set_access_token(access_token_key, access_token_secret)
#Search terms (spanish or english)
setTerms = ["puebla",
            "cholula",
            "mexico",
            "uruguay",]
			
l = StreamListener()
streamer = tweepy.Stream(auth=auth1, listener=l, timeout=60)
"""
Create a stream object to obtain tweets

Languages: es-spanish or en-english
"""
streamer.filter(track = setTerms,languages=["es"])
