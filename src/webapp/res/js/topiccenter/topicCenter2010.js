// JavaScript Document
//
var myLevels = new Array('k', '1', '2', '3', '4', '5');
//var selectedLevel = myLevels[Math.floor(Math.random() * 6)];
var selectedLevel = 'k';
//
// required to avoid "$j" collisions with Prototype.js
var $j = jQuery;
//
$j(document).ready(function() {
    //
    /* ----------------------------------------------- */
    /* ELEMENTARY SCHOOL FEATURE AREA GRADE LEVEL TABS */
    /* ----------------------------------------------- */
    //

    $j('#es-gbg').show();  
    if ($j('#es-gbgTabs').length > 0) {

    // extend the core jQuery with disableSelection
    $j.fn.extend({
        disableSelection : function() {
            this.each(function() {
                this.onselectstart = function() {
                    return false;
                };
                this.unselectable = "on";
                $j(this).css('-moz-user-select', 'none');
            });
        }
    });
    //
    // disable selection of grade by grade
    $j('#featured').disableSelection();
    //
    // Position the tick marks
    //var tabContainerOrigin = $j('#es-gbgTabs').position().left;
    var tab = [];
    tab['k'] = $j('.es-gbg-tab-k');
    tab['1'] = $j('.es-gbg-tab-1');
    tab['2'] = $j('.es-gbg-tab-2');
    tab['3'] = $j('.es-gbg-tab-3');
    tab['4'] = $j('.es-gbg-tab-4');
    tab['5'] = $j('.es-gbg-tab-5');
    var tabPosition = [];
    var tabWidth = null;
    var tabMarkerPosition = [];
    //
    $j('#es-gbgTabs li').each(function () {
        var focusedTab = $j(this).attr('class').substring(11, 12);
        //console.log('selectedLevel: ' + focusedTab);
        tabPosition[focusedTab] = Math.round(tab[focusedTab].position().left);
        tabWidth = tab[focusedTab].outerWidth(true);
        //console.log('tabPosition'+focusedTab+': ' + tabPosition[focusedTab]);
        //console.log('tabWidth'+selectedLevel+': ' + tabWidth);
        tabMarkerPosition[focusedTab] = Math.round(tabPosition[focusedTab] + (tabWidth / 2));
        //console.log('tabMarkerPosition'+focusedTab+': ' + tabMarkerPosition[focusedTab]);
        $j('#es-gbg-marker-' + focusedTab).attr('style', 'left:' + tabMarkerPosition[focusedTab] + 'px;');
    });
    //
    // Present initial tab selected
    var content = [];
    content['k'] = $j('.featureGroup_1');
    content['1'] = $j('.featureGroup_2');
    content['2'] = $j('.featureGroup_3');
    content['3'] = $j('.featureGroup_4');
    content['4'] = $j('.featureGroup_5');
    content['5'] = $j('.featureGroup_6');
    $j('#featureContent > div').hide();
    content[selectedLevel].show();
    $j('.es-gbg-tab-' + selectedLevel).addClass('selected');
    // End - initial tab selected
    //
    var theHandle = $j('#es-gbgHandle');
    var theHandleWidth = theHandle.outerWidth(true);
    var halfHandleWidth = Math.round(theHandleWidth / 2);
    // Present initial handle position
    var initHandlePosition = $j('#es-gbg-marker-' + selectedLevel).position().left - halfHandleWidth;
    //console.log('initHandlePosition: '+initHandlePosition);
    theHandle.attr('style', 'left:' + initHandlePosition + 'px;');
    theHandle.show();
    // End - initial handle position
    //
    // On click of a tab, select that tab, reposition the handle
    $j('#es-gbgTabs ul li').click(function() {
        // Present the tab selected
        selectedLevel = $j(this).attr('class').substring(11, 12);
        $j('#featureContent > div').hide();
        content[selectedLevel].show();
        $j('#es-gbgTabs ul li').removeClass('selected');
        $j('.es-gbg-tab-' + selectedLevel).addClass('selected');
        // Reposition the handle to the tab selected
        var handlePosition = $j('#es-gbg-marker-' + selectedLevel).position().left - halfHandleWidth;
        theHandle.stop().animate({ 'left': handlePosition }, 200);
        return false;
    });
    // End tab select handling
    //
    //
    // Handle dragging //
    //
    var theSlider = $j('#es-gbgSlider');
    var theSliderWidth = theSlider.innerWidth();
    //console.log('theSliderWidth: ' + theSliderWidth);
    var theTabs = $j('#es-gbgTabs');
    var theTabsLeftPadding = parseInt(theTabs.css('paddingLeft'));
    var theTabsWidth = theTabs.innerWidth();
    var theTabsTrueWidth = theTabsWidth - theTabsLeftPadding;
    //console.log('theTabsWidth: ' + theTabsWidth);
    //console.log('theTabsTrueWidth: ' +theTabsTrueWidth+' = '+theTabsWidth+' - '+theTabsLeftPadding);
    //
    // Initialize handle variables and limit handle positioning
    var minpos = theTabsLeftPadding;
    //console.log('minpos: ' + minpos);
/*    if ($j.browser.mozilla) {
        var maxpos = theTabsWidth;

    } else {
        var maxpos = theTabsWidth - halfHandleWidth - 1;

    }*/
    var maxpos = theTabsWidth - theHandleWidth;
    //console.log('maxpos: ' + maxpos);
    var newpos = null;
    var sliderPositionLeft = $j('#es-gbgSlider').offset().left;
    //console.log('sliderPositionLeft: '+sliderPositionLeft);
    //
    // Set handle position, content, and tab after handle drag
    var setHandlePosition = function() {
        //console.log('newpos: '+newpos);
        switch (newpos >= minpos && newpos <= maxpos)
            //switch (true)
        {
            case (newpos >= tabPosition['k'] && newpos < (tabPosition['1']-halfHandleWidth)):
                selectedLevel = 'k';
                break;
            case (newpos >= (tabPosition['1']-halfHandleWidth) && newpos < (tabPosition['2']-halfHandleWidth)):
                selectedLevel = '1';
                break;
            case (newpos >= (tabPosition['2']-halfHandleWidth) && newpos < (tabPosition['3']-halfHandleWidth)):
                selectedLevel = '2';
                break;
            case (newpos >= (tabPosition['3']-halfHandleWidth) && newpos < (tabPosition['4']-halfHandleWidth)):
                selectedLevel = '3';
                break;
            case (newpos >= (tabPosition['4']-halfHandleWidth) && newpos < (tabPosition['5']-halfHandleWidth)):
                selectedLevel = '4';
                break;
            case (newpos >= (tabPosition['5']-halfHandleWidth) && newpos < theTabsWidth):
                selectedLevel = '5';
                break;
            default:
                alert("A valid position was not set.");
        }
        newpos = tabMarkerPosition[selectedLevel] - halfHandleWidth;
        theHandle.stop().animate({ 'left': newpos }, 200);
        $j('#es-gbgTabs ul li').removeClass('selected');
        $j('.es-gbg-tab-' + selectedLevel).addClass('selected');
        $j('#featureContent > div').hide();
        content[selectedLevel].show();
        return false;
    };
    //
    // Tie handle movement to mouse movement
    var mousemoveFunction = function(e) {
        //console.log('mouse move');
        //var pageCoords = "( " + e.pageX + ", " + e.pageY + " )";
        //var clientCoords = "( " + e.clientX + ", " + e.clientY + " )";
        //console.log(e.pageX);
        //console.log(e.clientX);
        newpos = e.pageX - halfHandleWidth - sliderPositionLeft;
        //console.log('newpos: '+newpos);
        if (newpos < minpos) {
            newpos = minpos;
        }
        if (newpos > maxpos) {
            newpos = maxpos;
        }
        //console.log('newpos: '+newpos);
        theHandle.stop().css('left', newpos + 'px');
    };
    //
    // Initialize mousing of handle
    var handleMousing = false;
    //
    // Tie mousing down on handle to possible handle repositioning
    theHandle.mousedown(function() {
        handleMousing = true;
        if (handleMousing) {
            //console.log('mouse down');
            $j('html').bind('mousemove', mousemoveFunction);
        }
    });
    //
    // Tie mousing up anywhere to stopping of handle repositioning
    $j('html').mouseup(function() {
        if (handleMousing) {
            //console.log('mouse up');
            $j('html').unbind('mousemove', mousemoveFunction);
            setHandlePosition();
        }
        handleMousing = false;
    });
    //
    // On click of slider region, select a tab, reposition the handle
    $j('#es-gbgSlider').click(function(e) {
        //console.log('clicked on the slider region');
        //var pageCoords = "( " + e.pageX + ", " + e.pageY + " )";
        //var clientCoords = "( " + e.clientX + ", " + e.clientY + " )";
        //console.log(e.pageX);
        //console.log(e.clientX);
        newpos = e.pageX - halfHandleWidth - sliderPositionLeft;
        //console.log('newpos: '+newpos);
        if (newpos < minpos) {
            newpos = minpos;
        }
        if (newpos > maxpos) {
            newpos = maxpos;
        }
        setHandlePosition();
    });

    }

    //
    //
    /* ------------------------------------------------------- */
    /* -- TOPIC CENTER AND GRADE LEVEL LEFT-HAND NAVIGATION -- */
    /* ------------------------------------------------------- */
    //
    // Initialize (all twirly content closed)
    var theTwirly = $j('#topicbarGS .hasNested > span');
    var theLink = $j('#topicbarGS .hasNested > a');
    //
    // On twirly click
    theTwirly.click(function () {
        ($j(this).hasClass('twirlyClosed')) ? $j(this).removeClass('twirlyClosed').addClass('twirlyOpen') : $j(this).removeClass('twirlyOpen').addClass('twirlyClosed');
        ($j(this).hasClass('twirlyClosed')) ? $j(this).parent().find('ul').slideUp(250) : $j(this).parent().find('ul').slideDown(250);
        return false;
    });
   //
   // On link click
    theLink.click(function () {
        ($j(this).parent().find('span').hasClass('twirlyClosed')) ? $j(this).parent().find('span').removeClass('twirlyClosed').addClass('twirlyOpen') : $j(this).parent().find('span').removeClass('twirlyOpen').addClass('twirlyClosed');
        ($j(this).parent().find('span').hasClass('twirlyClosed')) ? $j(this).parent().find('ul').slideUp(250) : $j(this).parent().find('ul').slideDown(250);
        return false;
    });

    /* ------------------------------------------------ */
    /* -- TOPIC CENTER AND GRADE LEVEL FIND A SCHOOL -- */
    /* ------------------------------------------------ */

    // change state
    $j('#topSchoolsStateSelector').change(function() {
        var url = '/accountInformationAjax.page';
        var pars = 'state=' + $j(this).val() + '&showNotListed=false';
        var cityDiv = $j('#topCitiesCityListSpan');
        var citySelect = $j('#topCitiesCityList');
        var cityLoading = $j('#topCitiesCityLoadingSpan');
        var stateSelect = $j('#topSchoolsStateSelector');

        citySelect.hide();
        stateSelect.hide();

        cityLoading.html('<span>&nbsp;&nbsp;Loading ...</span>');
        cityLoading.show();

        $j.get(url, {state: $j(this).val(), showNotListed: 'false'}, function(data) {
            citySelect.html(data);
            citySelect.show();
            cityLoading.html('');
            cityLoading.hide();
            stateSelect.show();
           });
    });
});


