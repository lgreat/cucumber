/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModel.java,v 1.2 2006/06/03 03:03:38 apeterson Exp $
 */

package gs.web.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides the model of what the gsml:list.tagx expects.
 *
 * The model can either be an instance of this class itself, or it
 * can be a map using the static values identifying the properties.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class AnchorListModel {

    public static final String HEADING = "heading";

    /**
     * Collection of Anchor objects.
     */
    public static final String RESULTS = "results";
    public static final String COLUMNS = "columns";

    public static final String DEFAULT = "anchorListModel";

    private String _heading;
    private int _columns;

    /**
     * Collection of Anchor objects.
     */
    private final List _results;

    public AnchorListModel() {
        _results = new ArrayList();
    }

    public AnchorListModel(String heading) {
        _heading = heading;
        _results = new ArrayList();
    }

    public void add(Anchor anchor) {
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

    public int getColumns() {
        return _columns;
    }

    public void setColumns(int columns) {
        _columns = columns;
    }
}
