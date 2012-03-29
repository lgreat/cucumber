console.log('defining chef');
define(['jquery'],function($) {
    console.log('evaluating chef');
    var $log = $('#chef .messages');

    var log = function(msg) {
        $log.html($log.html() + "<br/>" + msg);
    };

    $('#chef-get-cheese').click(function() {
       require(['cheese'], function() {
            log('ready with the cheese');
       });
    });

    $('#chef-get-steak').click(function() {
        require(['steak'], function() {
            log('ready with the steak');
        });
    });

    $('#chef-make-cheesesteak').click(function() {
        require(['cheeseSteak'], function() {
            log('ready with the cheeseSteak');
        });
    });
    log('ready');
    return {
        log:log
    };

});