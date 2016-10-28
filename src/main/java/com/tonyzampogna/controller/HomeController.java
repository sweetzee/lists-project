package com.tonyzampogna.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;


@RestController
public class HomeController {

	@RequestMapping("/home")
	public ModelAndView getHome() {
		ModelAndView model = new ModelAndView("home");
		model.addObject("time", new Date());
		model.addObject("message", "hello world");
		return model;
	}

}
