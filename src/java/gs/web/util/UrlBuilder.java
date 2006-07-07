/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilder.java,v 1.45 2006/07/07 19:54:40 apeterson Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.geo.ICity;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.school.SchoolsController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Provides a builder utility for our URLs. Deals with the intricacies of:
 * <ol>
 * <li>URL encoding
 * <li>modifying an existing URL string or parameters
 * <li>multiple parameters with the same name
 * <li>deleting parameters
 * <li>our specific pages, or "vpages". This provides a centralized place to
 * create URLs. These are fundamentally separate concepts, but they are
 * intertwined here, at least for the time being.
 * </ol>
 * </p>
 * In Java code, use this class to build URLs. On Jsps and within Tag files,
 * use the "link" taglib, which is a thin wrapper around this class.
 * </p>
 * Test coverage should be near 100% for this class, as its functionality is
 * quite critical.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see <a href="http://www.rfc-editor.org/rfc/rfc1738.txt">RFC 1738</a>
 * @see UrlUtil
 * @see gs.web.jsp.link
 * @see gs.web.jsp.link.LinkTagHandler
 */
public class UrlBuilder {

    private static final Log _log = LogFactory.getLog(UrlBuilder.class);


    /**
     * Path relative to the host/context.
     */
    private String _path;
    private Map _parameters;
    private boolean _perlPage = false;
    private VPage _vPage; // used for some urls
    private static UrlUtil _urlUtil = new UrlUtil();

    /**
     * Provides type-safety for identifying our unique "pages".
     */
    public static class VPage {

        private String _name;

        private VPage(String s) {
            _name = s;
        }
    }

    /**
     * A page that provides an overview of our entire article library.
     */
    public static final VPage ARTICLE_LIBRARY = new VPage("vpage:articleLibrary");

    public static final VPage CITY_PAGE = new VPage("vpage:city");
    public static final VPage CITIES = new VPage("vpage:cities"); // all the cities in a state
    public static final VPage CITIES_MORE_NEARBY = new VPage("vpage:moreNearbyCities");

    public static final VPage CONTACT_US = new VPage("vpage:contactUs");

    public static final VPage DISTRICT_PROFILE = new VPage("vpage:districtProfile");

    /**
     * Allows a user to edit and create school lits.
     */
    public static final VPage MY_SCHOOL_LIST = new VPage("vpage:mySchoolList");
    /**
     * Information about newsletters.
     */
    public static final VPage NEWSLETTER_CENTER = new VPage("vpage:newsletterCenter");
    /**
     * Manage new or existing subscriptions.
     */
    public static final VPage NEWSLETTER_MANAGEMENT = new VPage("vpage:newsletterManagement");

    /**
     * New state home page: research and compare, with optional state.
     */
    public static final VPage RESEARCH = new VPage("vpage:research");

    public static final VPage SCHOOL_PROFILE = new VPage("vpage:schoolProfile");
    /**
     * Parent reviews about a specific school.
     */
    public static final VPage SCHOOL_PARENT_REVIEWS = new VPage("vpage:schoolParentReviews");

    public static final VPage SCHOOLS_IN_CITY = new VPage("vpage:schoolsInCity");
    public static final VPage SCHOOLS_IN_DISTRICT = new VPage("vpage:schoolsInDistrict");

    public static final VPage PRIVACY_POLICY = new VPage("vpage:privacyPolicy");

    public static final VPage BETA_SIGNUP = new VPage("vpage:betaSignup");
    public static final VPage BETA_UNSUBSCRIBE = new VPage("vpage:betaUnsubscribe");

    public static final VPage SIGN_IN = new VPage("vpage:signIn");
    public static final VPage SIGN_OUT = new VPage("vpage:signOut");

    public static final VPage ADMIN_NEWS_ITEMS = new VPage("vpage:newItems");
    public static final VPage ADMIN_NEWS_ITEMS_CREATE = new VPage("vpage:newItemsCreate");
    public static final VPage ADMIN_NEWS_ITEMS_DELETE = new VPage("vpage:newItemsDelete");

