package gs.web.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chriskimm@greatschools.net
 */
public class SpringUtil {

    private static final ApplicationContext _applicationContext;

    // This static block ensures that Spring's BeanFactory is only loaded once
    static {
        String[] paths = {"classpath:gs/data/applicationContext-data.xml",
                          "classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml",
                          "classpath:gs/data/school/performance/applicationContext-performance.xml"
        };
        _applicationContext =
                new ClassPathXmlApplicationContext(paths, gs.data.util.SpringUtil.getApplicationContext());
    }

    /**
     * @return The Spring ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    // prevent instantiation
    private SpringUtil() { }
}
