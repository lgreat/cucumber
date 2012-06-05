define(function() {
    var namespace = 'GS.';
    var enabled = !!window.localStorage;

    var setItem = function(key,value) {
        if (!enabled) {
            return false;
        }
        if (typeof value === "object") {
            value = JSON.stringify(value);
        }

        try {
            localStorage.setItem(namespace + key,value);
        } catch (err) {
            return false;
        }
        return true;
    };

    var getItem = function(key) {
        if (!enabled) {
            return null;
        }
        var value = localStorage.getItem(namespace + key);

        if (value === null) {
            return null;
        }

        if (value[0] === '{') {
            value = JSON.parse(value);
        }

        return value;
    };

    var removeItem = function(key) {
        if (!enabled) {
            return false;
        }
        localStorage.removeItem(namespace + key);
        return true;
    };

    return {
        enabled:enabled,
        setItem:setItem,
        getItem:getItem,
        removeItem:removeItem
    }

});