    /**
     * Page that allows users to search for a school in order to add a parent review
     */
    public static final VPage ADD_PARENT_REVIEW_SEARCH = new VPage("vpage:addParentReviewSearch");


    /**
     * Create a builder to the given site page.
     *
     * @param request             the current request
     * @param contextRelativePath the requested page. Null asks for the same page, but it may or
     *                            may not work. It would be great if you could always generate the link to the
     *                            current page, but unfortunately this isn't available at all points.
     *                            Tomcat, or possibly spring, has decorated the request so that it
     *                            doesn't point to what the user really asked for. If you're in the midst of processing a page, it now points to the
     *                            Jsp page that is being shown, not the user's request. It does seem to work in the controller, though.
     *                            I know of one solution that I haven't implemented here:
     *                            Grab the path earlier on in the servlet processing
     *                            and stash it away for later retrieval.
     * @deprecated use VPage-oriented methods when possible
     */
    public UrlBuilder(HttpServletRequest request, String contextRelativePath) {
        _path = contextRelativePath;
        if (contextRelativePath == null) {
            _path = request.getRequestURI();
            _path = StringUtils.removeStart(_path, request.getContextPath());
        } else {
            _path = contextRelativePath;
        }
        _perlPage = _urlUtil.smellsLikePerl(_path);
        // _log.error("PathInfo="+request.getPathInfo()); // yields null
        // _log.error("PathTranslated="+request.getPathTranslated()); // yields null
        // _log.error("ServletPath="+request.getServletPath()); // yields "/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURI="+request.getRequestURI()); // yields "/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURL="+request.getRequestURL()); // yields "http://apeterson.office.greatschools.net:8080/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
    }


    /**
     * Create a link to an article
     *
     * @param featured should the "featured" url be used instead of the normal one. This is
     */
    public UrlBuilder(Article article, State s, boolean featured) {
        _perlPage = true;

        // Calculate page to use
        String page;
        if (featured) {
            page = "showarticlefeature";
        } else {
            page = "showarticle";
        }

        // Calculate link
        // TH: Commented this out because buildHref is noop with a null request
        Integer id = article != null ? article.getId() : new Integer(1);
        _path = "/cgi-bin/" +
                page +
                "/" +
                s.getAbbreviationLowerCase() +
                "/" +
                id;
    }

