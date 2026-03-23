package fr.uha.ensisa.ff.todo.app.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import fr.uha.ensisa.ff.todo.Task;
import fr.uha.ensisa.ff.todo.dao.DaoFactory;

@Controller
public class TaskController {
	
	@Autowired
	public DaoFactory daoFactory;
	
	@Autowired
	public String hostname;

	@Autowired
	public Boolean authEnabled;
	
	@RequestMapping(value="/list")
	public ModelAndView list(@RequestAttribute(required=false) String userName) throws IOException{
		ModelAndView ret = new ModelAndView("list");
		ret.addObject("hostname", hostname);
		ret.addObject("daobk", daoFactory);
		ret.addObject("tasks", daoFactory.getTaskDao().findAll(userName));
		ret.addObject("userName", userName);
		return ret;
	}

	@RequestMapping(value="/edit")
	public ModelAndView edit(@RequestAttribute(required=false) String userName, @RequestParam(required=false)Long id) throws IOException{
		ModelAndView ret = new ModelAndView("edit");
		Task t = null;
		if (id != null && id > 0) {
			t = daoFactory.getTaskDao().find(userName, id);
		}
		if (t == null) {
			t = new Task();
		}
		ret.addObject("task", t);
		return ret;
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	public String edit(@RequestAttribute(required=false) String userName, @ModelAttribute Task newTask) throws IOException{
		newTask.setUser(userName);
		if (newTask.getId() < 0) {
			daoFactory.getTaskDao().store(newTask);
		} else {
			daoFactory.getTaskDao().update(newTask);
		}
		return "redirect:/list";
	}

	@RequestMapping(value="/delete")
	public String delete(@RequestAttribute(required=false) String userName, @RequestParam(required=true)Long id) throws IOException{
		Task t = new Task();
		t.setId(id);
		t.setUser(userName);
		daoFactory.getTaskDao().remove(t);
		return "redirect:/list";
	}
	
	@RequestMapping(value="/create")
	public String create(@RequestAttribute(required=false) String userName) throws IOException{
		Task newTask = new Task();
		newTask.setUser(userName);
		daoFactory.getTaskDao().store(newTask);
		return "redirect:/list";
	}
}
