package gs.web.request;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext _applicationContext = null;

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        _applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return _applicationContext;
    }
}
