console.log('defining cheeseSteak');
define(['jquery','cheese','steak','chef'],function($,cheese,steak,chef) {
    console.log('evaluating cheeseSteak');
    var $log = $('#cheesesteak-sandwich .messages');

    var log = function(msg) {
        $log.html($log.html() + "<br/>" + msg);
    };
    log('ready');
    return {
        log:log
    };

});