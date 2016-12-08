package com.tonyzampogna.controller;

import com.tonyzampogna.domain.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Handle requests from HTTP.
 *
 * The controller:
 * 	- Checks to see if the user has permissions to perform the operation.
 * 	- Validates the input to make sure it's clean.
 * 	- Any other business logic.
 * 	- Sends appropriate error messages back, if necessary.
 */
@RestController
public class UserController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create Users (Update, Delete as well)
	 */
	@RequestMapping(
			value = "/users",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> createUsers(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<UserModel> userModels) {

		if ("UPDATE".equals(action)) {
			userModels = handleUpdateUsersRequest(createUserId, userModels);
		}
		else if ("DELETE".equals(action)) {
			userModels = handleDeleteUsersRequest(createUserId, userModels);
		}
		else {
			userModels = handleCreateUsersRequest(createUserId, userModels);
		}

		return userModels;
	}

	/**
	 * Get Users (by user ID or by username)
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
	 * Update Users (not username/password, though)
	 */
	@RequestMapping(
			value = "/users",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> updateUsers(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<UserModel> userModels) {

		return handleUpdateUsersRequest(updateUserId, userModels);
	}

	/**
	 * Update User Credentials (for example, username and password)
	 */
	@RequestMapping(
			value = "/users/credentials",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> updateUserCredentials(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<UserModel> userModels) {

		return handleUpdateUserCredentialsRequest(updateUserId, userModels);
	}

	/**
	 * Delete Users
	 */
	@RequestMapping(
			value = "/users",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<UserModel> deleteUsers(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<UserModel> userModels) {

		return handleDeleteUsersRequest(deleteUserId, userModels);
	}


	/////////////////////////////////////////////////
	// Handler Methods
	/////////////////////////////////////////////////

	private List<UserModel> handleCreateUsersRequest(String createUserId, List<UserModel> userModels) {
		log.info("A request has come in to create a user. Request User ID: " + createUserId);

		// Validate the permission for the user.
		validateCreateUserPermissions(createUserId, userModels);

		// Set the create and update fields.
		for (UserModel userModel : userModels) {
			userModel.setCreateUser(UUID.fromString(createUserId));
			userModel.setCreateDate(new Date());
			userModel.setUpdateUser(UUID.fromString(createUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModels = userService.createUsers(userModels);

		return userModels;
	}

	private UserModel handleGetUserRequest(String readUserId, String userIdOrUsername) {
		log.info("A request has come in to update a user. Request User ID: " + readUserId);

		UserModel userModel = getUserModel(userIdOrUsername);

		// Validate the permission for the user.
		validateReadUserPermissions(readUserId, userModel);

		return userModel;
	}

	/**
	 * This function updates the UserModel in the database. It uses the
	 * function that does not update the username/password.
	 */
	private List<UserModel> handleUpdateUsersRequest(String updateUserId, List<UserModel> userModels) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Validate the permission for the user.
		validateUpdateUserPermissions(updateUserId, userModels);

		// Set the update fields.
		for (UserModel userModel : userModels) {
			userModel.setUpdateUser(UUID.fromString(updateUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModels = userService.updateUsers(userModels);

		return userModels;
	}

	/**
	 * This function updates the UserModel credentials in the database.
	 * For example, the username and password. To update other fields,
	 * use updateUser().
	 */
	private List<UserModel> handleUpdateUserCredentialsRequest(String updateUserId, List<UserModel> userModels) {
		log.info("A request has come in to update a user. Request User ID: " + updateUserId);

		// Validate the permission for the user.
		validateUpdateUserPermissions(updateUserId, userModels);

		// Set the update fields.
		for (UserModel userModel : userModels) {
			userModel.setUpdateUser(UUID.fromString(updateUserId));
			userModel.setUpdateDate(new Date());
		}

		// Service call
		userModels = userService.updateUserCredentials(userModels);

		return userModels;
	}

	private List<UserModel> handleDeleteUsersRequest(String deleteUserId, List<UserModel> userModels) {
		log.info("A request has come in to delete a user. Request User ID: " + deleteUserId);

		// Validate the permission for the user.
		validateDeleteUserPermissions(deleteUserId, userModels);

		// Service call
		userModels = userService.deleteUsers(userModels);

		return userModels;
	}


	/////////////////////////////////////////////////
	// Permission Validation Methods
	/////////////////////////////////////////////////

	private void validateCreateUserPermissions(String requestUserId, List<UserModel> userModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (UserModel userModel : userModels) {
			boolean canCreateUser = false;
			UUID userId = userModel.getUserId();

			// Admins can create users.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canCreateUser = true;
			}

			// Users can create users with the same ID.
			if (requestUserModel.getUserId() != null &&
				requestUserModel.getUserId().equals(userId)) {
				canCreateUser = true;
			}

			if (!canCreateUser) {
				throw new RuntimeException("The requesting user cannot create the user. Request User ID: " + requestUserId + ". User ID: " + userId);
			}
		}
	}

	private void validateReadUserPermissions(String requestUserId, UserModel userModel) {
		List<UserModel> userModels = new ArrayList<UserModel>();
		userModels.add(userModel);
		validateReadUserPermissions(requestUserId, userModels);
	}

	private void validateReadUserPermissions(String requestUserId, List<UserModel> userModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (UserModel userModel : userModels) {
			boolean canUpdateUser = false;
			UUID userId = userModel.getUserId();

			// Admins can create users.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canUpdateUser = true;
			}

			// Users can create users with the same ID.
			if (requestUserModel.getUserId() != null &&
				requestUserModel.getUserId().equals(userId)) {
				canUpdateUser = true;
			}

			if (!canUpdateUser) {
				throw new RuntimeException("The requesting user cannot update the user. Request User ID: " + requestUserId + ". User ID: " + userId);
			}
		}
	}

	private void validateUpdateUserPermissions(String requestUserId, List<UserModel> userModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (UserModel userModel : userModels) {
			boolean canUpdateUser = false;
			UUID userId = userModel.getUserId();

			// Admins can create users.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canUpdateUser = true;
			}

			// Users can create users with the same ID.
			if (requestUserModel.getUserId() != null &&
				requestUserModel.getUserId().equals(userId)) {
				canUpdateUser = true;
			}

			if (!canUpdateUser) {
				throw new RuntimeException("The requesting user cannot update the user. Request User ID: " + requestUserId + ". User ID: " + userId);
			}
		}
	}

	private void validateDeleteUserPermissions(String requestUserId, List<UserModel> userModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (UserModel userModel : userModels) {
			boolean canDeleteUser = false;
			UUID userId = userModel.getUserId();

			// Admins can create users.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canDeleteUser = true;
			}

			// Users can create users with the same ID.
			if (requestUserModel.getUserId() != null &&
				requestUserModel.getUserId().equals(userId)) {
				canDeleteUser = true;
			}

			if (!canDeleteUser) {
				throw new RuntimeException("The requesting user cannot delete the user. Request User ID: " + requestUserId + ". User ID: " + userId);
			}
		}
	}
}
