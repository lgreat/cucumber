package gs.web.util;


import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.Entry;

public enum Url {

    // example of a static URL
    HOME("/index.page"),
    // example of a static URL that uses a query parameter
    RECENT_ACTIVITY("/account/?viewAllActivity=true"),
    // example of a dynamic url that requires parameter replacement
    CITY_HOME("/{state}/{city}/"),
    // example of how to specify a custom URL Processor
    CITY_BROWSE("/{state}/{city}/schools/", DefaultUrlProcessor.INSTANCE),
    DISTRICT_HOME("/{state}/{city}/{district}/"),
    DISTRICT_BROWSE("/{state}/{city}/{district}/schools/", DefaultUrlProcessor.INSTANCE),
    SEARCH_SCHOOLS_BY_NAME("/search/search.page?search_type=0&q={query}&state={stateAbbreviation}&c=school"),



    DISTRICTS_PAGE("/schools/districts/{state}/{stateAbbreviation}/")
    ;

    // the url path
    private final String _template;

    // matches everything in between the curly braces
    private static Pattern KEY_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    static {
        for (Url url : EnumSet.allOf(Url.class)) {
            url.initKeys();
        }
    }

    // a list of all the keys in the template
    private List<String> _keys;

    private boolean _containsQueryParameters;

    private static final Logger _log = Logger.getLogger(Url.class);

    private IUrlProcessor _urlBuilder;

    Url(String template) {
        this(template, DefaultUrlProcessor.INSTANCE);
    }

    Url(String template, IUrlProcessor urlBuilder) {
        _urlBuilder = urlBuilder;
        _template = template;

        _containsQueryParameters = _template.contains("?");
    }

    private void initKeys() {
        _keys = new ArrayList<String>();

        Matcher m = KEY_PATTERN.matcher(_template);
        while(m.find()){
            _keys.add(m.group());
        }
    }

    public String relative() {
        return _template;
    }
    
    public String relative(Map<String, String> keyVals, boolean putExtrasInQueryString) {
        String path = _template;
        
        for (Entry<String,String> entry : keyVals.entrySet()) {
            if (_keys.contains(entry.getKey())) {
                path.replaceAll("{" + entry.getKey() + "}", entry.getValue());
            } else if (putExtrasInQueryString) {
                path = UrlUtil.addParameter(path, keyVals + "=" + entry.getValue());
            }
        }
        
        return path;
    }

    public String relative(String... replacements) {
        return _urlBuilder.process(_template, replacements);
    }

    public String relative(State state, String city, String... replacements) {
        String stateReplacement = DirectoryStructureUrlFactory.getStateNameForUrl(state);
        String cityReplacement = DirectoryStructureUrlFactory.getCityNameForUrl(state, city);

        String[] combinedReplacements = new String[replacements.length + 2];

        combinedReplacements[0] = stateReplacement;
        combinedReplacements[1] = cityReplacement;
        System.arraycopy(replacements, 0, combinedReplacements, 2, replacements.length);

        return relative(combinedReplacements);
    }
    
    public String absolute(HttpServletRequest request, String... replacements) {
        return UrlUtil.buildHostAndPortString(request).toString() + relative(replacements);
    }
    
    public String relative(Map<String,String> queryParameters, String... replacements) {
        String path = relative(replacements);
        for (Map.Entry<String,String> entry : queryParameters.entrySet()) {
            path = UrlUtil.addParameter(path, entry.getKey() + "=" + entry.getValue());
        }
        return path;
    }
    
    public String absolute(HttpServletRequest request, Map<String,String> queryParameters, String... replacements) {
        return UrlUtil.buildHostAndPortString(request).toString() + relative(queryParameters, replacements);
    }

}

class DefaultUrlProcessor implements IUrlProcessor {
    public static final DefaultUrlProcessor INSTANCE;

    static {
        INSTANCE = new DefaultUrlProcessor();
    }

    private DefaultUrlProcessor() {}

    // matches the curly braces and everything in between
    private static final Pattern REPLACE_PATTERN = Pattern.compile("(\\{[^}]+\\})");

    public String process(String template, String... replacements) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = REPLACE_PATTERN.matcher(template);
        int position = 0;

        while (matcher.find() && position < replacements.length) {
            String key = matcher.group().substring(1,matcher.group().length()-1);
            matcher.appendReplacement(sb, getReplacement(key, replacements[position++]));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public String getReplacement(String key, String requestedReplacement) {
        if (key.equalsIgnoreCase("state") || key.equalsIgnoreCase("city") || key.equalsIgnoreCase("district")) {
            requestedReplacement = requestedReplacement.replaceAll("-", "_").replaceAll(" ", "-").replaceAll("/", "-").toLowerCase();
        }
        return requestedReplacement;
    }
}


