package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;


@RestController
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;


	@RequestMapping("/user")
	public ModelAndView getHome() {
		UserModel userModel = userService.getUserByUsername("test");

log.info(userModel.getUserId().toString());
log.info(userModel.getUsername());


		ModelAndView model = new ModelAndView("home");
		model.addObject("time", new Date());
		model.addObject("message", "hello world");
		return model;
	}

}
