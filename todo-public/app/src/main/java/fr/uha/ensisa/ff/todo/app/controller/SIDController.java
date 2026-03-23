package fr.uha.ensisa.ff.todo.app.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import fr.uha.ensisa.ff.todo.app.config.Session;
import fr.uha.ensisa.ff.todo.app.config.SessionInterceptor;

@Controller
public class SIDController {
	
	@Autowired
	private Map<String, Session> sessions;

	@Autowired
	private Boolean authEnabled;

	@RequestMapping(value = "/auth", method = RequestMethod.GET)
	public ModelAndView auth(@RequestAttribute(required=false) String userName) throws IOException {
		if (!authEnabled || userName != null) return new ModelAndView("redirect:/");
		return new ModelAndView("auth");
	}

	@RequestMapping(value = "/auth", method = RequestMethod.POST)
	public String auth(@RequestParam(value = "name", required = true) String name, HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		if (authEnabled) {
			Session session = new Session(name);
			SessionInterceptor.renewCookie(req, res, session);
			sessions.put(session.getId(), session);
			System.out.println(new Date().toString() + " - created session " + session.getId());
		}
		return "redirect:/";
	}

	@RequestMapping(value = "/auth/disconnect")
	public String disconnect(@CookieValue(name=SessionInterceptor.SessionCookieName, required=false) String sid, HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (authEnabled) {
			Cookie cookie = new Cookie(SessionInterceptor.SessionCookieName, null);
			cookie.setPath(req.getContextPath());
			cookie.setMaxAge(0);
			res.addCookie(cookie);
			if (sid != null && (sid = sid.trim()).length() > 0) sessions.remove(sid);
		}
		return "redirect:/auth";
	}
}
