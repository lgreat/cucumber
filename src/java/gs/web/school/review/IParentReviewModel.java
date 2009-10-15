package gs.web.school.review;

import gs.data.school.School;

public interface IParentReviewModel {
    String getSchoolName();

    int getStars();

    String getDate();

    String getQuip();

    School getSchool();
}
