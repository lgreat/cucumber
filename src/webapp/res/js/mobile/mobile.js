define(['tracking','navigation'],function(tracking,navigation) {

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
                window.location.reload();
                return false;
            }).show();

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
            $('#shownav').hide();

            $('#topnav_link').click(function(){
                if($('#shownav').is(':hidden')){
                    $('#shownav').show('fast');
                    $('#topnav_link').find(".iconx24").removeClass('i-24-expand').addClass('i-24-collapse');
                    $('#topnav_link').removeClass('but-topnav').addClass('but-topnav-on');
                }
                else{
                    $('#shownav').hide('fast');
                    $('#topnav_link').find(".iconx24").removeClass('i-24-collapse').addClass('i-24-expand');
                    $('#topnav_link').removeClass('but-topnav-on').addClass('but-topnav');
                }
            });
        });

        navigation.init();
    };

    return {
        init:init
    }
});
