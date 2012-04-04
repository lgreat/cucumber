var GS = GS || {};

require.config(GS.requireConfig);

require(['order!jquery','order!jquery.mobile', 'mobile'], function($,jQueryMobile, mobile) {
    GS.log('main mobile module and its dependencies loaded.');
    mobile.init();
});

