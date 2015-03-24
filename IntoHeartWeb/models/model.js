var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var User = mongoose.model('User', Schema({
    name: { type: String, required: true},
    password: { type: String, required: true},
    email: { type: String, unique: true, required: true},
    friends: [User],
    requests_sent: [User],
    requests_received: [User],
    score: Number,
    average: Number
}));