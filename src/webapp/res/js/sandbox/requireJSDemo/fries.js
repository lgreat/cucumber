console.log('defining fries');
define(['jquery'],function($) {
    console.log('evaluating fries');
    var $obj = $('#fries .messages');

    var log = function(msg) {
        $log.html($log.html() + "<br/>" + msg);
    };
    log('ready');
    return {
        log:log
    };

});