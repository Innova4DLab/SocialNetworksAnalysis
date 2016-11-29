function initialize() {
  //Setup Google Map
  var light_grey_style = [{"featureType":"landscape","stylers":[{"saturation":-100},{"lightness":65},{"visibility":"on"}]},{"featureType":"poi","stylers":[{"saturation":-100},{"lightness":51},{"visibility":"simplified"}]},{"featureType":"road.highway","stylers":[{"saturation":-100},{"visibility":"simplified"}]},{"featureType":"road.arterial","stylers":[{"saturation":-100},{"lightness":30},{"visibility":"on"}]},{"featureType":"road.local","stylers":[{"saturation":-100},{"lightness":40},{"visibility":"on"}]},{"featureType":"transit","stylers":[{"saturation":-100},{"visibility":"simplified"}]},{"featureType":"administrative.province","stylers":[{"visibility":"off"}]},{"featureType":"water","elementType":"labels","stylers":[{"visibility":"on"},{"lightness":-25},{"saturation":-100}]},{"featureType":"water","elementType":"geometry","stylers":[{"hue":"#ffff00"},{"lightness":-25},{"saturation":-97}]}];
  var myOptions = {
    zoom: 2,
    center: new google.maps.LatLng(17.7850,-12.4183),
    mapTypeId: google.maps.MapTypeId.ROADMAP,
    mapTypeControl: true,
    mapTypeControlOptions: {
      style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
      position: google.maps.ControlPosition.LEFT_BOTTOM
    },
    styles: light_grey_style
  };
  var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  
  var markers = [];

  if(io !== undefined) {
    // Storage for WebSocket connections
    var socket = io.connect('/');

    // This listens on the "twitter-steam" channel and data is 
    // received everytime a new tweet is receieved.
    socket.on('twitter-stream', function (data) {
      //alert(JSON.stringify(data));
      
      var infoWindow = new google.maps.InfoWindow();

      for(var i=0; i<data.length ;i++){
          if(!data[i].location.length>0){continue;}
          var tweetLocation = new google.maps.LatLng(data[i].lat, data[i].lng);
          console.log("New point:"+JSON.stringify(tweetLocation));

          //Icon
          var icon = 'css/small-dot-icon-blue.png';
          if(data[i].sentimiento=="N" || data[i].sentimiento=="N+"){
            icon = 'css/small-dot-icon-red.png';
          }else if(data[i].sentimiento=="P" || data[i].sentimiento=="P+"){
            icon = 'css/small-dot-icon-green.png';
          }

          var marker = new google.maps.Marker({
              position: tweetLocation,
              map: map,
              icon: icon,//'http://maps.gstatic.com/mapfiles/ridefinder-images/mm_20'
              title: data[i].location+" - "+data[i].tweet
          });

          (function(marker, tweet){
              google.maps.event.addListener(marker, 'click', function(e) {
                    infoWindow.setContent(tweet.location+" - "+tweet.tweet);
                    infoWindow.open(map, marker);
              });
          })(marker, data[i]);
      }
      
    });

    // Listens for a success response from the server to 
    // say the connection was successful.
    socket.on("connected", function(r) {
      //Now that we are connected to the server let's tell 
      //the server we are ready to start receiving tweets.
      socket.emit("start tweets", {'query': ''});
    });
  }
}