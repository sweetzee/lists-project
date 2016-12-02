package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create (possibly Update or Delete as well)
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/user", consumes = "application/json", produces = "application/json")
	public UserModel createUser(@RequestParam(name = "userId") String createUserId, @RequestParam(name = "action", required = false) String action, @RequestBody UserModel userModel) {
		if ("UPDATE".equals(action)) {
			userModel = updateUserModel(createUserId, userModel);
		}
		else if ("DELETE".equals(action)) {
			userModel = deleteUserModel(createUserId, userModel);
		}
		else {
			userModel = createUserModel(createUserId, userModel);
		}
		return userModel;
	}

	/**
	 * Read
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/user/{userIdOrUsername}", produces = "application/json")
	public UserModel getUser(@RequestParam(name = "userId") String readUserId, @PathVariable(name = "userIdOrUsername") String userIdOrUsername) {
		return getUserModel(userIdOrUsername);
	}

	/**
	 * Update
	 *
	 * Note: This update will not update the username/password. Use updateUserCredentials to do that.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/user", consumes = "application/json", produces = "application/json")
	public UserModel updateUser(@RequestParam(name = "userId") String updateUserId, @RequestBody UserModel userModel) {
		return updateUserModel(updateUserId, userModel);
	}

	/**
	 * Update Credentials
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/user/{user}/credentials", consumes = "application/json", produces = "application/json")
	public UserModel updateUserCredentials(@RequestParam(name = "userId") String updateUserId, @RequestBody UserModel userModel) {
		return updateUserModelCredentials(updateUserId, userModel);
	}

	/**
	 * Delete
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/user", consumes = "application/json", produces = "application/json")
	public UserModel deleteUser(@RequestParam(name = "userId") String deleteUserId, @RequestBody UserModel userModel) {
		return deleteUserModel(deleteUserId, userModel);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	/**
	 * This function creates the UserModel in the database.
	 */
	private UserModel createUserModel(String createUserId, UserModel userModel) {
		// Change the create and update fields.
		userModel.setCreateUser(UUID.fromString(createUserId));
		userModel.setCreateDate(new Date());
		userModel.setUpdateUser(UUID.fromString(createUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.createUser(userModel);

		return userModel;
	}

	/**
	 * This function returns the UserModel from the database.
	 *
	 * The function uses a regular expression to determine
	 * if the argument passed in is the userId or the username.
	 *
	 * @Param user: Either a user_id (String in the form of UUID) or a username
	 */
	private UserModel getUserModel(String userIdOrUsername) {
		UserModel userModel = null;

		// Check to see if it is a UUID or a username.
		if (!StringUtils.isEmpty(userIdOrUsername)) {
			Pattern pattern = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
			Matcher matcher = pattern.matcher(userIdOrUsername);
			if (matcher.matches()) {
				userModel = userService.getUserById(UUID.fromString(userIdOrUsername));
			}
			else {
				userModel = userService.getUserByUsername(userIdOrUsername);
			}
		}

		return userModel;
	}

	/**
	 * This function updates the UserModel in the database. It uses the
	 * function that does not update the username/password.
	 */
	private UserModel updateUserModel(String updateUserId, UserModel userModel) {
		// Update the create and update fields.
		userModel.setUpdateUser(UUID.fromString(updateUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.updateUser(userModel);

		return userModel;
	}

	/**
	 * This function updates the UserModel in the database.
	 */
	private UserModel updateUserModelCredentials(String updateUserId, UserModel userModel) {
		// Update the create and update fields.
		userModel.setUpdateUser(UUID.fromString(updateUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.updateUserCredentials(userModel);

		return userModel;
	}

	/**
	 * This function deletes the UserModel in the database.
	 */
	private UserModel deleteUserModel(String deleteUserId, UserModel userModel) {
		// Update the create and update fields.
		userModel.setUpdateUser(UUID.fromString(deleteUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.deleteUser(userModel);

		return userModel;
	}
}
