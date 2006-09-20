/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagManager.java,v 1.2 2006/09/20 17:06:39 dlee Exp $
 */
package gs.web.ads;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Singleton that retrieves the ad code for cobrands that serve their own ads
 *
 * @see gs.web.util.PageHelper#isAdServedByCobrand()
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public final class AdTagManager {

    private Map _adCodesByCobrand;
    private static final AdTagManager AD_INSTANCE = new AdTagManager();


    public static AdTagManager getInstance() {
        return gs.web.ads.AdTagManager.AD_INSTANCE;
    }

    private AdTagManager() {
        _adCodesByCobrand = new HashMap();
        Document document;

        try {
            ClassPathResource resource = new ClassPathResource("gs/web/ads/cobranded-ad-codes.xml");
            URL url = resource.getURL();
            SAXReader reader = new SAXReader();
            document = reader.read(url);
            Element root = document.getRootElement();

            // iterate through child elements of root
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element cobrand = (Element) i.next();
                String cobrandName = cobrand.attributeValue("id");

                for (Iterator j = cobrand.elementIterator(); j.hasNext();) {
                    Element ad = (Element) j.next();

                    String adName = ad.attributeValue("id");
                    AdPosition adPosition = AdPosition.getAdPosition(adName);

                    String adTag = ad.getText();
                    String key = getCobrandKey(cobrandName, adPosition);
                    if (StringUtils.isEmpty(adTag)) {
                        throw new RuntimeException("Empty ad tag: " + key);
                    }

                    if (_adCodesByCobrand.containsKey(key)) {
                        throw new RuntimeException("Key already Exists: " + key);
                    } else {
                        _adCodesByCobrand.put(key, adTag);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            document = null;
        }
    }

    /**
     * Get the ad tag for a particular cobrand and ad position.
     * If no ad tag is found, an empty string is returned.
     * @param cobrand
     * @param adPosition
     * @return
     */
    public String getAdTag(String cobrand, AdPosition adPosition) {
        if (StringUtils.isEmpty(cobrand) || null == adPosition) {
            throw new IllegalArgumentException("Must specify cobrand and ad position");
        }

        String key = getCobrandKey(cobrand, adPosition);
        if (_adCodesByCobrand.containsKey(key)) {
            return (String) _adCodesByCobrand.get(key);
        } else {
            return "";
        }
    }

    /**
     * for testing only.  Should never have to use this method.
     * @param cobrand
     * @param adPosition
     * @return
     */
    protected String getCobrandKey(String cobrand, AdPosition adPosition) {
        if (cobrand.matches("yahoo|yahooed")) {
            cobrand = "yahoo";
        }
        return cobrand + adPosition.getName();
    }
}
