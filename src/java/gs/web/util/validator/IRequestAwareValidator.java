/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: IRequestAwareValidator.java,v 1.3 2009/12/04 22:27:16 chriskimm Exp $
 */
package gs.web.util.validator;

import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

/**
 * Validator that has access to HttpServletRequest
 *
 * Spring validators are not web specific so they know nothing about
 * HttpServletRequest.
 *
 * To get access to HttpServletRequest, you have a couple of options:
 * 1) Set HttpServletRequest to be a property in your command object,
 * continue to use Spring's validation framework
 *
 * 2) Implement this interface and then use it by overriding the form controller's
 * onBindAndValidate method
 *
 * @see org.springframework.web.servlet.mvc.AbstractFormController#onBindAndValidate(javax.servlet.http.HttpServletRequest, Object, org.springframework.validation.BindException)
 * @see org.springframework.validation.Validator
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public interface IRequestAwareValidator {
    
    void validate(HttpServletRequest request, Object object, Errors errors);

}
