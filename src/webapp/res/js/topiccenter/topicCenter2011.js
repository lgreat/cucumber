function switchTabs(tabToShow, tabToHide) {

    //track user clicks on most popular tab in Omniture.
    if (tabToShow == 'js_mostPopularTab') {
        if (s.tl) {
            s.tl(true, 'o', 'Topic_Center_Most_Popular_Tab');
        }
    }
    var showTab = jQuery('#' + tabToShow);
    var hideTab = jQuery('#' + tabToHide);
    showTab.show();
    hideTab.hide();
}
