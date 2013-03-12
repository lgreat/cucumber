package gs.web.school;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchoolProfileStatsDisplayRowTest {

    @Test
    public void testCensusValueNotEmpty() {
        assertFalse(SchoolProfileStatsDisplayRow.censusValueNotEmpty(null));
        assertFalse(SchoolProfileStatsDisplayRow.censusValueNotEmpty(""));
        assertFalse(SchoolProfileStatsDisplayRow.censusValueNotEmpty("n/a"));
        assertFalse(SchoolProfileStatsDisplayRow.censusValueNotEmpty("N/A"));
        assertTrue(SchoolProfileStatsDisplayRow.censusValueNotEmpty("    "));
        assertTrue(SchoolProfileStatsDisplayRow.censusValueNotEmpty("0"));
        assertTrue(SchoolProfileStatsDisplayRow.censusValueNotEmpty("five"));
        assertTrue(SchoolProfileStatsDisplayRow.censusValueNotEmpty("no"));
        assertTrue(SchoolProfileStatsDisplayRow.censusValueNotEmpty("empty"));
    }

}
