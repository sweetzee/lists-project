package com.tonyzampogna.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.Map;


@RestController
public class HomeController {

	@RequestMapping("/home")
	public ModelAndView getHome() {
		ModelAndView model = new ModelAndView("test");
		model.addObject("msg", "hello world");
		return model;
	}

	@GetMapping("/home2")
	public String welcome(Map<String, Object> model) {
		model.put("time", new Date());
		model.put("message", "hello");
		return "home2";
	}

}
