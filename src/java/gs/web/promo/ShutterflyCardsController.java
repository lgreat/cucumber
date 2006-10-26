package gs.web.promo;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 *
 * This controller delivers three different views based on the following logic:
 *
 * <ol>
 * <li>Valid (members before 11/8/06) members who have not yet been sent a promo code
 *     are shown a success view and are sent an email with the promo code.</li>
 * <li>Members who have already received a promo code are shown an "already been
 *     redeemed" view</li>
 * <li>All other users are shown an "ineligible" view</li>
 * </ol>
 */
public class ShutterflyCardsController extends AbstractController {
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
