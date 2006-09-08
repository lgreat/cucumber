/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsController.java,v 1.4 2006/09/08 17:59:35 apeterson Exp $
 */
package gs.web.test;

import gs.data.test.rating.IRatingsDisplay;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draw the rating page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingsController extends AbstractController {

    private static final Log _log = LogFactory.getLog(RatingsController.class);

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        model.put("columns", getColumns());
        model.put("rowGroups", getRowGroups());

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    private List getColumns() {
        List columns = new ArrayList();

        columns.add("English");
        columns.add("Math");

        return columns;
    }

    private List getRowGroups() {
        List rowGroups = new ArrayList();

        RowGroup rowGroup1 = new RowGroup("By Gender");
        rowGroup1.addRow(new Row("Male"));
        rowGroup1.addRow(new Row("Female"));

        RowGroup rowGroup2 = new RowGroup("By Category");
        rowGroup2.addRow(new Row("Rich"));
        rowGroup2.addRow(new Row("Poor"));

        RowGroup rowGroup3 = new RowGroup("By Ethnicity");
        rowGroup3.addRow(new Row("Black"));
        rowGroup3.addRow(new Row("White"));

        rowGroups.add(rowGroup1);
        rowGroups.add(rowGroup2);
        rowGroups.add(rowGroup3);

        return rowGroups;
    }

    private static class RowGroup implements IRatingsDisplay.IRowGroup {
        List _rows = new ArrayList();
        String _label;

        RowGroup(String label) {
            _label = label;
        }

        public String getLabel() {
            return _label;
        }

        public int getNumRows() {
            return _rows.size();
        }

        public IRow getRow(int i) {
            return (IRow) _rows.get(i);
        }

        public void addRow(IRow row) {
            _rows.add(row);
        }
    }

    private static class Row implements IRatingsDisplay.IRowGroup.IRow {
        String _label;

        Row(String label) {
            _label = label;
        }

        public String getLabel() {
            return _label;
        }

        public Integer getRating(int column) {
            return new Integer(1);
        }

        public Integer getTrend(int subjectGroupIndex) {
            return null;
        }
    }
}
