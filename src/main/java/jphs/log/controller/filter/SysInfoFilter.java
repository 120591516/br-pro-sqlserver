
package jphs.log.controller.filter;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jphs.log.utils.HttpRequestResolve;

public class SysInfoFilter implements HandlerInterceptor {

	private String reqeustUrl;

	private final Logger logger = LoggerFactory.getLogger("");

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssss");

	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		System.out.println("preHandle......");
		reqeustUrl = request.getRequestURI();
		logger.info("preHandle Request--> URL is " + reqeustUrl + ",parameter is "
				+ HttpRequestResolve.getRequestBody(request));
		System.out.println((new Date()) + " preHandle Request--> URL is " + reqeustUrl + ",parameter is "
				+ HttpRequestResolve.getRequestBody(request));
		return true;
	}

}
