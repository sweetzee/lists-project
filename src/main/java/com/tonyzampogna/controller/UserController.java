package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
public class UserController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);


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
			value = "/users",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> createUsers(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<UserModel> userModelList) {

		if ("UPDATE".equals(action)) {
			userModelList = handleUpdateUsersRequest(createUserId, userModelList);
		}
		else if ("DELETE".equals(action)) {
			userModelList = handleDeleteUsersRequest(createUserId, userModelList);
		}
		else {
			userModelList = handleCreateUsersRequest(createUserId, userModelList);
		}

		return userModelList;
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
			value = "/users",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> updateUsers(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<UserModel> userModelList) {

		return handleUpdateUsersRequest(updateUserId, userModelList);
	}

	/**
	 * Update user credentials (for example, username and password)
	 */
	@RequestMapping(
			value = "/users/credentials",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> updateUserCredentials(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<UserModel> userModelList) {

		return handleUpdateUserCredentialsRequest(updateUserId, userModelList);
	}

	/**
	 * Delete user by ID
	 */
	@RequestMapping(
			value = "/users",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> deleteUsers(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<UserModel> userModelList) {

		return handleDeleteUsersRequest(deleteUserId, userModelList);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private List<UserModel> handleCreateUsersRequest(String createUserId, List<UserModel> userModelList) {
		log.info("A request has come in to create a user. Request User ID: " + createUserId);

		// Set the create and update fields.
		for (UserModel userModel : userModelList) {
			userModel.setCreateUser(UUID.fromString(createUserId));
			userModel.setCreateDate(new Date());
			userModel.setUpdateUser(UUID.fromString(createUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModelList = userService.createUsers(userModelList);

		return userModelList;
	}

	private UserModel handleGetUserRequest(String readUserId, String userIdOrUsername) {
		log.info("A request has come in to update a user. Request User ID: " + readUserId);

		return getUserModel(userIdOrUsername);
	}

	/**
	 * This function updates the UserModel in the database. It uses the
	 * function that does not update the username/password.
	 */
	private List<UserModel> handleUpdateUsersRequest(String updateUserId, List<UserModel> userModelList) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Set the update fields.
		for (UserModel userModel : userModelList) {
			userModel.setUpdateUser(UUID.fromString(updateUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModelList = userService.updateUsers(userModelList);

		return userModelList;
	}

	/**
	 * This function updates the UserModel credentials in the database.
	 * For example, the username and password. To update other fields,
	 * use updateUser().
	 */
	private List<UserModel> handleUpdateUserCredentialsRequest(String updateUserId, List<UserModel> userModelList) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Set the update fields.
		for (UserModel userModel : userModelList) {
			userModel.setUpdateUser(UUID.fromString(updateUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModelList = userService.updateUserCredentials(userModelList);

		return userModelList;
	}

	private List<UserModel> handleDeleteUsersRequest(String deleteUserId, List<UserModel> userModelList) {
		log.info("A request has come in to delete a user. Request User ID: " + deleteUserId);

		// Service call
		userModelList = userService.deleteUsers(userModelList);

		return userModelList;
	}
}
