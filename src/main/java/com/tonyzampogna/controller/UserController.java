package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;


@RestController
public class UserController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create a new user.
	 *
	 * It is possible to update or delete a list by ID as well
	 * using the action parameter.
	 */
	@RequestMapping(
			value = "/user",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public UserModel createUser(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody UserModel userModel) {

		if ("UPDATE".equals(action)) {
			userModel = handleUpdateUserRequest(createUserId, userModel);
		}
		else if ("DELETE".equals(action)) {
			userModel = handleDeleteUserRequest(createUserId, userModel);
		}
		else {
			userModel = handleCreateUserRequest(createUserId, userModel);
		}

		return userModel;
	}

	/**
	 * Get user by user ID or by username.
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}",
			method = RequestMethod.GET,
			produces = "application/json")
	public UserModel getUser(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername) {

		return handleGetUserRequest(readUserId, userIdOrUsername);
	}

	/**
	 * Update user by ID
	 *
	 * Note: This update will not update the username/password.
	 * Use updateUserCredentials to do that.
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public UserModel updateUser(
			@RequestParam(name = "userId") String updateUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody UserModel userModel) {

		return handleUpdateUserRequest(updateUserId, userModel);
	}

	/**
	 * Update user credentials (for example, username and password)
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/credentials",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public UserModel updateUserCredentials(
			@RequestParam(name = "userId") String updateUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody UserModel userModel) {

		return handleUpdateUserCredentialsRequest(updateUserId, userModel);
	}

	/**
	 * Delete user by ID
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public UserModel deleteUser(
			@RequestParam(name = "userId") String deleteUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody UserModel userModel) {

		return handleDeleteUserRequest(deleteUserId, userModel);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private UserModel handleCreateUserRequest(String createUserId, UserModel userModel) {
		log.info("A request has come in to create a user. Request User ID: " + createUserId);

		// Change the create and update fields.
		userModel.setCreateUser(UUID.fromString(createUserId));
		userModel.setCreateDate(new Date());
		userModel.setUpdateUser(UUID.fromString(createUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.createUser(userModel);

		return userModel;
	}

	private UserModel handleGetUserRequest(String readUserId, String userIdOrUsername) {
		log.info("A request has come in to update a user. Request User ID: " + readUserId);

		return getUserModel(userIdOrUsername);
	}

	/**
	 * This function updates the UserModel in the database. It uses the
	 * function that does not update the username/password.
	 */
	private UserModel handleUpdateUserRequest(String updateUserId, UserModel userModel) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Update the create and update fields.
		userModel.setUpdateUser(UUID.fromString(updateUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.updateUser(userModel);

		return userModel;
	}

	/**
	 * This function updates the UserModel credentials in the database.
	 * For example, the username and password. To update other fields,
	 * use updateUser().
	 */
	private UserModel handleUpdateUserCredentialsRequest(String updateUserId, UserModel userModel) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Update the create and update fields.
		userModel.setUpdateUser(UUID.fromString(updateUserId));
		userModel.setUpdateDate(new Date());

		// Service call
		userModel = userService.updateUserCredentials(userModel);

		return userModel;
	}

	private UserModel handleDeleteUserRequest(String deleteUserId, UserModel userModel) {
		log.info("A request has come in to delete a user. Request User ID: " + deleteUserId);

		// Service call
		userModel = userService.deleteUser(userModel);

		return userModel;
	}
}
