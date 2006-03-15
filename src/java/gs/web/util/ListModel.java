/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ListModel.java,v 1.1 2006/03/15 02:24:21 apeterson Exp $
 */

package gs.web.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides the model of what the unorderedList.jspx page expects.
 *
 * The model can either be an instance of this class itself, or it
 * can be a map using the static values identifying the properties.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ListModel {
    public static final String HEADING = "heading";

    /**
     * Collection of Anchor objects.
     */
    public static final String RESULTS = "results";


    private String _heading;

    /**
     * Collection of Anchor objects.
     */
    private final List _results;

    public ListModel() {
        _results = new ArrayList();
    }

    public ListModel(String heading) {
        _heading = heading;
        _results = new ArrayList();
    }

    public void addResult(Anchor anchor) {
        _results.add(anchor);
    }

    public String getHeading() {
        return _heading;
    }

    public void setHeading(String heading) {
        _heading = heading;
    }

    public List getResults() {
        return _results;
    }

}
