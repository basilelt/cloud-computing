package fr.uha.ensisa.ff.todo.app.config;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

public final class SessionInterceptor implements HandlerInterceptor {
	public static final String SessionCookieName = "sid";
	public static final String SessionNameAttribute = "userName";
	public static final int SessionTimeoutS = 25;
	public static final long SessionTimeout = TimeUnit.SECONDS.toMillis(SessionTimeoutS);

	public static void renewCookie(HttpServletRequest req, HttpServletResponse res, Session session) {
		Cookie cookie = new Cookie(SessionInterceptor.SessionCookieName, session.getId());
		cookie.setPath(req.getContextPath());
		cookie.setMaxAge(SessionInterceptor.SessionTimeoutS);
		cookie.setHttpOnly(true);
		//cookie.setSecure(true);
		res.addCookie(cookie);
	}
	
	@Autowired
	private Boolean authEnabled;

	@Autowired
	private Map<String, Session> sessions;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!authEnabled) return true;
		
		String path = request.getServletPath();
		System.out.println(new Date().toString() + " - requested " + path);
		
		Session session = findSession(request);
		if (session == null) {
			// It's OK not to have a session when authenticating
			if (path != null && path.startsWith("/auth")) return true;
			
			// otherwise, one should authenticate
			String rootPath = request.getContextPath();
			if (rootPath.endsWith("/")) rootPath = rootPath.substring(0, rootPath.length()-1);
			response.sendRedirect(rootPath + "/auth");
			return false;
		}
		
		request.setAttribute(SessionNameAttribute, session.getName());
		session.touch();
		sessions.put(session.getId(), session); // Session was updated (endTime att)
		renewCookie(request, response, session);
		
		return true;
	}
	
	private Session findSession(HttpServletRequest request) {
		Session session;
		Cookie sessionCookie = WebUtils.getCookie(request, SessionCookieName);
		if (sessionCookie == null) {
			return null;
		}
		
		session = sessions.get(sessionCookie.getValue());
		if (session == null) {
			return null;
		} else if (!session.isValid()) {
			System.out.println(new Date().toString() + " - expired session " + sessionCookie.getValue());
			sessions.remove(sessionCookie.getValue());
			return null;
		}
		
		return session;
	}
}
