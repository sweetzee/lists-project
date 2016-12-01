package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;


	@RequestMapping("/user")
	public UserModel getUser() {
		UserModel userModel = userService.getUserByUsername("test0001");
		return userModel;
	}

}
