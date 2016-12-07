package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;


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
	 * Get List (by list ID).
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
	 * Get Lists (by user ID or by username).
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
	// Helper Methods
	/////////////////////////////////////////////////

	private List<ListModel> handleCreateListsRequest(String createUserId, List<ListModel> listModels) {
		return handleCreateListsForUserRequest(createUserId, createUserId, listModels);
	}

	private List<ListModel> handleCreateListsForUserRequest(String createUserId, String userIdOrUsername, List<ListModel> listModels) {
		log.info("A request has come in to create lists for a user. Request User ID: " + createUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);
		if (userModel == null) {
			return null;
		}

		// Set the create and update fields.
		for (ListModel listModel : listModels) {
			listModel.setCreateUser(UUID.fromString(createUserId));
			listModel.setCreateDate(new Date());
			listModel.setUpdateUser(UUID.fromString(createUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModels = listService.createLists(userModel, listModels);

		return listModels;
	}

	private ListModel handleGetListRequest(String readUserId, String listId) {
		log.info("A request has come in to read a list. Request User ID: " + readUserId);

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

		listModels = listService.getLists(userId);

		return listModels;
	}

	private List<ListModel> handleUpdateListsRequest(String updateUserId, List<ListModel> listModels) {
		return handleUpdateListsForUserRequest(updateUserId, updateUserId, listModels);
	}

	private List<ListModel> handleUpdateListsForUserRequest(String updateUserId, String userIdOrUsername, List<ListModel> listModels) {
		log.info("A request has come in to update lists for a user. Request User ID: " + updateUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);
		if (userModel == null) {
			return null;
		}

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
		if (userModel == null) {
			return null;
		}

		// Service call
		listModels = listService.deleteLists(userModel, listModels);

		return listModels;
	}

}
