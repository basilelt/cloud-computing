package fr.uha.ensisa.ff.todo.app.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping(value="/")
	public String home() throws IOException{
		return "forward:/list";
	}
}
