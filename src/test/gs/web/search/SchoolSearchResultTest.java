package gs.web.search;

import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.search.Indexer;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SchoolSearchResultTest extends TestCase {
    public void testGreatSchoolsRatingComesFromIndex() {
        Document doc = new Document();
        doc.add(new Field(Indexer.OVERALL_RATING, "4", Field.Store.YES, Field.Index.TOKENIZED));
        SchoolSearchResult result = new SchoolSearchResult(doc);

        assertEquals("Unexpected greatschools rating", "4", result.getGreatSchoolsRating());
    }

    public void testParentRatingShouldBeIncludedOnlyIfThreeOrMore() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        assertNull("Expected parent rating to be null when no reviews exist", result.getParentRating());

        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, "4", Field.Store.YES, Field.Index.TOKENIZED));
        assertNull("Expected parent rating to be null when no reviews exist, even if quality is set", result.getParentRating());

        doc.add(new Field(Indexer.PARENT_RATINGS_COUNT, "2", Field.Store.YES, Field.Index.TOKENIZED));
        assertNull("Expected no parent rating when less than 3 exist", result.getParentRating());
    }

    public void testParentRatingCount() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        assertEquals(0, result.getParentRatingCount());

        doc.add(new Field(Indexer.PARENT_RATINGS_COUNT, "2", Field.Store.YES, Field.Index.TOKENIZED));
        assertEquals(2, result.getParentRatingCount());
    }

    public void testReviewMap() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        assertNull( result.getReviewMap().get("reviewCount"));

        doc.add(new Field("reviewCount", "2", Field.Store.YES, Field.Index.UN_TOKENIZED));
        assertEquals("2", result.getReviewMap().get("reviewCount"));
        doc.add(new Field("reviewBlurb", "groupof10 groupof10 groupof10 groupof10 groupof10", Field.Store.YES, Field.Index.UN_TOKENIZED));
        assertEquals("groupof10 groupof10 groupof10 groupof10 groupof10", result.getReviewMap().get("reviewBlurb"));
    }

    public void testParentRatingComesFromIndex() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        result.setSchool(school);
        doc.add(new Field(Indexer.PARENT_RATINGS_COUNT, "3", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, "4", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_P_OVERALL, "5", Field.Store.YES, Field.Index.TOKENIZED));

        assertEquals("Unexpected parent rating value", "4", result.getParentRating());
    }

    public void testPreschoolElementaryParentRatingComesFromIndex() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        School school = new School();
        school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        result.setSchool(school);
        doc.add(new Field(Indexer.PARENT_RATINGS_COUNT, "3", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, "4", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_P_OVERALL, "5", Field.Store.YES, Field.Index.TOKENIZED));

        assertEquals("Unexpected parent rating value", "4", result.getParentRating());
    }

    public void testPreschoolParentRatingComesFromIndex() {
        Document doc = new Document();
        SchoolSearchResult result = new SchoolSearchResult(doc);
        School school = new School();
        school.setLevelCode(LevelCode.PRESCHOOL);
        result.setSchool(school);
        doc.add(new Field(Indexer.PARENT_RATINGS_COUNT, "3", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, "4", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(Indexer.PARENT_RATINGS_AVG_P_OVERALL, "5", Field.Store.YES, Field.Index.TOKENIZED));

        assertEquals("Unexpected parent rating value", "5", result.getParentRating());
    }

    public void testSchoolShouldBeNullByDefault() {
        SchoolSearchResult result = new SchoolSearchResult(new Document());

        assertNull("School should be null by default", result.getSchool());

        School expectedSchool = new School();
        result.setSchool(expectedSchool);

        assertEquals("Unexpected school", expectedSchool, result.getSchool());
    }
}
