var Boundary = (function (){
    var $map, $dropdown, $header, $list, $level, $search, $priv, $charter, $redo, currentLevel = 'e';

    var init = function (map, dropdown, header, list, level, search, priv, charter, redo ) {
        $map = $(map), $dropdown = $(dropdown), $header = $(header)
            , $list = $(list), $level = $(level), $priv = $(priv)
            , $charter = $(charter), $search = $(search), $redo = $(redo);

        $redo.hide();

        updateEventListeners();

        var params = getUrlParams()
            , paramsSet = false;

        var options = {type:'districts', infoWindow: infoWindowMarkupCallback, autozoom: false};
        if (params.level){
            options.level = params.level;
        }
        if (params.lat && params.lon) {
            paramsSet = true;
        }


        $map.boundaries(options);
        if (params.address) {
            $search.find('#js_mapAddressQuery').val(params.address);
            $map.boundaries('geocode', params.address);
        }
        if (paramsSet){
            $map.boundaries('center', new google.maps.LatLng(params.lat, params.lon));
            $map.boundaries('districts');
        }
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
        if ( obj.type=='school'){
            if (obj.address.street1) address += obj.address.street1 + '<br/>';
            if (obj.address.cityStateZip) address += obj.address.cityStateZip;
            (obj.address.zip) ?
                $homes.show().find('a').attr('href', 'http://www.realtor.com/realestateandhomes-search/'+ obj.address.zip + '?gate=gs&cid=PRT300014').attr('target', '_blank') :
                $homes.hide();

        }
        else if (obj.type == 'district' && obj.counts){
            var array = new Array();
            array.push('Elementary (' + obj.counts['e'] + ')');
            array.push('Middle (' + obj.counts['m'] + ')');
            array.push('High (' + obj.counts['h'] + ')');

            if (array.length) {
                address = array.join(', ');
            }
        }
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
                $listItem.data('school', school);
                $listItem.on('click', function(){
                    $('.js-listItem').removeClass('selected');
                    $(this).addClass('selected');
                    var val = $(this).data('school');
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
        $map.on('load.boundaries', loadEventHandler );
        $map.on('geocode.boundaries', geocodeEventHandler );
        $map.on('moved.boundaries', movedEventHandler );
        $map.on('mapclick.boundaries', mapClickEventHandler );
        $map.on('markerclick.boundaries', markerClickEventHandler);
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

    var addDropdownItem = function(district) {
        var $option = $('<option></option>');
        $option.data('district', district);
        $dropdown.append($option.html(district.name).val(district.getKey()));
    }

    var districtsEventHandler = function (event, obj) {
        $dropdown.html('');
        obj.data.sort(sort);
        $dropdown.append($('<option></option>').html('Select a district'));
        for( var i=0; i<obj.data.length; i++) {
            addDropdownItem(obj.data[i]);
        }
        if ($priv.prop('checked')) $map.boundaries('nondistrict', 'private');
        if ($charter.prop('checked')) $map.boundaries('nondistrict', 'charter');
        $map.boundaries('district');
    };

    var loadEventHandler = function (event, obj) {
        if (typeof obj == 'object' && obj.data ) {
            if (obj.data.length) {
                if (obj.data[0].type=='school'){
                    schoolsEventHandler(event, obj);
                } else {
                    districtsEventHandler(event, obj);
                }
            }
            else if (obj.data.getType()=='district') {
                addDropdownItem(obj.data);
                $dropdown.val(obj.data.getKey());
            }
        }
    }

    var schoolsEventHandler = function (event, obj) {
        updateSchoolList(obj.data);
    }

    var dropdownEventHandler = function (event) {
        var option = $dropdown.find('option:selected');
        if (option.length) {
            var district = $(option[0]).data('district');
            $map.boundaries('focus', district);
            fireCustomLink('Dist_Bounds_Map_Select_District');
        }
    };

    var mapClickEventHandler = function ( event, obj ){
        if (!$dropdown.find('option').length){
            $map.boundaries('districts', obj.data);
        }
        $map.boundaries('district', obj.data);
        fireCustomLink('Dist_Bounds_Map_Enroll_Bound_Click');
    }

    var markerClickEventHandler = function( event, obj) {
        if (obj.data && obj.data.type=='school') fireCustomLink('Dist_Bounds_Map_School_Pin_Click');
        if (obj.data && obj.data.type=='district') fireCustomLink('Dist_Bounds_Map_Dist_Pin_Click');
    }

    var focusEventHandler = function (event, obj) {
        if (obj.data.type=='district'){
            $dropdown.val(obj.data.getKey());
            updateDistrictHeader(obj.data);

        }
        if (obj.data.type=='school'){
            $('.js-listItem').removeClass('selected');
            var $this = $('.js-listItem[id=' + obj.data.getKey() + ']');
            if ($this && $this.position()) {
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
        }
    };

    var movedEventHandler = function ( event ) {
        $redo.show();
    }

    var geocodeEventHandler = function ( event, obj ) {
        updateHistory('?lat='+obj.data[0].lat+'&lon='+obj.data[0].lon+'&level='+currentLevel);
        $redo.hide();
        $map.boundaries('district');
    };

    var updateDistrictHeader = function( district ){
        $header.removeClass('hidden');
        $header.find('#ratings-test').html(district.rating);
        $header.find('#school-name-test').html(district.name);
    };


    var updateHistory = function ( params ){
        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            window.History.replaceState(null, document.title, params);
        } else {
            window.location = window.location.pathname + '' + params;
        }
    };

    var levelEventHandler = function ( event ) {
        var val = $(this).val();
        $map.boundaries('level', val);
        $list.html('');
        currentLevel = val;
        var lat = $map.data('boundaries').getMap().getCenter().lat()
            , lon = $map.data('boundaries').getMap().getCenter().lng();
        updateHistory('?lat='+lat+'&lon='+lon+'&level='+currentLevel);
    }

    var searchEventHandler = function ( event ) {
        var val = $('#js_mapAddressQuery').val();
        $map.boundaries('geocode', val);
        firePageView('BoundariesMap');
    }

    var charterEventHandler = function ( event ) {
        if ($(this).prop('checked')){
            $map.boundaries('nondistrict', 'charter');
            fireCustomLink('Dist_Bounds_Map_Show_Charter');
        }
        else $map.boundaries('hide', 'charter');
    }

    var privateEventHandler = function ( event ) {
        if ($(this).prop('checked')){
            $map.boundaries('nondistrict', 'private');
            fireCustomLink('Dist_Bounds_Map_Show_Private');
        }

        else $map.boundaries('hide', 'private');
    }

    var redoEventHandler = function (){
        $map.boundaries('refresh');
        privateEventHandler();
        charterEventHandler();
        fireCustomLink('Dist_Bounds_Map_Redo_Search');
    }

    var getUrlParams = function(){
        var urlParams = {};
        var e,
            a = /\+/g,  // Regex for replacing addition symbol with a space
            r = /([^&=]+)=?([^&]*)/g,
            d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
            q = window.location.search.substring(1);

        while (e = r.exec(q))
            urlParams[d(e[1])] = d(e[2]);

        return urlParams;
    };

    var fireCustomLink = function (name) {
        if (s.tl) {
            s.tl(true, 'o', name);
        }
    }

    var firePageView = function (name) {
        if (s) {
            s.prop45 = clickCapture.getProp(45,$('#js_mapAddressQuery').val());
            s.prop46 = clickCapture.getProp(46,currentLevel);
        }
        pageTracking.pageName = name;
        pageTracking.send();
    }

    return {
        init: init,
        getUrlParams: getUrlParams
    }
});