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
        for (var i = 0; i < u.friends.length; ++i) {
            var friend = u.friends[i];
            if (friend.email == target_email) {
                result["success"] = 0;
                result["message"] = "you already has friend: " + friend.name;
                res.end(JSON.stringify(result));
                return;
            }
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
                    if (t_user.email == user.friends[i]) isFriend = true;
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
    auth(email, password, function(success, message, user) {
        if (!success) {
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
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
                $pull: {"requests_received": t_user},
                $push: {"friends": t_user.email}
            }, function(err, numberAffected, rawResponse) {
                if (numberAffected == 1) {
                    User.update({"email": target_email}, {
                        $pull: {"requests_sent": user},
                        $push: {"friends": user.email}
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
        User.find({
            'email': {$in: u.friends}
        }, function(err, friends) {
            for (var i = 0; i < friends.length; ++i) {
                var friend = friends[i];
                result.friends.push({
                    "name": friend.name,
                    "email": friend.email,
                    "score": friend['info'].score,
                    "score_detail": friend['info'].scoreDetail,
                    "average": friend.average
                });
            }
            result.friends.push({
                "name": u.name,
                "email": u.email,
                "score": u['info'].score,
                "score_detail": u['info'].scoreDetail,
                "average": u.average
            });
            res.end(JSON.stringify(result));
        });
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
            var from = u.requests_received[i];
            var isFriend = false;
            for (var j = 0; j < u.friends.length; ++j) {
                var friend = u.friends[j];
                if (friend.email == from.email) {
                    isFriend = true;
                    break;
                }
            }
            if (!isFriend)
                result.from.push({
                    "name": from.name,
                    "email": from.email
                });
        }
        res.end(JSON.stringify(result));
    });
});

router.post('/update', function(req, res, next) {
    var email = req.body.email;
    var password = req.body.password;
    var userData = JSON.parse(req.body.data);
    auth(email, password, function(success, message, u) {
        if (!success) {
            var result = {};
            result["success"] = 0;
            result["message"] = message;
            res.end(JSON.stringify(result));
            return;
        }
        User.update({"email": email},
            userData, function(err, numberAffected, rawResponse) {
            if (numberAffected > 0) {
                res.end(JSON.stringify({
                    "success": 1
                }));
            } else {
                res.end(JSON.stringify({
                    "success": 0,
                    "message": "Fail to update"
                }));
            }
        });
    });
});

router.post('/info', function(req, res, next) {
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
        res.end(JSON.stringify({
            "success": 1,
            "info": u.info
        }));
    });
});

module.exports = router;
