define(['jquery'], function($) {

    var init = function() {
        // search for paragraph tags with 'data-more-text' attribute
        $('p[data-more-text]').each(function(){
            truncate(this);
            $(this).click(
                function() {
                    expand(this);
                }
            );
        });
    };

    // truncate the paragraph tag
    var truncate = function (el) {

        numberOfWords = 12;

        // copy original text
        text = $.trim(
            $(el).html()
        );

        if ( text.length > 0 ){

            // split words
            split = text.split(' ');
            shown = "";
            collapsed = "";

            // if too short to truncate
            if (split.length < numberOfWords) {
                shown = text;
            }
            // build shown text and collapsed text
            else {
                for (i=0; i<split.length; i++) {
                    if ( i < numberOfWords ) {
                        shown += split[i] + " ";
                    }
                    else {
                        // set collapsed text to rest of
                        if ( shown.length < text.length) {
                            collapsed = text.substring(shown.length-1, text.length);
                        }
                        break;
                    }
                }
            }

            // reset inner html element
            $(el).html('');
            $(el).append($('<span></span>').html(shown));

            // if there is more than just the shown text
            // add the more link
            if ( collapsed.length > 0 ) {

                // hide collapsed text
                $(el).append(
                    $('<span></span>').css('display','inline').addClass('collapsed').html(collapsed).hide());

                // add more link
                $(el).append(
                    $('<a></a>').addClass('more').html("More..."));

            }
        }
    }

    var expand = function (el) {
        $('a.more', el).hide();
        $('span.collapsed', el).show().css('display','inline');
    }

    return {
        init:init
    }
});