//Setup web server and socket
var express = require('express'),
    app = express(),
    http = require('http'),
    server = http.createServer(app),
    io = require('socket.io').listen(server),
    mongoose = require('mongoose');//Mongodb driver

mongoose.connect('mongodb://127.0.0.1/twitter');//Connect to the mongodb "server:database"
var db = mongoose.connection;//Reference to the mongodb connection

db.on('error', console.error.bind(console, 'Connection to Mongodb error:'));

db.once('open', function() {
  console.log("Connected to Mongodb!!!");
  //Start webapp in  port 8181
  server.listen(8181);
  console.log("Listening on 8181");
});

//Define Schema
var tweetSchema = require('./models/tweet');
//Setting Schema to the Model
var tweetModel = mongoose.model('tweets', tweetSchema);

//Setup routing for app
app.use(express.static(__dirname + '/public'));

//Create web sockets connection.
io.sockets.on('connection', function (socket) {
  socket.on("start tweets", function(data) {
      tweetModel.find(function(err, tweets){
        if(err) socket.emit('twitter-stream', []);
        console.log(tweets.length+" tweets!!!");
        socket.emit('twitter-stream', tweets);//Emit tweets
        return;
      });   
  });
  // Emits signal to the client telling them that the
  // they are connected and can start receiving Tweets
  socket.emit("connected");
});
