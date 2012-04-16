var GS = GS || {};

require.config(GS.requireConfig);

require(['order!jQuery','order!mobile'], function(jQuery, mobile) {
    GS.log('main mobile module and its dependencies loaded.');
    mobile.init();
});

