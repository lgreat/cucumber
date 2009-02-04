package gs.web.api;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.WebUtils;

public abstract class GSServletAnnotationMappingUtils {

	public static boolean checkRequestMethod(RequestMethod[] methods, HttpServletRequest request) {
		if (!ObjectUtils.isEmpty(methods)) {
			boolean match = false;
			for (RequestMethod method : methods) {
				if (method.name().equals(request.getMethod())) {
					match = true;
				}
			}
			if (!match) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkParameters(String[] params, HttpServletRequest request) {
		if (!ObjectUtils.isEmpty(params)) {
			for (String param : params) {
				int separator = param.indexOf('=');
				if (separator == -1) {
					if (param.startsWith("!")) {
						if (WebUtils.hasSubmitParameter(request, param.substring(1))) {
							return false;
						}
					} else if (!WebUtils.hasSubmitParameter(request, param)) {
						return false;
					}
				} else {
					String key = param.substring(0, separator);
					String value = param.substring(separator + 1);
					if (!value.equals(request.getParameter(key))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
