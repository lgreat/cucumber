/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PaymentController.java,v 1.1 2005/05/03 01:38:30 apeterson Exp $
 */
package gs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class PaymentController extends org.springframework.web.servlet.mvc.SimpleFormController {
    private Log _log = LogFactory.getLog(PaymentController.class);

    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return super.formBackingObject(httpServletRequest);

    }

}
