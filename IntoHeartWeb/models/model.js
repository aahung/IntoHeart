var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var User = mongoose.model('User', Schema({
    name: { type: String, required: true},
    password: { type: String, required: true},
    email: { type: String, unique: true, required: true},
    friends: [String], // emails
    requests_sent: [User], // emails
    requests_received: [User], // emails

    // user data
    info: {
        score: Number,
        scoreDetail: [Number],
        average: Number,
        age: Number,
        height: Number,
        weight: Number,
        phone: String,
        lifestyles: [Number]
    }
}));