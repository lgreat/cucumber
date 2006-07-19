package gs.web;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * This is a temporary fix to workaround that the current 1.2.6 Spring Mock does not allow
 * updating request pararameters. This problem will go away if the Spring team implements:
 *
 * http://opensource2.atlassian.com/projects/spring/browse/SPR-1505
 *
 * @author thuss
 */
public class GsMockHttpServletRequest extends MockHttpServletRequest   {

    /**
     * Sets the given parameter, removing any previous value.
     * I've implemented this method as a workaround to allow updating existing request parameters
     *
     * @param name The parameter name
     * @param value The new parameter value
     */
    public void setParameter(String name, String value) {
        if (getParameterValues(name) != null && getParameterValues(name).length > 0) {
            getParameterValues(name)[0] = value;
        } else {
            addParameter(name, value);
        }
    }
}
