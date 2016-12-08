package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

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
public class ListController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create Lists (Update, Delete as well)
	 */
	@RequestMapping(
			value = "/lists",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> createLists(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<ListModel> listModels) {

		if ("UPDATE".equals(action)) {
			listModels = handleUpdateListsRequest(createUserId, listModels);
		}
		else if ("DELETE".equals(action)) {
			listModels = handleDeleteListsRequest(createUserId, listModels);
		}
		else {
			listModels = handleCreateListsRequest(createUserId, listModels);
		}

		return listModels;
	}

	/**
	 * Create Lists (Update, Delete as well)
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> createListsForUser(
			@RequestParam(name = "userId") String createUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<ListModel> listModels) {

		if ("UPDATE".equals(action)) {
			listModels = handleUpdateListsForUserRequest(createUserId, userIdOrUsername, listModels);
		}
		else if ("DELETE".equals(action)) {
			listModels = handleDeleteListsForUserRequest(createUserId, userIdOrUsername, listModels);
		}
		else {
			listModels = handleCreateListsForUserRequest(createUserId, userIdOrUsername, listModels);
		}

		return listModels;
	}

	/**
	 * Get List (by list ID)
	 */
	@RequestMapping(
			value = "/list/{listId}",
			method = RequestMethod.GET,
			produces = "application/json")
	public ListModel getList(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "listId") String listId) {

		return handleGetListRequest(readUserId, listId);
	}

	/**
	 * Get Lists (by user ID or by username)
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.GET,
			produces = "application/json")
	public List<ListModel> getListsForUser(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername) {

		return handleGetListsForUserRequest(readUserId, userIdOrUsername);
	}

	/**
	 * Update Lists
	 */
	@RequestMapping(
			value = "/lists",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> updateLists(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<ListModel> listModels) {

		return handleUpdateListsRequest(updateUserId, listModels);
	}

	/**
	 * Update Lists
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> updateListsForUser(
			@RequestParam(name = "userId") String updateUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody List<ListModel> listModels) {

		return handleUpdateListsForUserRequest(updateUserId, userIdOrUsername, listModels);
	}

	/**
	 * Delete Lists
	 */
	@RequestMapping(
			value = "/lists",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> deleteLists(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<ListModel> listModels) {

		return handleDeleteListsRequest(deleteUserId, listModels);
	}

	/**
	 * Delete Lists
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> deleteListsForUser(
			@RequestParam(name = "userId") String deleteUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody List<ListModel> listModels) {

		return handleDeleteListsForUserRequest(deleteUserId, userIdOrUsername, listModels);
	}


	/////////////////////////////////////////////////
	// Handler Methods
	/////////////////////////////////////////////////

	private List<ListModel> handleCreateListsRequest(String createUserId, List<ListModel> listModels) {
		return handleCreateListsForUserRequest(createUserId, createUserId, listModels);
	}

	private List<ListModel> handleCreateListsForUserRequest(String createUserId, String userIdOrUsername, List<ListModel> listModels) {
		log.info("A request has come in to create lists for a user. Request User ID: " + createUserId + ". For User: " + userIdOrUsername);

		// Get the UserModel for the user attached to the list.
		UserModel listUserModel = getUserModel(userIdOrUsername);

		// Validate the permission for the user.
		validateCreateListPermissions(createUserId, listUserModel);

		// Set the create and update fields.
		for (ListModel listModel : listModels) {
			listModel.setCreateUser(UUID.fromString(createUserId));
			listModel.setCreateDate(new Date());
			listModel.setUpdateUser(UUID.fromString(createUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModels = listService.createLists(listUserModel, listModels);

		return listModels;
	}

	private ListModel handleGetListRequest(String readUserId, String listId) {
		log.info("A request has come in to read a list. Request User ID: " + readUserId);

		UserModel userModel = getUserModel(readUserId);

		// Validate the permission for the user.
		validateReadListPermissions(readUserId, userModel);

		return getListModel(listId);
	}

	private List<ListModel> handleGetListsForUserRequest(String readUserId, String userIdOrUsername) {
		List<ListModel> listModels = null;

		log.info("A request has come in to read lists for a user. Request User ID: " + readUserId + ". For User: " + userIdOrUsername);

		UUID userId = null;
		if (isUUID(userIdOrUsername)) {
			userId = UUID.fromString(userIdOrUsername);
		}
		else {
			UserModel userModel = userService.getUserByUsername(userIdOrUsername);
			userId = userModel.getUserId();
		}

		listModels = listService.getListsByUserId(userId);

		return listModels;
	}

	private List<ListModel> handleUpdateListsRequest(String updateUserId, List<ListModel> listModels) {
		return handleUpdateListsForUserRequest(updateUserId, updateUserId, listModels);
	}

	private List<ListModel> handleUpdateListsForUserRequest(String updateUserId, String userIdOrUsername, List<ListModel> listModels) {
		log.info("A request has come in to update lists for a user. Request User ID: " + updateUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);

		// Validate the permission for the user.
		validateUpdateListPermissions(updateUserId, userModel);

		// Set the create and update fields.
		for (ListModel listModel : listModels) {
			listModel.setUpdateUser(UUID.fromString(updateUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModels = listService.updateLists(userModel, listModels);

		return listModels;
	}

	private List<ListModel> handleDeleteListsRequest(String deleteUserId, List<ListModel> listModels) {
		return handleDeleteListsForUserRequest(deleteUserId, deleteUserId, listModels);
	}

	private List<ListModel> handleDeleteListsForUserRequest(String deleteUserId, String userIdOrUsername, List<ListModel> listModels) {
		log.info("A request has come in to delete lists for a user. Request User ID: " + deleteUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);

		// Validate the permission for the user.
		validateDeleteListPermissions(deleteUserId, userModel);

		// Service call
		listModels = listService.deleteLists(userModel, listModels);

		return listModels;
	}


	/////////////////////////////////////////////////
	// Permission Validation Methods
	/////////////////////////////////////////////////

	private void validateCreateListPermissions(String requestUserId, UserModel listUserModel) {
		boolean canCreateList = false;

		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		if (listUserModel == null) {
			throw new RuntimeException("There is not a user set for the list. Request User ID: " + requestUserId);
		}

		// Admins can create lists.
		if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
			canCreateList = true;
		}

		// Users can create lists with the same user ID.
		if (requestUserModel.getUserId() != null &&
			requestUserModel.getUserId().equals(listUserModel.getUserId())) {
			canCreateList = true;
		}

		if (!canCreateList) {
			throw new RuntimeException("The requesting user cannot create the list. Request User ID: " + requestUserId + ". List User ID: " + listUserModel.getUserId());
		}
	}

	private void validateReadListPermissions(String requestUserId, UserModel listUserModel) {
		boolean canReadList = false;

		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		if (listUserModel == null) {
			throw new RuntimeException("There is not a user set for the list. Request User ID: " + requestUserId);
		}

		// Admins can create lists.
		if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
			canReadList = true;
		}

		// Users can create lists with the same user ID.
		if (requestUserModel.getUserId() != null &&
			requestUserModel.getUserId().equals(listUserModel.getUserId())) {
			canReadList = true;
		}

		if (!canReadList) {
			throw new RuntimeException("The requesting user cannot read the list. Request User ID: " + requestUserId + ". List User ID: " + listUserModel.getUserId());
		}
	}

	private void validateUpdateListPermissions(String requestUserId, UserModel listUserModel) {
		boolean canUpdateList = false;

		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		if (listUserModel == null) {
			throw new RuntimeException("There is not a user set for the list. Request User ID: " + requestUserId);
		}

		// Admins can create lists.
		if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
			canUpdateList = true;
		}

		// Users can create lists with the same user ID.
		if (requestUserModel.getUserId() != null &&
			requestUserModel.getUserId().equals(listUserModel.getUserId())) {
			canUpdateList = true;
		}

		if (!canUpdateList) {
			throw new RuntimeException("The requesting user cannot create the list. Request User ID: " + requestUserId + ". List User ID: " + listUserModel.getUserId());
		}
	}

	private void validateDeleteListPermissions(String requestUserId, UserModel listUserModel) {
		boolean canDeleteList = false;

		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		if (listUserModel == null) {
			throw new RuntimeException("There is not a user set for the list. Request User ID: " + requestUserId);
		}

		// Admins can create lists.
		if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
			canDeleteList = true;
		}

		// Users can create lists with the same user ID.
		if (requestUserModel.getUserId() != null &&
			requestUserModel.getUserId().equals(listUserModel.getUserId())) {
			canDeleteList = true;
		}

		if (!canDeleteList) {
			throw new RuntimeException("The requesting user cannot create the list. Request User ID: " + requestUserId + ". List User ID: " + listUserModel.getUserId());
		}
	}
}
