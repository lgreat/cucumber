package gs.web.school;

import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;

import gs.data.school.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author jnorton@greatschools.net
 */
public class RatingsSummaryTagHandler extends BaseTagHandler {


    private School _school;

    public void doTag() throws JspException, IOException {

        SchoolSummaryHelper helper = new SchoolSummaryHelper();
        RatingsSummaryCommand command = helper.buildRatingsSummaryCommand(_school, this.getSchoolDao(), this.getReviewDao()) ;
        SchoolMessageBuilder builder = helper.getMessageBuilder(command);

        writeOpeningDiv();

        writeParagraph(builder.buildFirstSentence(command) + "  " + builder.buildSecondSentence(command));

        String p = builder.buildThirdSentence(command);
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

        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(b.asSiteRelativeXml(request));

        sb.append("\" class=\"submodal-344-362 addParentReviewLink\"");
        sb.append(" onclick=\"Popup=window.open(this.href,'Popup','toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no, width=344,height=362,left=50,top=50'); return false; \"");
        sb.append(">") ;
        sb.append("Be the first to share your review!");
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

    public void setSchool(School value) {
        _school = value;
    }



}
