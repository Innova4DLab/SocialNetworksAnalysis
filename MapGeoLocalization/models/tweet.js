var schema = require('mongoose').Schema

var tweetSchema = new schema({
  tweet: {type: String},
  fecha_creacion: {type: String},
  sentimiento: {type: String},
  location: {type: String},
  lat: {type: String},
  lng: {type: String}
});

var Tweet = module.exports = tweetSchema