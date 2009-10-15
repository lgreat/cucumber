package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.review.Review;
import gs.data.school.review.CategoryRating;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.StringUtils;

public class ReviewFacade implements IParentReviewModel {
    private final School _school;
    private final Review _review;

    public ReviewFacade(School school, Review review) {
        _school = school;
        _review = review;
    }

    public String getSchoolName() {
        return _school.getName();
    }

    public int getStars() {
        CategoryRating quality;
        if (LevelCode.PRESCHOOL.equals(_school.getLevelCode())) {
            quality = _review.getPOverall();
        } else {
            quality = _review.getQuality();
        }

        if (CategoryRating.RATING_1.equals(quality)) {
            return 1;
        } else if (CategoryRating.RATING_2.equals(quality)) {
            return 2;
        } else if (CategoryRating.RATING_3.equals(quality)) {
            return 3;
        } else if (CategoryRating.RATING_4.equals(quality)) {
            return 4;
        } else if (CategoryRating.RATING_5.equals(quality)) {
            return 5;
        }
        return 0;
    }

    public String getDate() {
        Date today = new Date();
        long diff = Math.abs(today.getTime() - _review.getPosted().getTime());
        if (diff < DateUtils.MILLIS_PER_DAY) {
            return "today";
        }
        if (diff < (DateUtils.MILLIS_PER_DAY + DateUtils.MILLIS_PER_DAY)) {
            return "yesterday";
        } else {
            return "" + Math.round(diff / DateUtils.MILLIS_PER_DAY) + " days ago";
        }
    }

    public String getQuip() {
        String c = _review.getComments();
        return StringUtils.abbreviate(c, 90);
    }

    public School getSchool() {
        return _school;
    }

    public Review getReview() {
        return _review;
    }
}
