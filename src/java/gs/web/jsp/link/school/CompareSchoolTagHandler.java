/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CompareSchoolTagHandler.java,v 1.3 2007/06/04 21:29:55 chriskimm Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.School;
import gs.data.util.Address;
import gs.web.util.UrlBuilder;

/**
 * Compare spare link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CompareSchoolTagHandler extends BaseSchoolTagHandler {
    private String _sortBy = "distance";
    private String _from;

    protected UrlBuilder createUrlBuilder() {
        School school = getSchool();
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.COMPARE_SCHOOL);

        //Parameteres
        //?street=1101+Eucalyptus+Dr.&amp;school_selected=6397&amp;city=San+Francisco&amp;
        //zip=94132&amp;area=m&amp;miles=1000&amp;level=h&amp;sortby=distance&amp;tab=over&amp;showall=1">

        //defaults - create getter and setter if / when it makes sense to do so
        builder.setParameter("school_selected", school.getId().toString());
        builder.setParameter("area", "m");
        builder.setParameter("miles", "1000");
        builder.setParameter("showall", "1");
        builder.setParameter("tab", "over");

        if (_from != null) {
            builder.setParameter("from", _from);            
        }
        
        try {
            Address address = school.getPhysicalAddress();
            if (null != address) {
                builder.setParameter("street", address.getStreet());
                builder.setParameter("city", address.getCity());
                builder.setParameter("zip", address.getZip());
            }
            builder.setParameter("level", String.valueOf(school.getLevelCode().getCommaSeparatedString().charAt(0)));
        } catch (NullPointerException e) {} //do nothing

        builder.setParameter("sortby", getSortBy());                
        return builder;
    }

    public String getSortBy() {
        return _sortBy;
    }

    public void setSortBy(String sortBy) {
        _sortBy = sortBy;
    }

    public String getFrom() {
        return _from;
    }

    public void setFrom(String from) {
        _from = from;
    }
}