    public UrlBuilder(School school, VPage page) {
        if (SCHOOL_PROFILE.equals(page)) {
            _perlPage = true;
            if (school.getType().equals(SchoolType.PRIVATE)) {
                _path = "/cgi-bin/" +
                        school.getDatabaseState().getAbbreviationLowerCase() +
                        "/private/" + school.getId();
            } else {
                _path = "/modperl/browse_school/" +
                        school.getDatabaseState().getAbbreviationLowerCase() +
                        "/" + school.getId();
            }
        } else if (SCHOOL_PARENT_REVIEWS.equals(page)) {
            _perlPage = true;
            _path = "/modperl/parents/" +
                    school.getDatabaseState().getAbbreviationLowerCase() +
                    "/" +
                    school.getId();
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(District district, VPage page) {
        if (DISTRICT_PROFILE.equals(page)) {
            _perlPage = true;

            _path = "/cgi-bin/" +
                    district.getDatabaseState().getAbbreviationLowerCase() +
                    "/district_profile/" +
                    district.getId();
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(ICity city, VPage page) {
        _vPage = page;
        if (CITY_PAGE.equals(page)) {
            _perlPage = false;
            _path = "/city/";
            this.setParameter("city", city.getName());
            this.setParameter("state", city.getState().getAbbreviation());
        } else if (CITIES_MORE_NEARBY.equals(page)) {
            _perlPage = false;
            _path = "/cities.page";
            this.setParameter("city", city.getName());
            this.setParameter("state", city.getState().getAbbreviation());
        } else if (SCHOOLS_IN_CITY.equals(page)) {
            _perlPage = false;
            _path = "/schools.page";
            this.setParameter("city", city.getName());
            this.setParameter("state", city.getState().getAbbreviation());

        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    /**
     * @param levelCode  can be null
     * @param schoolType can be null, can also be a comma-separated list
     */
    public UrlBuilder(ICity city, VPage page, LevelCode levelCode, String schoolType) {
        this(city, page); // use existing method to set city/state
        if (SCHOOLS_IN_CITY.equals(page)) {
            if (levelCode != null && StringUtils.isNotEmpty(levelCode.getCommaSeparatedString())) {
                String[] lcs = StringUtils.split(levelCode.getCommaSeparatedString(), ",");
                for (int i = 0; i < lcs.length; i++) {
                    addParameter(SchoolsController.PARAM_LEVEL_CODE, lcs[i]);
                }
            }
            if (schoolType != null) {
                if (StringUtils.isNotEmpty(schoolType)) {
                    String[] sts = StringUtils.split(schoolType, ",");
                    for (int i = 0; i < sts.length; i++) {
                        addParameter(SchoolsController.PARAM_SCHOOL_TYPE, sts[i]);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    public UrlBuilder(VPage page, State state) {
        init(page, state, null);
    }

    public UrlBuilder(VPage page, State state, String param0) {
        init(page, state, param0);
    }

    /**
     * @param levelCode  can be null
     * @param schoolType can be null, can also be a comma-separated list
     */
    public UrlBuilder(VPage page, State state, String city, LevelCode levelCode, String schoolType) {
        init(page, state, city, levelCode, schoolType);
    }

    /**
     * @param levelCode  can be null
     * @param schoolType can be null, can also be a comma-separated list
     */
    private void init(VPage page, State state, String city, LevelCode levelCode, String schoolType) {
        _vPage = page;

        // use existing code to set state/city
        init(page, state, city);

        if (SCHOOLS_IN_CITY.equals(page)) {
            if (levelCode != null && StringUtils.isNotEmpty(levelCode.getCommaSeparatedString())) {
                String[] lcs = StringUtils.split(levelCode.getCommaSeparatedString(), ",");
                for (int i = 0; i < lcs.length; i++) {
                    addParameter(SchoolsController.PARAM_LEVEL_CODE, lcs[i]);
                }
            }
            if (schoolType != null) {
                if (StringUtils.isNotEmpty(schoolType)) {
                    String[] sts = StringUtils.split(schoolType, ",");
                    for (int i = 0; i < sts.length; i++) {
                        addParameter(SchoolsController.PARAM_SCHOOL_TYPE, sts[i]);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("VPage unknown " + page);
        }
    }

    private void init(VPage page, State state, String param0) {
        _vPage = page;

        if (CITY_PAGE.equals(page)) {
            _perlPage = false;
            _path = "/city/";
            setParameter("city", param0);
            setParameter("state", state.getAbbreviation());
        } else if (CITIES.equals(page)) {
            _perlPage = true;
            _path = "/modperl/cities/" +
                    state.getAbbreviation() +
                    "/";
        } else if (MY_SCHOOL_LIST.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/msl_confirm/" +
                    state.getAbbreviation() +
                    "/";
        } else if (ARTICLE_LIBRARY.equals(page)) {
            _perlPage = false;
            _path = "/content/allArticles.page";
            setParameter("state", state.getAbbreviation());
        } else if (NEWSLETTER_CENTER.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/newsletters/" +
                    state.getAbbreviation() +
                    "/";
        } else if (SCHOOLS_IN_CITY.equals(page)) {
            _perlPage = false;
            _path = "/schools.page";
            setParameter("city", param0);
            setParameter("state", state.getAbbreviation());
        } else if (SCHOOLS_IN_DISTRICT.equals(page)) {
            _perlPage = false;
            _path = "/schools.page";
            setParameter("district", param0);
            setParameter("state", state.getAbbreviation());
        } else if (RESEARCH.equals(page)) {
            /*if (state == null) {
                _perlPage = false;
                _path = "/test/research.page";
            } else {
                _perlPage = true; // well, it looks like one
                _path = "/modperl/go/" + state.getAbbreviation();
            } */
            _perlPage = false;
            _path = "/path/choose.page";
        } else if (PRIVACY_POLICY.equals(page)) {
            _perlPage = false;
            _path = "/about/privacyStatement.page";
            setParameter("state", state.getAbbreviation());
        } else if (SIGN_IN.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/site/signin.cgi/" + state.getAbbreviation();
        } else if (SIGN_OUT.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/logout/" + state.getAbbreviation();
            if (param0 != null) {
                if (StringUtils.isNumeric(param0)) {
                    setParameter("mid", param0);
                } else  {
                    setParameter("email", param0);
                }
            }
        } else if (NEWSLETTER_MANAGEMENT.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/newsletterSubscribe";
            setParameter("state", state.getAbbreviation());
        } else if (ADMIN_NEWS_ITEMS.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/list.page";
        } else if (ADMIN_NEWS_ITEMS_CREATE.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/create.page";
        } else if (ADMIN_NEWS_ITEMS_DELETE.equals(page)) {
            _perlPage = false;
            _path = "/admin/news/delete.page";
            setParameter("id", param0);
        } else if (BETA_SIGNUP.equals(page)) {
            _perlPage = false;
            _path = "/community/beta.page";
            if (param0 != null) {
                setParameter("email", param0);
            }
            setParameter("state", state.getAbbreviation());
        } else if (BETA_UNSUBSCRIBE.equals(page)) {
            _perlPage = false;
            _path = "/community/betaUnsubscribe.page";
            if (param0 != null) {
                setParameter("email", param0);
            }
            setParameter("state", state.getAbbreviation());
        } else if (CONTACT_US.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/feedback/" + state.getAbbreviation();
        } else if (ADD_PARENT_REVIEW_SEARCH.equals(page)) {
            _perlPage = true;
            _path = "/cgi-bin/regSearch/" + state.getAbbreviation();
        } else {
            throw new IllegalArgumentException("VPage unknown" + page);
        }
    }

    /**
     * Set the path to the page.
     *
     * @param path context-relative path
     */
    public void setPath(String path) {
        _path = path;
        _perlPage = _urlUtil.smellsLikePerl(path);
    }

    /**
     * Takes all the parameters in the given requests and adds them to the URL.
     * If some parameters already exist, they will be appended to.
     */
    public void addParametersFromRequest(HttpServletRequest request) {
        Map parameterMap = request.getParameterMap();
        for (Iterator i = parameterMap.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            String[] value = request.getParameterValues(key);
            for (int j = 0; j < value.length; j++) {
                this.addParameter(key, value[j]);
            }
        }
    }

    /**
     * Replaces the given parameter.
     *
     * @param value previously encoded value. Spaces should be represented by "+" signs,
     *              and "=" and "&" should be encoded, along with other extended characters.
     */
    public void setParameterNoEncoding(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
        }
        _parameters.put(key, new String[]{value});
    }

    /**
     * Adds the given parameter. If one exists already, this one is added as well.
     *
     * @param value previously encoded value. Spaces should be represented by "+" signs,
     *              and "=" and "&" should be encoded, along with other extended characters.
     * @see #setParameterNoEncoding(String, String)
     * @see #addParameter(String, String)
     */
    public void addParameterNoEncoding(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
            _parameters.put(key, new String[]{value});
        } else {
            String[] existingValues = (String[]) _parameters.get(key);
            if (existingValues == null) {
                _parameters.put(key, new String[]{value});
            } else {
                String [] newValues = org.springframework.util.StringUtils.addStringToArray(existingValues, value);
                _parameters.put(key, newValues);
            }

        }
    }

    /**
     * Replaces the given parameter.
     *
     * @param value unencoded values. Spaces, ampersands, equal signs, etc. will be replaced.
     */
    public void setParameter(String key, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to encode parameter");
        }
        setParameterNoEncoding(key, value);
    }

    /**
     * Adds the given parameter. If it exists already, then this one is also added.
     *
     * @param value unencoded values. Spaces, ampersands, equal signs, etc. will be replaced.
     * @see #addParameterNoEncoding
     * @see #setParameter(String, String)
     */
    public void addParameter(String key, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to encode parameter");
        }
        addParameterNoEncoding(key, value);
    }

    /**
     * Take away the parameter.
     */
    public void removeParameter(String key) {
        if (_parameters != null) {
            _parameters.remove(key);
        }
    }

    public String toString() {
        return asSiteRelativeXml(null);
    }

    /**
     * Provides a site-relative path to the page, including the context path if needed.
     * Encoded correctly to dump directly to XHTML.
     *
     * @param request option request object.
     */
    public String asSiteRelativeXml(HttpServletRequest request) {
        String s = buildSiteRelative(request);
        s = encodeForXml(s);
        return s;
    }

    /**
     * Provides a site-relative path to the page, including the context path if needed.
     * Not encoded for XHTML.
     *
     * @param request option request object.
     */
    public String asSiteRelative(HttpServletRequest request) {
        String s = buildSiteRelative(request);
        return s;
    }

    private String buildSiteRelative(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();
        String contextPath = request != null ? request.getContextPath() : "";
        if (!_perlPage) {
            sb.append(contextPath);
        }
        sb.append(_path);

        // City page's parameters get stuck in the first part of the
        // URL for SEO purposes.
        if (CITY_PAGE.equals(_vPage)) {
            String[] values = (String[]) _parameters.get("city");
            final String cityName = values[0];
            // undo encoding...
            sb.append(cityName.replaceAll("%27", "'").replaceAll("\\+", "_"));
            // ...minimal encoding. See http://www.rfc-editor.org/rfc/rfc1738.txt
            sb.append("/");
            values = (String[]) _parameters.get("state");
            sb.append(values[0]);
        } else {
            if (_parameters != null && _parameters.size() > 0) {
                sb.append("?");
                List keys = new ArrayList(_parameters.keySet());
                Collections.sort(keys);
                for (Iterator iter = keys.iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    String[] values = (String[]) _parameters.get(key);
                    for (int i = 0; i < values.length; i++) {
                        sb.append(key);
                        sb.append("=" + values[i]);
                        if (i < (values.length - 1) || iter.hasNext()) {
                            sb.append("&");
                        }
                    }
                }

            }
        }

        String s = sb.toString();
        return s;
    }

    /**
     * Simple encoding of a string to put into an Xml document. Note that it doesn't deal
     * with real encoding-- only the specific XML characters, & < and >.
     */
    public static String encodeForXml(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    public Anchor asAnchor(HttpServletRequest request, String label) {
        return new Anchor(asSiteRelative(request), label);
    }

    public Anchor asAnchor(HttpServletRequest request, String label, String styleClass) {
        return new Anchor(asSiteRelative(request), label, styleClass);
    }

    public Anchor asAnchor(HttpServletRequest request, String label, String styleClass, String image) {
        return new Anchor(asSiteRelative(request), label, styleClass, image);
    }

    /**
     * Provides a full URL to the page. This is the raw URL, not encoded correctly
     * for XHTML. This is generally not needed, but is needed for redirect usage.
     *
     * @see #asSiteRelativeXml(javax.servlet.http.HttpServletRequest)
     */
    public String asFullUrl(HttpServletRequest request) {

        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String url = "http://" +
                serverName +
                ((serverPort != 80) ? ":" + serverPort : "") +
                buildSiteRelative(request);
        return url;
    }


    /**
     * Provides a site-relative link wrapped in an a tag.
     * Encoded correctly to dump directly to XHTML.
     */
    public String asAHref(HttpServletRequest request, String label) {
        return "<a href=\"" + asSiteRelativeXml(request) + "\">" + label + "</a>";
    }

    /**
     * Provides a site-relative link wrapped in an a tag.
     * Encoded correctly to dump directly to XHTML.
     *
     * @param label      the contents of the a tag
     * @param styleClass the css class attribute
     */
    public String asAHref(HttpServletRequest request, String label, String styleClass) {
        return "<a href=\"" + asSiteRelativeXml(request) + "\" class=\"" + styleClass + "\">" + label + "</a>";
    }

}
