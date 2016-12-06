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
			value = "/lists",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> createLists(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<ListModel> listModelList) {

		if ("UPDATE".equals(action)) {
			listModelList = handleUpdateListsRequest(createUserId, listModelList);
		}
		else if ("DELETE".equals(action)) {
			listModelList = handleDeleteListsRequest(createUserId, listModelList);
		}
		else {
			listModelList = handleCreateListsRequest(createUserId, listModelList);
		}

		return listModelList;
	}

	/**
	 * Create a new lists for a user.
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> createListsForUser(
			@RequestParam(name = "userId") String createUserId,
			@PathVariable(name = "userIdOrUsername") String userIdOrUsername,
			@RequestBody List<ListModel> listModelList) {

		return handleCreateListsForUserRequest(createUserId, userIdOrUsername, listModelList);
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
			value = "/lists",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> updateLists(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<ListModel> listModelList) {

		return handleUpdateListsRequest(updateUserId, listModelList);
	}

	/**
	 * Delete list by ID
	 */
	@RequestMapping(
			value = "/lists",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<ListModel> deleteLists(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<ListModel> listModelList) {

		return handleDeleteListsRequest(deleteUserId, listModelList);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private List<ListModel> handleCreateListsRequest(String createUserId, List<ListModel> listModelList) {
		log.info("A request has come in to create a list. Request User ID: " + createUserId);

		// Set the create and update fields.
		for (ListModel listModel : listModelList) {
			listModel.setCreateUser(UUID.fromString(createUserId));
			listModel.setCreateDate(new Date());
			listModel.setUpdateUser(UUID.fromString(createUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModelList = listService.createLists(listModelList);

		return listModelList;
	}

	private List<ListModel> handleCreateListsForUserRequest(String createUserId, String userIdOrUsername, List<ListModel> listModelList) {
		log.info("A request has come in to create a list for a user. Request User ID: " + createUserId + ". For User: " + userIdOrUsername);

		UserModel userModel = getUserModel(userIdOrUsername);
		if (userModel == null) {
			return null;
		}

		// Set the create and update fields.
		for (ListModel listModel : listModelList) {
			listModel.setCreateUser(UUID.fromString(createUserId));
			listModel.setCreateDate(new Date());
			listModel.setUpdateUser(UUID.fromString(createUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModelList = listService.createListsForUser(userModel, listModelList);

		return listModelList;
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

	private List<ListModel> handleUpdateListsRequest(String updateUserId, List<ListModel> listModelList) {
		log.info("A request has come in to update a list. Request User ID: " + updateUserId);

		// Set the update fields.
		for (ListModel listModel : listModelList) {
			listModel.setUpdateUser(UUID.fromString(updateUserId));
			listModel.setUpdateDate(new Date());
		}

		// Service call
		listModelList = listService.updateLists(listModelList);

		return listModelList;
	}

	private List<ListModel> handleDeleteListsRequest(String deleteUserId, List<ListModel> listModelList) {
		log.info("A request has come in to delete a list. Request User ID: " + deleteUserId);

		// Service call
		listModelList = listService.deleteLists(listModelList);

		return listModelList;
	}
}
