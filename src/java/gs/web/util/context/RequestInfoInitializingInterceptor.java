package gs.web.util.context;


import gs.data.school.ISchoolDao;
import gs.data.util.SpringUtil;
import gs.web.mobile.Device;
import gs.web.request.RequestInfo;
import gs.web.util.PageHelper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import net.sourceforge.wurfl.spring.SpringWurflManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestInfoInitializingInterceptor implements HandlerInterceptor, BeanFactoryAware {

    private BeanFactory _beanFactory;

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object o) throws Exception {

        RequestInfo requestInfo = new RequestInfo(request);
        SpringWurflManager springWurflManager = (SpringWurflManager) _beanFactory.getBean("springWurflManager");
        if (springWurflManager != null) {
            requestInfo.setDevice(new Device(springWurflManager.getDeviceForRequest(request)));
        }
        request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, requestInfo);

        return true; // go on
    }


    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // nothing
    }

    public BeanFactory getBeanFactory() {
        return _beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        _beanFactory = beanFactory;
    }
}
