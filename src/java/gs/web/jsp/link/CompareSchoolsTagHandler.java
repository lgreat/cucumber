package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareSchoolsTagHandler extends LinkTagHandler {
    private String _schools;
    private String _tab;
    private Integer _page;
    private String _remove;

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (StringUtils.equals("overview", _tab)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_OVERVIEW);
        } else if (StringUtils.equals("ratings", _tab)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_RATINGS);
        } else if (StringUtils.equals("map", _tab)) {
            builder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_MAP);
        } else {
            throw new IllegalArgumentException("Tab not recognized for compare: " + _tab);
        }

        if (StringUtils.isNotEmpty(_remove)) {
            String[] schoolsArr = (String[]) ArrayUtils.removeElement(_schools.split(","), _remove);
            _schools = StringUtils.join(schoolsArr, ",");
        }
        builder.setParameter("schools", _schools);

        if (_page != null && _page > 1) {
            builder.setParameter("p", String.valueOf(_page));
        }

        return builder;
    }

    public String getSchools() {
        return _schools;
    }

    public void setSchools(String schools) {
        _schools = schools;
    }

    public String getTab() {
        return _tab;
    }

    public void setTab(String tab) {
        _tab = tab;
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }

    public String getRemove() {
        return _remove;
    }

    public void setRemove(String remove) {
        _remove = remove;
    }
}
