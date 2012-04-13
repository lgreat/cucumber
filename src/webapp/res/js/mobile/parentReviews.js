define(['truncate','hogan'], function(truncate, hogan) {

    var next,container,pages,template={},cur=1;

    var init = function(nextEl, containerEl, totalPages, currentPage, templateHtml) {
        next = nextEl;
        container = containerEl;
        pages = totalPages;
        cur = currentPage;
        template = hogan.compile(templateHtml);

        // hide next element if there are no more pages
        if ( cur >= pages ) {
            $(next).hide();
        }
        // pull down next page
        else if ( pages > 1 ) {
            $(next).click(function(){
                nextPage();
            });
        }
    };

    // handle getting the next page
    var nextPage = function(){

        if ( cur < pages ){

            // remove page related params
            url = String(window.location).replace(/[\&\?](page=)([^\&]+)/, "").replace(/\.page/, '.json');

            // if this is the last page, hide the
            // view more button immediately
            if ( cur == ( pages-1 ) ) {
                $(next).hide();
            }

            // make the ajax call and render results
            var request = $.ajax({
                url: url,
                type:'get',
                data: {page: ( cur + 1 )}
            });

            request.done(function(data){
                render(data);
                cur++;
            });

            request.fail(function(xhr, txt){
                $(next).show();
            });
        }
    };

    // handle rendering of the next page
    var render = function(data) {
        // append anchor tag
        $(container).append($('<a></a>')
            .attr('name','page' + cur));

        for (i=0; i<data.reviews.length; i++){
            review = data.reviews[i];
            html = $('<div></div>').append(template.render({review:review}));
            truncate.init(html);
            $(container).append(html);
        }
    };

    return {
        init:init
    }
});