var GS = GS || {};

require.config(GS.requireConfig);

require(['global'], function(global){
   GS.log('global loaded');
});
require(['tracking'], function(tracking){
    GS.log('tracking loaded');
});
require(['order!jquery','order!jquery.mobile'], function($, jQueryMobile){
    GS.log('jquery and jquery mobile loaded');
});

// this is fine even if jquery and jquery mobile aren't done downloading/executing
// execution will begin after jquery has been executed (even if jquery.mobile isn't done)
require(['navigation'], function(navigation) {
    GS.log('navigation loaded');
    navigation.init(); // initialize navigation click handlers etc
});

