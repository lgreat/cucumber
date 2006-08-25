/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Column.java,v 1.1 2006/08/25 23:08:26 dlee Exp $
 */
package gs.web.test;

import gs.data.test.TestDataSet;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class Column {
    private TestDataSet [] _testDataSets;
    private String _label;

    public Column(TestDataSet[] testDataSets, String label) {
        _testDataSets = testDataSets;
        _label = label;
    }

    public TestDataSet[] getTestDataSets() {
        return _testDataSets;
    }

    public String getLabel() {
        return _label;
    }
}