// Returns true if a state has been selected.
function checkStateSelection(id) {
    var select = document.getElementById(id);
    if (select.options[select.selectedIndex].value == "") {
        alert("Please select a state");
        return false;
    } else {
        return true;
    }
}

// Swaps text value with another
function textSwitch(el, target, replace) {
    if (el.value == replace) {
        el.value = target;
    }
}

function changeCity(cityName, stateAbbr) {
    var newHref = window.location.href;
    if (newHref.indexOf('?') > 0) {
        newHref = newHref.substring(0, newHref.indexOf('?'));
    }
    newHref = newHref + '?city=' + encodeURIComponent(cityName) + '&state=' + stateAbbr + '#findASchool';
    window.location.href = newHref;
}


 function findAndCompareCheck(queryId, stateSelectorId, pathwaysId) {
    var returnVal = true;
    var queryVal = document.getElementById(queryId).value;
    var stateVal = document.getElementById(stateSelectorId).value;

    var noSearchTerms = (queryVal == 'Enter school, city, or district');
    var noState = (stateVal == ("-" + "-") || stateVal == "");

    if (noSearchTerms && noState) {
        // go to national R&C (/school/research.page)
        window.location.href = '/school/research.page';
        returnVal = false;
    } else if (!noSearchTerms && noState) {
        // show javascript alert
        alert("Please select a state.");
        returnVal = false;
    } else if (noSearchTerms && !noState) {
        // go to state R&C (/school/research.page)
        window.location.href = '/school/research.page?state='+stateVal;
        returnVal = false;
    }
    return returnVal;
}

// EOF