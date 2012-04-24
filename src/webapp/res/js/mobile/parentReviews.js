define(function() {

    var next,container,pages,cur=1;

    var init = function(nextEl, containerEl, totalPages, currentPage) {
        next = nextEl;
        container = containerEl;
        pages = totalPages;
        cur = currentPage;

        // hide next element if there are no more pages
        if ( cur >= pages ) {
            $(next).hide();
        }
        // pull down next page
        else if ( pages > 1 ) {
            $(next).click(function(){
                nextPage();
                return false;
            });
        }
    };

    // handle getting the next page
    var nextPage = function(){

        if ( cur < pages ){

            // remove page related params
            var url = String(window.location).replace(/[\&\?](page=)([^\&]+)/, "");

            // if this is the last page, hide the
            // view more button immediately
            if ( cur == ( pages-1 ) ) {
                $(next).hide();
            }

            // make the ajax call and render results
            var request = $.ajax({
                url: url,
                type:'get',
                data: { page: ( cur + 1 ), ajax: true, decorator: 'emptyDecorator', confirm: true }
            });

            request.done(function(data){
                $(container).html($(container).html() + data);
                cur++;
            });

            request.fail(function(xhr, txt){
                $(next).show();
            });
        }
    };

    return {
        init:init
    }
});