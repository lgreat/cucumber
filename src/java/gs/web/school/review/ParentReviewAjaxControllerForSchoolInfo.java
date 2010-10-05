package gs.web.school.review;

import gs.data.school.review.Ratings;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.web.util.UrlBuilder;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * Parent review page
 *
 * @author <a href="mailto:npatury@greatschools.org">Nanditha Patury</a>
 */

public class ParentReviewAjaxControllerForSchoolInfo implements Controller {

    ISchoolDao _schoolDao;
    IReviewDao _reviewDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        StringBuffer str = new StringBuffer();
        if (request.getParameter("schoolId") != null && request.getParameter("state") != null) {
            String state = request.getParameter("state");
            School school = _schoolDao.getSchoolById(State.fromString(state), Integer.parseInt(request.getParameter("schoolId")));
            if (school != null) {

                str.append(school.getName() + ";");
                List<School> schoolList = new ArrayList<School>();
                schoolList.add(school);
                List<SchoolWithRatings> schoolWithRatingsList = _schoolDao.populateSchoolsWithRatings(State.fromString(state), schoolList);

                if (schoolWithRatingsList != null && schoolWithRatingsList.size() > 0) {
                    SchoolWithRatings swr = schoolWithRatingsList.get(0);
                    str.append((swr.getRating() == null ? "noRatingInfo" : swr.getRating()) + ";");
                } else {
                    str.append("noRatingInfo" + ";");
                }
                Ratings ratings = _reviewDao.findRatingsBySchool(school);
                str.append((ratings.getOverall() == null ? "" : ratings.getOverall()) + ";");
                str.append((ratings.getCount() == null ? "" : ratings.getCount()) + ";");
                str.append(school.getStreet() + ";");
                str.append((school.getStreetLine2() == "" ? "" : school.getStreetLine2()) + ";");
                str.append(school.getCity() + ";");
                str.append(school.getStateAbbreviation() + ";");
                str.append(school.getZipcode() + ";");
                str.append(school.getCounty() + ";");
                str.append(school.getId() + ";");
                UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP_LOGIN);
                str.append(builder.asFullUrl(request) + ";");
                if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                    str.append("isPreschool" + ";");
                }
                if (SchoolType.PUBLIC.equals(school.getType())) {
                    str.append("isPublic" + ";");
                }
                str.append(school.getLevelCode()).append(";");

                boolean showStudent = true;
                Grades below9 = Grades.createGrades(Grade.PRESCHOOL, Grade.G_8);
                if (school.getGradeLevels().containsAny(below9)) {
                   showStudent = false;
                }
                if (showStudent) {
                    str.append("showStudent").append(";");
                }
            }
        }
        PrintWriter out = response.getWriter();
        out.print(str);
        return null;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
