var express = require('express');
var router = express.Router();
var models = require("../models/model");
var mongoose = require('mongoose');
var User = mongoose.model('User');


function auth(email, password, callback) {
    User.findOne({"email": email}, function(err, user) {
        if (user) {
            if (password == user.password) {
                callback(1, user.name, user);
            } else {
                callback(0, "log in failed: wrong password", user);
            }
        } else {
            callback(0, "log in failed: user not found", user);
        }
    });
}

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.send('respond with a resource');
});

router.post('/login', function (req, res, next) {
    console.log(req.body);
    var email = req.body.email;
    var password = req.body.password;
    auth(email, password, function(success, message, u) {
        res.end(JSON.stringify({
            'success': success,
            'message': message
        }));
    });
});

router.post('/signup', function(req, res, next) {
    var name = req.body.name;
    var email = req.body.email;
    var password = req.body.password;

    var result = {
        "success": 0
    };

    if (name && email && password) {
        var user = new User({
            "name": name,
            "email": email,
            "password": password,
            "score": 0
        });
        user.save(function(err, user) {
            if (!err) result.success = 1;
            res.end(JSON.stringify(result));
        });
    } else {
        res.end(JSON.stringify(result));
    }
});

router.post('/search', function(req, res, next) {
    var email = req.body.email;
    var result = {};
    User.findOne({"email": email}, function(err, user) {
        if (user) {
            result['success'] = 1;
            result['message'] = user.name;
        } else {
            result['success'] = 0;
            result['message'] = "Cannot find user with email " + email;
        }
        res.end(JSON.stringify(result));
    });
});

router.post('/request', function(req, res, next) {
    var email = req.body.email;
    var password = req.body.password;
    var target_email = req.body.t_email;
    var result = {};
    auth(email, password, function(success, message, u) {
        if (!success) {
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
        User.findOne({"email": email}, function(err, user) {
            User.findOne({"email": target_email}, function(err, t_user) {
                if (!t_user) {
                    result["success"] = 0;
                    result["message"] = "cannot find user with email: " + target_email;
                    res.end(JSON.stringify(result));
                    return;
                }
                var isFriend = false;
                for (var i = 0; i < user.friends.length; ++i) {
                    if (t_user.email == user.friends[i].email) isFriend = true;
                }
                if (isFriend) {
                    result["success"] = 0;
                    result["message"] = "you already has friend: " + t_user.name;
                    res.end(JSON.stringify(result));
                    return;
                }
                var hasRequested = false;
                for (var i = 0; i < user.requests_sent.length; ++i) {
                    if (t_user.email == user.requests_sent[i].email) hasRequested = true;
                }
                if (hasRequested) {
                    result["success"] = 0;
                    result["message"] = "you already requested to " + t_user.name;
                    res.end(JSON.stringify(result));
                    return;
                }
                User.update({"email": email}, {
                    $push: {"requests_sent": t_user}
                }, function(err, numberAffected, rawResponse) {
                    if (numberAffected == 1) {
                        User.update({"email": target_email}, {
                            $push: {"requests_received": user}
                        }, function(err, numberAffected, rawResponse) {
                            if (numberAffected == 1) {
                                result["success"] = 1;
                                result["message"] = t_user.name;
                                res.end(JSON.stringify(result));
                            } else {
                                result["success"] = 0;
                                result["message"] = "failed to send request";
                                res.end(JSON.stringify(result));
                            }
                        })
                    } else {
                        result["success"] = 0;
                        result["message"] = "failed to send request";
                        res.end(JSON.stringify(result));
                    }
                })
            });
        });
    });
});

router.post('/response', function(req, res, next) {
    var email = req.body.email;
    var password = req.body.password;
    var target_email = req.body.t_email;
    var result = {};
    auth(email, password, function(success, message, u) {
        if (!success) {
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
        User.findOne({"email": email}, function(err, user) {
            User.findOne({"email": target_email}, function(err, t_user) {
                if (!t_user) {
                    result["success"] = 0;
                    result["message"] = "cannot find user with email: " + target_email;
                    res.end(JSON.stringify(result));
                    return;
                }
                var isFriend = false;
                for (var i = 0; i < user.friends.length; ++i) {
                    if (t_user.email == user.friends[i].email) isFriend = true;
                }
                if (isFriend) {
                    result["success"] = 0;
                    result["message"] = "you already has friend: " + t_user.name;
                    res.end(JSON.stringify(result));
                    return;
                }
                User.update({"email": email}, {
                    $pull: {"requests_sent": t_user},
                    $push: {"friends": t_user}
                }, function(err, numberAffected, rawResponse) {
                    if (numberAffected == 1) {
                        User.update({"email": target_email}, {
                            $pull: {"requests_received": user},
                            $push: {"friends": user}
                        }, function(err, numberAffected, rawResponse) {
                            if (numberAffected == 1) {
                                result["success"] = 1;
                                result["message"] = t_user.name;
                                res.end(JSON.stringify(result));
                            } else {
                                result["success"] = 0;
                                result["message"] = "failed to confirm";
                                res.end(JSON.stringify(result));
                            }
                        })
                    } else {
                        result["success"] = 0;
                        result["message"] = "failed to confirm";
                        res.end(JSON.stringify(result));
                    }
                })
            });
        });
    });
});

router.post('/rank', function(req, res, next) {
    var email = req.body.email;
    var password = req.body.password;
    auth(email, password, function(success, message, u) {
        if (!success) {
            var result = {};
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
        var result = {"success": 1, "friends": []};
        for (var i = 0; i < u.friends.length; ++i) {
            var friend = u.friends[i];
            result.friends.push({
                "name": friend.name,
                "email": friend.email,
                "score": friend.score,
                "average": friend.average
            });
        }
        res.end(JSON.stringify(result));
    });
});

router.post('/get_request', function(req, res, next) {
    var email = req.body.email;
    var password = req.body.password;
    auth(email, password, function(success, message, u) {
        if (!success) {
            var result = {};
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
        var result = {"success": 1, "from": []};
        for (var i = 0; i < u.requests_received.length; ++i) {
            var friend = u.requests_received[i];
            result.from.push({
                "name": friend.name,
                "email": friend.email
            });
        }
        res.end(JSON.stringify(result));
    });
});

module.exports = router;
