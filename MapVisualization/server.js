//Setup web server and socket
var twitter = require('twitter'),
    express = require('express'),
    app = express(),
    http = require('http'),
    server = http.createServer(app),
    io = require('socket.io').listen(server);

//Setup twitter stream api
var twit = new twitter({
  consumer_key: '#',
  consumer_secret: '#',
  access_token_key: '#',
  access_token_secret: '#'
}),
streame = null;

//Use port 8181 to listening
server.listen(8181);
console.log("Listening on 8181");

//Setup rotuing for app
app.use(express.static(__dirname + '/public'));

//Create web sockets connection.
io.sockets.on('connection', function (socket) {

  socket.on("start tweets", function(query) {
    if(streame !== null){
      console.log("************Destroying listener************");
      streame.destroy();
      streame=null;
    }

    if(streame === null) {
      //Connect to twitter stream passing in filter for entire world.
      //'locations':'-180,-90,180,90',
      //puebla:-99.14, 17.84, -96.71, 20.88
      //'track':'domingo'
      //https://dev.twitter.com/overview/api/response-codes
      console.log("Starting listener->"+query['query']);
      streame = twit.stream('statuses/filter', { 'track': query['query']});//, function(stream) {
          streame.on('data', function(data) {
                // Does the JSON result have coordinates
                if (data.coordinates !== null){
                  //If so then build up some nice json and send out to web sockets
                  var outputPoint = {"lat": data.coordinates.coordinates[0],"lng": data.coordinates.coordinates[1]};

                  console.log(JSON.stringify(outputPoint)+"<>"+data.user.location+"<>"+data.text);
                  //socket.broadcast.emit("twitter-stream", outputPoint);

                  //Send out to web sockets channel.
                  socket.emit('twitter-stream', outputPoint);
                }

                else if(data.user.location !== null){
                    var googleMapsClient = require('@google/maps').createClient({
                      key: 'AIzaSyCTNmQhAGn5m2k0-k-ejBDues1m3q961ok'
                    });

                    // Geocode an address.
                    googleMapsClient.geocode({
                      address: data.user.location
                    }, function(err, response) {
                      if (!err) {
                        console.log("Geocode<>"+JSON.stringify(response.json.results[0].geometry.location)+"<>"+data.user.location+"<>"+data.text);

                        var outputPoint = {"lat": response.json.results[0].geometry.location.lat,"lng": response.json.results[0].geometry.location.lng};
                        //socket.broadcast.emit("twitter-stream", outputPoint);
                        //Send out to web sockets channel.
                        socket.emit('twitter-stream', outputPoint);
                      }
                    });

                }else{
                  console.log("Not located<>"+"<>"+data.user.location+"<>"+data.text);
                }                 

              streame.on('limit', function(limitMessage) {
                return console.log(limitMessage);
              });

              streame.on('warning', function(warning) {
                return console.log(warning);
              });

              streame.on('disconnect', function(disconnectMessage) {
                return console.log(disconnectMessage);
              });
          });

          streame.on('error', function(error) {
            //throw error;
            return new Error("Error in Stream")
          });
      //});
    }
  });

    // Emits signal to the client telling them that the
    // they are connected and can start receiving Tweets
    socket.emit("connected");
});
