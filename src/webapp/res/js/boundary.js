var Boundary = (function (){
    var $map, $dropdown, $header, $list, $level, $search, $priv, $charter, $redo, currentLevel = 'e';
    var init = function (map, dropdown, header, list, level, search, priv, charter, redo ) {
        $map = $(map), $dropdown = $(dropdown), $header = $(header)
            , $list = $(list), $level = $(level), $priv = $(priv)
            , $charter = $(charter), $search = $(search), $redo = $(redo);

        $redo.hide();
        updateEventListeners();
        $map.boundaries({type: 'districts', level: 'e', schools: true, autozoom: false, info: true, infoWindowMarkupCallback: infoWindowMarkupCallback});
    }

    var infoWindowMarkupCallback = function ( obj ) {
        var id = (obj.type=='school') ? '#boundaryMapSchoolInfoWindow' : '#boundaryMapDistrictInfoWindow'
            , $element = $(id).clone()
            , $link = $('<a></a>').attr('href', obj.url).html(obj.name)
            , $rating = $('<span class="sprite badge_sm_na"></span>')
            , $homes = $($element.find('.js_homesforsale'))
            , $wrapper = $('<div class="mod standard_5-1 mbm"></div>')
            , address = '';

        if ( obj.rating > 0 && obj.rating < 11) $rating.removeClass('badge_sm_na').addClass('badge_sm_' + obj.rating);
        if (obj.address.street1) address += obj.address.street1 + '<br/>';
        if (obj.address.cityStateZip) address += obj.address.cityStateZip;
        (obj.address.zip) ?
            $homes.show().find('a').attr('href', 'http://www.realtor.com/realestateandhomes-search/'+ obj.address.zip + '?gate=gs&cid=PRT300014').attr('target', '_blank') :
            $homes.hide();
        $element.find('.js_name').html($link);
        $element.find('.js_rating').html($rating);
        $element.find('.js_address').html(address);

        if (obj.type=='school') {
            var $comments = $element.find('.js_comments');
            $wrapper.removeClass("mbm");
            $comments.html('');
            if (!obj.isPolygonShown()) $comments.html('<div class="ft smaller bottom"><div class="media attribution"><div class="img"><span class="iconx16 i-16-information"><!-- do not collapse --></span></div><div class="bd">Contact school district for school boundaries</div></div></div>');
            if (obj.schoolType=='private') $comments.append('<div class="ft smaller bottom"><div class="media attribution"><div class="img"><span class="iconx16 i-16-information"><!-- do not collapse --></span></div><div class="bd">Private schools are not in the district.</div></div></div>');
            if (obj.schoolType=='charter' && !((obj.schoolType == 'charter' && obj.districtId)))
                $comments.append('<div class="ft smaller bottom"><div class="media attribution"><div class="img"><span class="iconx16 i-16-information"><!-- do not collapse --></span></div><div class="bd">Charter schools are not in the district.</div></div></div>');
        }
        $wrapper.append($element);
        return $('<div></div>').append($wrapper).html();
    };

    var updateSchoolList = function ( schools ){
        $list.empty();
        var itemTemplate = '<div class="js-listItem media attribution pvs phm" style="border-bottom: 1px solid #f1f1f1"></div>'
            , spriteTemplate = '<div class="img round-small mrm"><!--Do not collapse--></div>'
            , nameTemplate = '<div class="bd" id=""></div>'
            , htmlString = '';
        schools.sort(sort);
        for (var i = 0; i < schools.length; i++) {
            var school = schools[i]
                , schoolRating = 'na'
                , $name = $(nameTemplate)
                , $sprite = $(spriteTemplate)
                , badge = (school.rating > 0 && school.rating < 11) ? school.rating :
                    (school.schoolType=='private') ? 'PR' : 'N/A';
            if (school.schoolType!='private' && !(school.schoolType=='charter' && !school.districtId)){
                $sprite.append(badge);
                $name.append(school.name);
                var $listItem = $(itemTemplate).attr('id',school.getKey()).append($sprite).append($name).attr('id', school.getKey());
                $listItem.on('click', function(){
                    $('.js-listItem').removeClass('selected');
                    $(this).addClass('selected');
                    var val = $(this).attr('id');
                    $map.boundaries('focus', val);
                });
                $list.append($listItem);
            }
        }
        (schools.length>0) ? $('#schoolListWrapper').show():$('#schoolListWrapper').hide();
    }

    var updateEventListeners = function (){
        $map.on('init.boundaries', initEventHandler );
        $map.on('focus.boundaries', focusEventHandler );
        $map.on('districts.boundaries', districtsEventHandler );
        $map.on('schools.boundaries', schoolsEventHandler );
        $map.on('geocode.boundaries', geocodeEventHandler );
        $map.on('moved.boundaries', movedEventHandler );
        $dropdown.on('change', dropdownEventHandler);
        $level.on('change', levelEventHandler);
        $search.on('submit', searchEventHandler);
        $priv.on('click', privateEventHandler);
        $charter.on('click', charterEventHandler);
        $redo.on('click', redoEventHandler);
    };

    var sort = function (a, b) {
        if (a.name == b.name) return 0;
        return (a.name < b.name) ? -1 : 1;
    }

    var initEventHandler = function(event, data) {
        $('.js_showWithMap').show();
    };

    var districtsEventHandler = function (event, obj) {
        $dropdown.html('');
        obj.data.sort(sort);
        $dropdown.append($('<option></option>').html('Select a district'));
        for( var i=0; i<obj.data.length; i++) {
            $dropdown.append($('<option></option>').html(obj.data[i].name).val(obj.data[i].getKey()));
        }
        if ($priv.prop('checked')) $map.boundaries('nondistrict', 'private');
        if ($charter.prop('checked')) $map.boundaries('nondistrict', 'charter');
    };

    var schoolsEventHandler = function (event, obj) {
        updateSchoolList(obj.data);
    }

    var dropdownEventHandler = function (event) {
        var val = $dropdown.val();
        $map.boundaries('focus', val);
    }

    var focusEventHandler = function (event, obj) {
        if (obj.data.type=='district'){
            $dropdown.val(obj.data.getKey());
        }
        if (obj.data.type=='school'){
            $('.js-listItem').removeClass('selected');
            var $this = $('.js-listItem[id=' + obj.data.getKey() + ']');
            $this.addClass('selected');

            // position should be between 0 and height.
            // parentElem must be relatively positioned!
            var elemTop = $this.position().top;
            var isScrolledIntoView = elemTop > 0 && elemTop < $('#schoolListDiv').height();

            if ($this.position() != null && isScrolledIntoView === false) {
                var scrollTop = $('#schoolListDiv').scrollTop();
                $('#schoolListDiv').scrollTop(scrollTop + $this.position().top);
            }
        }
    };

    var movedEventHandler = function ( event ) {
        $redo.show();
    }

    var geocodeEventHandler = function ( event, obj ) {
        updateHistory('?lat='+obj.data.lat()+'&lon='+obj.data.lng()+'&level='+currentLevel);
        $redo.hide();
        $map.boundaries('district');
    }

    var updateHistory = function ( params ){
        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            window.History.replaceState(null, document.title, params);
        } else {
            window.location = window.location.pathname + '' + params;
        }
    }

    var levelEventHandler = function ( event ) {
        var val = $(this).val();
        $map.boundaries('option', {level: val});
        $list.html('');
        currentLevel = val;
        var lat = $map.data('boundaries').map.getCenter().lat()
            , lon = $map.data('boundaries').map.getCenter().lng();
        updateHistory('?lat='+lat+'&lon='+lon+'&level='+currentLevel);
    }

    var searchEventHandler = function ( event ) {
        var val = $('#js_mapAddressQuery').val();
        $map.boundaries('geocode', val);
    }

    var charterEventHandler = function ( event ) {
        if ($(this).prop('checked')) $map.boundaries('nondistrict', 'charter');
        else $map.boundaries('hideNonDistrict', 'charter');
    }

    var privateEventHandler = function ( event ) {
        if ($(this).prop('checked')) $map.boundaries('nondistrict', 'private');
        else $map.boundaries('hideNonDistrict', 'private');
    }

    var redoEventHandler = function (){
        $map.boundaries('districts').boundaries('district');
    }

    return {
        init: init
    }
});