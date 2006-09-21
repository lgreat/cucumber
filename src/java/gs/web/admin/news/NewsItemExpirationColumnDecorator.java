package gs.web.admin.news;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.exception.DecoratorException;

import javax.servlet.jsp.PageContext;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NewsItemExpirationColumnDecorator implements DisplaytagColumnDecorator {
    public static final String EXPIRES_DATE_FORMAT = "MMM d, yyyy 'at' hh:mm aaa";
    public static final String EXPIRED_DATE_CLASS_NAME = "expired";
    /**
     * Keep as member variable so it gets initialized only once per page view, and not
     * once per table row.
     */
    private SimpleDateFormat _sdf;
    private Date _now;

    /**
     * Initialized once per table
     */
    public NewsItemExpirationColumnDecorator() {
        _sdf = new SimpleDateFormat(EXPIRES_DATE_FORMAT);
        _now = new Date();
    }

    public Object decorate(Object object, PageContext pageContext, MediaTypeEnum mediaTypeEnum) throws DecoratorException {
        if (object != null) {
            Date expiry = (Date) object;
            String formattedDate = _sdf.format(expiry);
            if (_now.after(expiry)) {
                return "<span class=\"" + EXPIRED_DATE_CLASS_NAME + "\">" + formattedDate + "</span>";
            }
            return formattedDate;
        }
        return null;
    }
}
