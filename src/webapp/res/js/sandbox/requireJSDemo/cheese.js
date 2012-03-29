console.log('defining cheese');
define(['jquery','chef'],function($,chef) {
    console.log('evaluating cheese');
    var $log = $('#cheese .messages');

    var log = function(msg) {
        $log.html($log.html() + "<br/>" + msg);
    };

    log('ready');

    return {
        log:log
    };

});