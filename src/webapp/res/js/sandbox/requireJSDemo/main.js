require.config({
    baseUrl: '/res/js/sandbox/requireJSDemo/',
    paths: {
    }
});


require(['order!jquery'], function($){
    console.log('jquery and jquery mobile ready');
});

require(['jquery'], function($) {
    console.log('jquery ready');
});

require(['chef'], function(chef) {

});
