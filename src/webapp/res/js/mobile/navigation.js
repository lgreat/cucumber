//when 'navigation' module is "require()d", execution will begin as soon as 'jquery' is available
define(function() {
    var $container = null;
    var $topnav_link = null;
    var $shownav = null;

    $(function() {
        $container = $('.js-mobile-header');
    });

    var setupEventHandlers = function() {
        $(function() {
            $('#js-navigation-findSchool').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Search');}
            });

            $('#js-navigation-my-saved-schools').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Saved_Schools');}
            });

            $('#js-navigation-subscribe-to-newsletter').click( function(){
                if (s.tl) {s.tl(this,'o', 'Mobile_Global_Newsletter');}
            });

            $topnav_link = $('#topnav_link');
            $shownav = $('#shownav');
            $shownav.hide();
            $topnav_link.click(function(){
                if($shownav.is(':hidden')){
                    openTopNav();
                } else{
                    closeTopNav();
                }
                return false; // do not propagate
            });
        })
    };

    // iPhone Safari doesn't always bubble click events to the body element, so I need to use js-global-event-catcher
    var closeTopNav = function() {
        $shownav.hide('fast');
        $topnav_link.find(".iconx24").removeClass('i-24-collapse').addClass('i-24-expand');
        $topnav_link.removeClass('but-topnav-on').addClass('but-topnav');
        $('#js-global-event-catcher').off('click.navigationCloseTopNav');
    };

    var openTopNav = function() {
        $shownav.show('fast');
        $topnav_link.find(".iconx24").removeClass('i-24-expand').addClass('i-24-collapse');
        $topnav_link.removeClass('but-topnav').addClass('but-topnav-on');
        $('#js-global-event-catcher').on('click.navigationCloseTopNav',  function(event) {
            var $eventTarget = $(event.target);
            // close navigation if the user clicks anywhere else
            if ($shownav.find($eventTarget).length == 0 && !$eventTarget.is($topnav_link) && !$eventTarget.is($shownav)) {
                closeTopNav();
            }
        });
    };

    var init = function() {
        setupEventHandlers();
        setTimeout(function() {
            // Hide the address bar!
            window.scrollTo(0, 1);
        }, 0);
    };

    var $getContainer = function() {
        return $container.filter(':visible');
    };

    return {
        $getContainer:$getContainer,
        init:init,
        closeTopNav:closeTopNav,
        openTopNav:openTopNav
    };
});