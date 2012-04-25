define(['tracking','navigation'],function(tracking,navigation) {


    var init = function() {
        $(document).ready(function (){
            $('#mobile-footer-view-full-site').click( function(){
                tracking.clear();
                tracking.successEvents = 'event70';
                tracking.send();
            });

            $('#mobile-footer-iphone-app').click( function(){
                tracking.clear();
                tracking.successEvents = 'event71';
                tracking.send();
            });
        });

        navigation.init();
    };

    return {
        init:init
    }
});
