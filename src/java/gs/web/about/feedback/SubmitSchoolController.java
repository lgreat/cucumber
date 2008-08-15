package gs.web.about.feedback;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.HashMap;

/**
 * @author 
 */
public class SubmitSchoolController extends SimpleFormController {
    String _type;

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object o, BindException errors) {
        
    }

    @Override
    protected ModelAndView onSubmit(Object o) throws ServletException {
        return new ModelAndView(getSuccessView());
    }

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        SubmitSchoolCommand command = (SubmitSchoolCommand)o;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type", getType());
        return map;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }    
}
