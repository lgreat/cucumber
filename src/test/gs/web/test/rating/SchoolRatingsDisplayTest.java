/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolRatingsDisplayTest.java,v 1.1 2006/09/26 23:22:18 apeterson Exp $
 */

package gs.web.test.rating;

import gs.data.school.School;
import gs.data.test.rating.IRatingsConfig;
import junit.framework.TestCase;
import org.easymock.MockControl;

import java.util.List;

/**
 * Tests SchoolRatingsDisplay.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolRatingsDisplayTest extends TestCase {
    private MockControl _ratingsConfigControl;
    private IRatingsConfig _ratingsConfig;

    private School _school;

    private SchoolRatingsDisplay _schoolRatingsDisplay;

    protected void setUp() throws Exception {
        super.setUp();

        _ratingsConfigControl = MockControl.createControl(IRatingsConfig.class);
        _ratingsConfig = (IRatingsConfig) _ratingsConfigControl.getMock();

        _school = new School();

        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getSubjectGroupConfigs(), new IRatingsConfig.ISubjectGroupConfig[] {});
        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getRowGroupConfigs(), new IRatingsConfig.IRowGroupConfig[] {});
        _ratingsConfigControl.replay();
        _schoolRatingsDisplay = new SchoolRatingsDisplay(_ratingsConfig, _school);
        _ratingsConfigControl.reset();
    }

    public void testEmpty() {
        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getSubjectGroupConfigs(), new IRatingsConfig.ISubjectGroupConfig[] {});
        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getSubjectGroupConfigs(), new IRatingsConfig.ISubjectGroupConfig[] {});
        _ratingsConfigControl.replay();

        List list = _schoolRatingsDisplay.getSubjectGroupLabels();
        assertNotNull(list);
        assertEquals(0, list.size());

        list = _schoolRatingsDisplay.getRowGroups();
        assertNotNull(list);
        //assertEquals(0, list.size());
    }
}
