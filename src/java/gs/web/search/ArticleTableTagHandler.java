package gs.web.search;

import gs.data.state.State;
import gs.data.content.Article;
import gs.data.search.highlight.TextHighlighter;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleTableTagHandler extends ResultsTableTagHandler {

    private List _articles;

    public void setArticles(List articles) {
        _articles = articles;
    }

    public String getConstraint() {
        return "article";
    }

    public void doTag() throws IOException {

        if (_articles != null) {
            JspWriter out = getJspContext().getOut();

            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr><td valign=\"top\">");
            out.println("<table class=\"article_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
            if (_articles.size() > 0) {
                out.println("<tr>");
                out.println("<th class=\"result_title\" width=\"1\">&nbsp;</th>");
                out.println("<th class=\"left\">Article Title and Abstract</th>");

                out.println("<th class=\"result_title\" width=\"1\">&nbsp;</th>");
                out.println("</tr>");

                State s = getStateOrDefault();

                for (int i = 0; i < _articles.size(); i++) {
                    Article article = getArticle((SearchResult) _articles.get(i));
                    out.println("<tr class=\"result_row\">");
                    out.println("<td width=\"1\">&nbsp;</td>");
                    out.println("<td>");
                    out.print("<a href=\"http://");
                    out.print(getHostname());
                    out.print("/cgi-bin/show");
                    if (article.isInsider()){
                        out.print("part");
                    }
                    out.print("article/");
                    out.print(s.getAbbreviationLowerCase());
                    out.print("/");
                    out.print(article.getId());
                    out.print("\">");

                    out.print(TextHighlighter.highlight(escapeLongstate(article.getTitle()), getQueryString(), "title"));
                    out.print("</a><br/>");
                    out.print(TextHighlighter.highlight(escapeLongstate(article.getAbstract()), getQueryString(), "abstract"));
                    out.println("</td><td></td>");
                    out.println("</tr>");
                }
                out.println("<tr class=\"last_row\"><td colspan=\"5\"><ul>");
                out.print("<li class=\"viewall\"><a href=\"http://");
                out.print(getHostname());
                out.print("/content/allArticles.page?state=");
                out.print(s.getAbbreviation());
                out.print("\">Browse all Articles</a></li></ul></td></tr>");



                out.println("</table>");
                out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
                out.println("<td>&nbsp;</td>");
                out.println("<td class=\"results_pagenav\">");
                writePageNumbers(out);
            } else {
                out.println("<tr><th class=\"left result_title\">No articles found</div></th></tr>");
                out.println("<tr class=\"last_row\"><td colspan=\"5\"><ul>");
                out.print("<li class=\"viewall\">");
                writeBrowseAllArticlesLink(out);
                out.println("</li></ul></td></tr>");
                out.println("<tr><td valign=\"top\" height=\"100\">");
            }
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");

        }
    }
}
