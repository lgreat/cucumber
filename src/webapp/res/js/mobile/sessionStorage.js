define(function() {
    var namespace = 'GS.';
    var enabled = !!window.sessionStorage;

    var setItem = function(key,value) {
        if (!enabled) {
            return false;
        }
        if (typeof value === "object") {
            value = JSON.stringify(value);
        }

        try {
            sessionStorage.setItem(namespace + key,value);
        } catch (err) {
            return false;
        }
        return true;
    };

    var getItem = function(key) {
        if (!enabled) {
            return null;
        }
        var value = sessionStorage.getItem(namespace + key);

        if (value === null) {
            return null;
        }

        if (value[0] === '{') {
            value = JSON.parse(value);
        }

        return value;
    };

    return {
        enabled:enabled,
        setItem:setItem,
        getItem:getItem
    }

});