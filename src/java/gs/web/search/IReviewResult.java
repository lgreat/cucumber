package gs.web.search;

import gs.data.search.ISearchResult;
import gs.data.state.State;

import java.util.Date;

public interface IReviewResult extends ISearchResult {

    Integer getId();

    Integer getSchoolId();

    State getSchoolDatabaseState();

    String getSchoolName();

    String getSchoolAddress();

    String getUrl();

    Date getPostedDate();

    Integer getRating();

    String getSubmitterName();

    String getComments();
}
