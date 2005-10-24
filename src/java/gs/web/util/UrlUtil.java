/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UrlUtil.java,v 1.1 2005/10/24 21:53:04 apeterson Exp $
 */

package gs.web.util;

import gs.data.state.State;

import javax.servlet.http.HttpServletRequest;

/**
 * Wrapping and URL munging tools.
 * These are distinct from the GSData ones, which are general utilities. These have to do
 * with the specific GSWeb environment, especially the session facade.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public final class UrlUtil {

    /**
     * Generates a url from the given requested resource. Common bottleneck for URL
     * building.
     * <p />
     * This routine can grow as we decide to do more or less with URLs.
     * <p />
     * It does substitution of $VARIABLES, per Perl world. The current variables
     * supported are:
     * <ul>
     * <li>$STATE
     * </ul>
     * This variable is taken from the request attribute named of the same name,
     * and if not found there, it uses the ISessionFacade available from the
     * request.
     * <p />
     * It will add the appropriate server name if this is a link to a perl page.
     * This is most helpful in the dev environment, but this bottleneck allows us
     * to use the same code for test and deploy environments.
     * <p />
     * It does not guarantee to build the smallest possible URL, but it attempts
     * to do so.
     */
    public String buildUrl(final String ref, HttpServletRequest request) {
        // If the URL has a STATE string in it (or more than one), replace it with the
        // user's state.

        gs.data.util.NetworkUtil networkUtil = new gs.data.util.NetworkUtil();

        String href = ref;

        if (href.indexOf("STATE") != -1) {
            // Allow a request attribute to override the session facade.
            if (request.getAttribute("STATE") != null &&
                    request.getAttribute("STATE") instanceof State) {
                State s = (State) request.getAttribute("STATE");
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
            } else {
                gs.web.ISessionFacade context = gs.web.SessionFacade.getInstance(request);
                gs.data.state.State s = context.getStateOrDefault();
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
            }
        }

        final String src = request.getRequestURL().toString();

        href = networkUtil.buildHref(href, false, src);

        return href;
    }
}