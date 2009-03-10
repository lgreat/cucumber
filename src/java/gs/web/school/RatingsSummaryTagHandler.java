package gs.web.school;

import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;

import gs.data.school.*;

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


    private School _school;

    public void doTag() throws JspException, IOException {


        SchoolSummaryHelper helper = new SchoolSummaryHelper();
        SchoolSummaryHelper.RatingsSummaryCommand command = helper.buildRatingsSummaryCommand(_school, this.getSchoolDao(), this.getReviewDao()) ;
        SchoolSummaryHelper.SchoolMessageBuilder builder = helper.getMessageBuilder(command);

        writeOpeningDiv();

        writeParagraph(builder.buildFirstSentence() + "  " + builder.buildSecondSentence());

        String p = builder.buildThirdSentence();
        if (!command.hasParentReviews){
            p += "  " + getReviewSchoolLink();
        }

        writeParagraph(p);

        writeClosingDiv();
    }

    public String getReviewSchoolLink(){
        UrlBuilder b = new UrlBuilder(_school, UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
        PageContext pageContext = (PageContext)getJspContext();
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        return b.asAHref (request, "Be the first to share your review!", "addParentReviewLink submodal-344-362 GS_CI9_");
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

    public void setSchool(School value) {
        _school = value;
    }



}
