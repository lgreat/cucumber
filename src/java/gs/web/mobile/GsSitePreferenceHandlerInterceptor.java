package gs.web.mobile;

import org.springframework.mobile.device.site.CookieSitePreferenceRepository;
import org.springframework.mobile.device.site.SitePreferenceHandlerInterceptor;
import org.springframework.mobile.device.site.StandardSitePreferenceHandler;

public class GsSitePreferenceHandlerInterceptor extends SitePreferenceHandlerInterceptor {
    public GsSitePreferenceHandlerInterceptor() {
        super(new StandardSitePreferenceHandler(new CookieSitePreferenceRepository(".greatschools.org")));
    }
}
