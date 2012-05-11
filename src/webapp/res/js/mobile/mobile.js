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
            /*  this is to handle the zoom event on iPhone - snaps back to correct size from zoom state - not perfect but better */
            var $viewportMeta = $('meta[name="viewport"]');
            $('input, select, textarea').bind('focus blur', function(event) {
                $viewportMeta.attr('content', 'width=device-width,initial-scale=1,maximum-scale=' + (event.type == 'blur' ? 10 : 1));
            });
        });

        navigation.init();
    };

    return {
        init:init
    }
});
