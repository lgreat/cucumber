/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: StateValidator.java,v 1.4 2009/12/04 20:54:17 npatury Exp $
 */
package gs.web.util.validator;

import gs.data.state.State;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class StateValidator implements Validator {
    public static final String BEAN_ID = "stateValidator";

    public static interface IState {
        State getState();
    }

    public boolean supports(Class aClass) {
        Class [] iFaces = aClass.getInterfaces();
        for (int i=0; i < iFaces.length; i++) {
            if (iFaces[i].equals(IState.class)) {
                return true;
            }
        }
        return false;
    }

    public void validate(Object object, Errors errors) {
        IState command = (IState)object;
        if (command.getState() == null) {
            errors.rejectValue("state", "invalid_state", "A valid state was not specified.");
        }
    }
}
