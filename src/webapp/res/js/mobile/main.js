require.config({
    baseUrl: '/res/js/mobile/',
    paths: {
    }
});

require(['order!jquery','order!jquery.mobile'], function($, jQueryMobile){
    console.log('jquery and jquery mobile loaded');
});

// this is fine even if jquery and jquery mobile aren't done downloading/executing
// execution will begin after jquery has been executed (even if jquery.mobile isn't done)
require(['navigation'], function(navigation) {
    console.log('navigation loaded');
    navigation.init(); // initialize navigation click handlers etc
});

