package gs.web.content.cms;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.GenericCollectionTypeResolver;
import gs.data.content.cms.CmsEmbeddedLinks;
import gs.data.content.cms.CmsContent;
import gs.data.content.cms.CmsLink;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.lang.reflect.InvocationTargetException;

public class CmsContentLinkResolver {

    public void replaceEmbeddedLinks(CmsContent content) throws Exception {
        BeanWrapper wrapper = new BeanWrapperImpl(content);

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (AnnotationUtils.getAnnotation(pd.getReadMethod(), CmsEmbeddedLinks.class) != null) {
                if (pd.getReadMethod().invoke(content) == null) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(pd.getPropertyType())) {
                    Class klass = GenericCollectionTypeResolver.getCollectionReturnType(pd.getReadMethod());

                    if (klass != null && CmsLink.class.isAssignableFrom(klass)) {
                        for (CmsLink link : (Collection<CmsLink>) pd.getReadMethod().invoke(content)) {
                            link.setUrl(CmsContentUtils.replaceGreatSchoolsUrlInString(link.getUrl()));
                        }
                    }

                    // TODO this doesn't work for nested items? e.g. CmsTopicCenter -> List<CmsSubtopic> -> List<CmsSubSubtopic> -> List<CmsLink>
                    
                } else if (CmsLink.class.isAssignableFrom(pd.getPropertyType())) {
                    CmsLink link = (CmsLink) pd.getReadMethod().invoke(content);
                    link.setUrl(CmsContentUtils.replaceGreatSchoolsUrlInString(link.getUrl()));
                } else if (String.class.isAssignableFrom(pd.getPropertyType())) {
                    String toReplace = (String) pd.getReadMethod().invoke(content);
                    String replace = CmsContentUtils.replaceGreatSchoolsUrlInString(toReplace);
                    pd.getWriteMethod().invoke(content, replace);
                }

            }
        }
    }
}
