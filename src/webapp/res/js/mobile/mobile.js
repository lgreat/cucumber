define(['tracking','navigation','orient'],function(tracking,navigation,orient) {
    var handleFormField = function(){
        //$('body').on('focus blur', 'input, select, textarea', function(event) {        // doesn't work
        $('input, select, textarea').bind('focus blur', function(event) {

            if (event.type == 'focus') {
               // $('.clear_x').css('display', 'none');
                $(this).siblings().css('display', 'block');
            }
            /*  this is to handle the zoom event on iPhone - snaps back to correct size from zoom state - not perfect but better */
            if ( !( /iPhone|iPad|iPod/.test( navigator.platform ) && navigator.userAgent.indexOf( "AppleWebKit" ) > -1 ) ){
                return;
            }
            var $viewportMeta = $('meta[name="viewport"]');
            $viewportMeta.attr('content', 'width=device-width,initial-scale=1,maximum-scale=' + (event.type == 'blur' ? 10 : 1));
        });
    }

    var init = function() {
        $(document).ready(function (){
            $('#mobile-footer-view-full-site').click( function(){
                tracking.clear();
                tracking.successEvents = 'event70';
                tracking.send();

                var domain = "; domain=greatschools.org";
                if (location.hostname == "localhost" || location.hostname == "") {
                    domain = "";
                }
                document.cookie =
                    "org.springframework.mobile.device.site.CookieSitePreferenceRepository.SITE_PREFERENCE=NORMAL; path=/" + domain;
                var $this = $(this);
                var alternateSitePath = $this.attr("data-alternate-site-path");
                if (alternateSitePath) {
                    window.location = alternateSitePath;
                } else {
                    window.location.reload();
                }
                return false;
            }).show();

            $('#mobile-footer-iphone-app').click( function(){
                tracking.clear();
                tracking.successEvents = 'event71';
                tracking.send();
            });

            handleFormField();

            $('.clear_x').click( function(){
                $(this).siblings().val('');
                $(this).siblings().trigger('focus');
            });

            $('.js-email-school-link').on('click', function() {
                tracking.clear();
                tracking.successEvents = 'event67';
                tracking.send();
            });
        });

        navigation.init();
        orient.init();
    };

    return {
        init:init
    }
});
