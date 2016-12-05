package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.ListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
public class ListController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private ListService listService;


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create a new list.
	 *
	 * It is possible to update or delete a list by ID as well
	 * using the action parameter.
	 */
	@RequestMapping(
			value = "/list",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public ListModel createList(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody ListModel listModel) {

		if ("UPDATE".equals(action)) {
			listModel = handleUpdateListRequest(createUserId, listModel);
		}
		else if ("DELETE".equals(action)) {
			listModel = handleDeleteListRequest(createUserId, listModel);
		}
		else {
			listModel = handleCreateListRequest(createUserId, listModel);
		}

		return listModel;
	}

	/**
	 * Create a new list for a user.
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/list",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public ListModel createListForUser(
			@RequestParam(name = "userId") String createUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody ListModel listModel) {

		return handleCreateListForUserRequest(createUserId, userIdOrUsername, listModel);
	}

	/**
	 * Get list by list ID.
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
	 * Get lists by user ID or by username.
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
	 * Update list by ID
	 */
	@RequestMapping(
			value = "/list/{listId}",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public ListModel updateList(
			@RequestParam(name = "userId") String updateUserId,
			@PathVariable(name = "listId") String listId,
			@RequestBody ListModel listModel) {

		return handleUpdateListRequest(updateUserId, listModel);
	}

	/**
	 * Delete list by ID
	 */
	@RequestMapping(
			value = "/list/{listId}",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public ListModel deleteList(
			@RequestParam(name = "userId") String deleteUserId,
			@PathVariable(name = "listId") String listId,
			@RequestBody ListModel listModel) {

		return handleDeleteListRequest(deleteUserId, listModel);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private ListModel handleCreateListRequest(String createUserId, ListModel listModel) {
		log.info("A request has come in to create a list. Request User ID: " + createUserId);

		// Change the create and update fields.
		listModel.setCreateUser(UUID.fromString(createUserId));
		listModel.setCreateDate(new Date());
		listModel.setUpdateUser(UUID.fromString(createUserId));
		listModel.setUpdateDate(new Date());

		// Service call
		listModel = listService.createList(listModel);

		return listModel;
	}

	private ListModel handleCreateListForUserRequest(String createUserId, String userIdOrUsername, ListModel listModel) {
		log.info("A request has come in to create a list for a user. Request User ID: " + createUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);
		if (userModel == null) {
			return null;
		}

		// Change the create and update fields.
		listModel.setCreateUser(UUID.fromString(createUserId));
		listModel.setCreateDate(new Date());
		listModel.setUpdateUser(UUID.fromString(createUserId));
		listModel.setUpdateDate(new Date());

		// Service call
		listModel = listService.createListForUser(userModel, listModel);

		return listModel;
	}

	private ListModel handleGetListRequest(String readUserId, String listId) {
		log.info("A request has come in to read a list. Request User ID: " + readUserId);

		return getListModel(listId);
	}

	private List<ListModel> handleGetListsForUserRequest(String readUserId, String userIdOrUsername) {
		List<ListModel> listModelList = null;

		log.info("A request has come in to read lists for a user. Request User ID: " + readUserId + ". For User: " + userIdOrUsername);

		if (isUUID(userIdOrUsername)) {
			listModelList = listService.getListsByUserId(UUID.fromString(userIdOrUsername));
		}
		else {
			listModelList = listService.getListsByUsername(userIdOrUsername);
		}

		return listModelList;
	}

	private ListModel handleUpdateListRequest(String updateUserId, ListModel listModel) {
		log.info("A request has come in to update a list. Request User ID: " + updateUserId);

		// Update the create and update fields.
		listModel.setUpdateUser(UUID.fromString(updateUserId));
		listModel.setUpdateDate(new Date());

		// Service call
		listModel = listService.updateList(listModel);

		return listModel;
	}

	private ListModel handleDeleteListRequest(String deleteUserId, ListModel listModel) {
		log.info("A request has come in to delete a list. Request User ID: " + deleteUserId);

		// Service call
		listModel = listService.deleteList(listModel);

		return listModel;
	}
}
