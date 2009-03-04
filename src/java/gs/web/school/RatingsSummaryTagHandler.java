package gs.web.school;

import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;

import gs.data.school.*;
import gs.data.school.review.Ratings;
import gs.data.school.district.District;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Feb 23, 2009
 * Time: 5:50:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RatingsSummaryTagHandler extends BaseTagHandler {

    public class CharterSchoolMessageBuilder {
        public CharterSchoolMessageBuilder(){

        }

        public String buildFirstSentence() {
            StringBuilder result = new StringBuilder();
            // beginning of sentence
            if ( _school.getGradeLevels() == null ) {
                result.append(_school.getName());
                result.append(" is a charter school in ");
                result.append(_school.getCity());
                result.append(", ");
                result.append(_school.getStateAbbreviation().getLongName());
                result.append(".");

                return result.toString();
            }

            // school name choice
            if (_school.getName().toUpperCase().charAt(0) < 'M') {
                result.append(_school.getName());
                result.append(", located in ");
                result.append(_school.getCity());
                result.append(", ");
                result.append(_school.getStateAbbreviation().getLongName());
                result.append(",");
            } else {
                result.append(getPossessive(_school.getCity()));
                result.append(" ");
                result.append(_school.getName());
            }


            if (getNumberOfSchoolsForGradeLevel() == 1 )
            {
                result.append(" is a charter ");
                if(getNumberOfLevels() == 1) {
                    result.append(getGradeLevel());
                    result.append(" school");
                }else{
                    result.append(" is a charter that serves grades ");
                    result.append(getGradesServed());
                }
            }

            if (getDistrict() == null){
                result.append(".");
            }else{
                result.append(" in the ");
                result.append(getDistrict().getName());
                result.append(".");
            }
            return result.toString();
        }

        public String buildSecondSentence() {
            StringBuilder result = new StringBuilder();
            //not all charter schools have gsratings!  for those who don't, lets skip this sentence!
            if (_gsRating < 1){
                return "";
            }

            if (getNumberOfLevels() == 1) {
                if (_gsRating >= 8) {
                    result.append("It is among the few ");
                    result.append(getGradeLevel());
                    result.append(" schools in ");
                    result.append(_school.getStateAbbreviation().getLongName());
                    result.append(" to receive a distinguished GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                } else if (_gsRating >= 5) {
                    result.append("It has received a GreatSchools rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10 based on its performance on state standardized tests.");
                } else {
                    result.append("Based on its state test results, it has received a GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                }
            }else{
                if (_gsRating >= 8) {
                    result.append("It is among the few schools in ");
                    result.append(_school.getStateAbbreviation().getLongName());
                    result.append(" to receive a distinguished GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                } else if (_gsRating >= 5) {
                    result.append("It has received a GreatSchools rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10 based on its performance on state standardized tests.");
                } else {
                    result.append("Based on its state test results, it has received a GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                }
            }
            return result.toString();
        }


        public String buildThirdSentence() {
            StringBuilder result = new StringBuilder();

            if (getNumberOfParentReviews() >= 50) {
                result.append("More than ");
                result.append(getNumberOfParentReviews() - getNumberOfParentReviews() % 10);  // round down to the nearest factor of 10
                result.append(" parents have shared their opinion about ");
                result.append(_school.getName());
                result.append(" giving it an average Parent Rating of ");
                result.append(getAverageParentRating());
                result.append(" out of 5 stars.");
            } else if (getNumberOfParentReviews() >= 10) {
                result.append(getNumberOfParentReviews());
                result.append(" parents have submitted a review for this school, and it has an average Parent Rating of ");
                result.append(getAverageParentRating());
                result.append(" out of 5 stars.");
            } else if (getNumberOfParentReviews() >= 1) {
                result.append("Parents have reviewed this school and given it a an average rating of ");
                result.append(getAverageParentRating());
                result.append(" out of 5 stars.");
            } else {
                result.append("Ratings and reviews from parents help tell the story about ");
                result.append(_school.getName());
                result.append(".  ");
                UrlBuilder b = new UrlBuilder(_school, UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
                PageContext pageContext = (PageContext)getJspContext();
                HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
                result.append(b.asAHref (request, "Be the first to share your review!", "addParentReviewLink submodal-344-362 GS_CI9_"));
            }

            return result.toString();
        }
    }
/*
    public class PublicSchoolMessageBuilder {


         private School _school;


        private int _numberOfSchools = 0;

        private int _gsRating = 0;
        private int _numberOfParentReviews = 0;
        private int _averageParentRating = 0;


        private District _district;

        public PublicSchoolMessageBuilder(School school, District district, int gsRating, int numberOfParentReviews, int averageParentRating) {
            _school = school;
            _gsRating = gsRating;
            _numberOfParentReviews = numberOfParentReviews;
            _averageParentRating = averageParentRating;
            _district = district;
        }

        public String buildFirstSentence() {

            StringBuilder result = new StringBuilder();
            // beginning of sentence

            if (_school.getType() == SchoolType.CHARTER && _school.getGradeLevels() == null) {
                result.append(_school.getName());
                result.append(" is a charter school in ");
                result.append(_school.getCity());
                result.append(", ");
                result.append(_school.getStateAbbreviation().getLongName());
                result.append(".");

                return result.toString();
            }

            if (_school.getName().toUpperCase().charAt(0) < 'M') {
                result.append(_school.getName());
                result.append(", located in ");
                result.append(_school.getCity());
                result.append(", ");
                result.append(_school.getStateAbbreviation().getLongName());
                result.append(",");
            } else {
                result.append(getPossessive(_school.getCity()));
                result.append(" ");
                result.append(_school.getName());
            }


            if (getNumberOfLevels() > 1) {
                result.append(" is a");
                result.append(_school.getType() == SchoolType.CHARTER ? " charter " : " public ");
                result.append("school that serves grades ");
                result.append(getGradesServed());
                result.append(" in the ");
            } else if (_numberOfSchools > 1) {
                result.append(" is one of ");
                result.append(_numberOfSchools);
                result.append(" ");
                result.append(getGradeLevel());
                result.append(" schools in the ");
            } else {
                result.append(" is a");
                result.append(_school.getType() == SchoolType.CHARTER ? " charter " : " public ");
                result.append(getGradeLevel());
            }

            if (_district == null) {
                result.append(".");
            } else {
                result.append(" school in the ");
                result.append(_district.getName());
                result.append(".");
            }


            return result.toString();
        }

        public String buildSecondSentence() {
            StringBuilder result = new StringBuilder();
            //not all charter schools have gsratings!  for those who don't, lets skip this sentence!
            if (_gsRating == 0){
                return "";
            }

            if (getNumberOfLevels() == 1) {
                if (_gsRating >= 8) {
                    result.append("It is among the few ");
                    result.append(getGradeLevel());
                    result.append(" schools in ");
                    result.append(_school.getStateAbbreviation().getLongName());
                    result.append(" to receive a distinguished GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                } else if (_gsRating >= 5) {
                    result.append("It has received a GreatSchools rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10 based on its performance on state standardized tests.");
                } else {
                    result.append("Based on its state test results, it has received a GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                }
            }else{
                if (_gsRating >= 8) {
                    result.append("It is among the few schools in ");
                    result.append(_school.getStateAbbreviation().getLongName());
                    result.append(" to receive a distinguished GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                } else if (_gsRating >= 5) {
                    result.append("It has received a GreatSchools rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10 based on its performance on state standardized tests.");
                } else {
                    result.append("Based on its state test results, it has received a GreatSchools Rating of ");
                    result.append(_gsRating);
                    result.append(" out of 10.");
                }                
            }
            return result.toString();
        }

        public String buildThirdSentence() {
            StringBuilder result = new StringBuilder();

            if (_numberOfParentReviews >= 50) {
                result.append("More than ");
                result.append(_numberOfParentReviews - _numberOfParentReviews % 10);  // round down to the nearest factor of 10
                result.append(" parents have shared their opinion about ");
                result.append(_school.getName());
                result.append(" giving it an average Parent Rating of ");
                result.append(_averageParentRating);
                result.append(" out of 5 stars.");
            } else if (_numberOfParentReviews >= 10) {
                result.append(_numberOfParentReviews);
                result.append(" parents have submitted a review for this school, and it has an average Parent Rating of ");
                result.append(_averageParentRating);
                result.append(" out of 5 stars.");
            } else if (_numberOfParentReviews >= 1) {
                result.append("Parents have reviewed this school and given it a an average rating of ");
                result.append(_averageParentRating);
                result.append(" out of 5 stars.");
            } else {
                result.append("Ratings and reviews from parents help tell the story about ");
                result.append(_school.getName());
                result.append(".  ");
                UrlBuilder b = new UrlBuilder(_school, UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
                PageContext pageContext = (PageContext)getJspContext();
                HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
                result.append(b.asAHref(request, "Be the first to share your review!"));
            }

            return result.toString();
        }





        public String getGradeLevel() {
            StringBuilder result = new StringBuilder();
            String[] levels = _school.getLevelCode().toString().split(",");

            int counter = 0;
            for (String level : levels) {
                switch (level.charAt(0)) {
                    case 'p':
                        result.append("preschool");
                        break;
                    case 'e':
                        result.append("elementary");
                        break;
                    case 'm':
                        result.append("middle");
                        break;
                    case 'h':
                        result.append("high");
                        break;
                }
                if (levels.length > 2) {
                    if (counter == levels.length - 2) {
                        result.append(" and ");
                    } else if (counter < levels.length - 1) {
                        result.append(", ");
                    }
                } else if (levels.length > 1) {
                    if (counter == levels.length - 2) {
                        result.append(" and ");
                    }
                }
                counter++;
            }
            return result.toString();
        }
    }

*/
    private School _school;
    private Ratings _parentRating;
    private int _gsRating = 0;


    public void doTag() throws JspException, IOException {

        int numberOfParentReviews = 0;


        //need logic for number of schools: city / district for a level and type?
        //if district is null, use city
        //need to get number of parent reviews for the school

        int parentOverallRating = 0;

        //MessageBuilder builder = new MessageBuilder(_school, _school.getDistrict(), _gsRating, numberOfParentReviews, parentOverallRating);
        CharterSchoolMessageBuilder builder = new CharterSchoolMessageBuilder();
        writeOpeningDiv();
        String firstParagraph =   builder.buildFirstSentence();
        if (_gsRating >=1 ){
            firstParagraph += "  " + builder.buildSecondSentence();
        }
        writeParagraph(builder.buildFirstSentence() + "  " + builder.buildSecondSentence());
        writeParagraph(builder.buildThirdSentence());
        writeClosingDiv();

    }

    public void writeOpeningDiv() throws JspException, IOException {
        final String OPEN_DIV = "<div id=\"rating_summary\">";
        getJspContext().getOut().write(OPEN_DIV);
    }

    public void writeClosingDiv() throws JspException, IOException {
        final String CLOSING_DIV = "</div>";
        getJspContext().getOut().write(CLOSING_DIV);
    }

    public void writeParagraph(String s) throws JspException, IOException {

        final String OPEN_PARAGRAPH = "<p>";
        final String CLOSE_PARAGRAPH = "</p>";

        getJspContext().getOut().write(OPEN_PARAGRAPH + s + CLOSE_PARAGRAPH);
    }

    public int getNumberOfParentReviews(){
        if (_parentRating != null && _parentRating.getDisplayOverallRating()) {
            return _parentRating.getCount();
        }else{
            return 0;
        }
    }

    public int getAverageParentRating(){
        if (_parentRating != null && _parentRating.getDisplayOverallRating()) {
            return _parentRating.getOverall();
        }else{
            return 0;
        }
    }


    public int getNumberOfSchools(){

        //if district != null, get the number of schools for the city
        ISchoolDao schoolDao = this.getSchoolDao();
        if(_school.getDistrict() == null){
            return schoolDao.countSchools(_school.getStateAbbreviation(),_school.getType(), _school.getLevelCode(), _school.getCity()) ;
        }else{
            return schoolDao.countSchoolsInDistrict(_school.getStateAbbreviation(), _school.getType(), _school.getLevelCode(), _school.getDistrictId().toString());
        }
    }

    public int getNumberOfSchoolsForGradeLevel(){

        //if district != null, get the number of schools for the city
        ISchoolDao schoolDao = this.getSchoolDao();
        if(_school.getDistrict() == null){
            return schoolDao.countSchools(_school.getStateAbbreviation(),_school.getType(), _school.getLevelCode(), _school.getCity()) ;
        }else{
            return schoolDao.countSchoolsInDistrict(_school.getStateAbbreviation(), _school.getType(), _school.getLevelCode(), _school.getDistrictId().toString());
        }
    }

    public District getDistrict(){
        return _school.getDistrict();
    }

    public String getGradeLevel() {
        StringBuilder result = new StringBuilder();
        String[] levels = _school.getLevelCode().toString().split(",");

        int counter = 0;
        for (String level : levels) {
            switch (level.charAt(0)) {
                case 'p':
                    result.append("preschool");
                    break;
                case 'e':
                    result.append("elementary");
                    break;
                case 'm':
                    result.append("middle");
                    break;
                case 'h':
                    result.append("high");
                    break;
            }
            if (levels.length > 2) {
                if (counter == levels.length - 2) {
                    result.append(" and ");
                } else if (counter < levels.length - 1) {
                    result.append(", ");
                }
            } else if (levels.length > 1) {
                if (counter == levels.length - 2) {
                    result.append(" and ");
                }
            }
            counter++;
        }
        return result.toString();
    }

    public int getNumberOfLevels() {
        String[] levels = _school.getLevelCode().toString().split(",");
        if (levels != null) {
            return levels.length;
        } else {
            return 0;
        }
    }
    public String getGradesServed() {
        Grades grades = _school.getGradeLevels();

        if (grades != null)
            return grades.getRangeString();
        else
            return "";
    }

    public static String getPossessive(String str) {
        return str + "'s";
    }

    public void setSchool(School value) {
        _school = value;
    }

    public void setGsRating(int value) {
        _gsRating = value;
    }

    public void setParentRating(Ratings value) {
        _parentRating = value;
    }


}
