console.log('defining hungry admin');
define(['jquery','cheeseSteak'],function($,cheeseSteak) {
    console.log('evaluating hungryAdmin');
    var $obj = $('#hungry-admin .messages');

    var log = function(msg) {
        $log.html($log.html() + "<br/>" + msg);
    };
    log('ready');
    return {
        log:log
    };

});