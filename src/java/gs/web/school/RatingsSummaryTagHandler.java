package gs.web.school;

import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;

import gs.data.school.*;
import gs.web.util.context.SessionContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author jnorton@greatschools.org
 */
public class RatingsSummaryTagHandler extends BaseTagHandler {


    private School _school;

    public void doTag() throws JspException, IOException {

        SchoolSummaryHelper helper = new SchoolSummaryHelper();
        RatingsSummaryCommand command = helper.buildRatingsSummaryCommand(_school, this.getSchoolDao(), this.getReviewDao()) ;

        SchoolMessageBuilder builder = helper.getMessageBuilder(_school);

        writeOpeningDiv();

        writeParagraph(builder.buildFirstSentence(command) + "  " + builder.buildSecondSentence(command), "summary");

        String p = "";
        if (builder.getClass().isAssignableFrom(PreschoolMessageBuilder.class)){
            boolean schoolNameLT_M = command.schoolName.toUpperCase().charAt(0) < 'M';

            if (command.numberOfParentReviews <= 2 && schoolNameLT_M) {
                p =  builder.buildThirdSentence(command) + "  " + getReviewSchoolLink(builder.buildParentRatingMessage(command));
            } else if (command.numberOfParentReviews <= 2) {
                p = getReviewSchoolLink(builder.buildParentRatingMessage(command)) + "  " + builder.buildThirdSentence(command);
            } else {
                p =  builder.buildThirdSentence(command);
            }

        }else{
            p = builder.buildThirdSentence(command);
            if (!command.hasParentReviews){
                p += "  " + getReviewSchoolLink("Share your review!");
            }
        }


        writeParagraph(p, "last");

        writeClosingDiv();
    }

    public String getReviewSchoolLink(String s) {
        UrlBuilder b = new UrlBuilder(_school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        PageContext pageContext = (PageContext)getJspContext();
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();

        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(b.asSiteRelativeXml(request));
        sb.append("\" class=\"submodal-344-362 addParentReviewLink\"");
        sb.append(">") ;
        sb.append(s);
        sb.append("</a>");

        return sb.toString();
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

    public void writeParagraph(String s, String styleClass) throws JspException, IOException {

        final String OPEN_PARAGRAPH = "<p class=\"" + styleClass + "\">";
        final String CLOSE_PARAGRAPH = "</p>";

        getJspContext().getOut().write(OPEN_PARAGRAPH + s + CLOSE_PARAGRAPH);
    }

    public void setSchool(School value) {
        _school = value;
    }



}
