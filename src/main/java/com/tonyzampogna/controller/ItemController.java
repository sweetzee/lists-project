package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.services.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
public class ItemController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(ItemController.class);

	@Autowired
	private ItemService itemService;


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create new items.
	 *
	 * It is possible to update or delete a list by ID as well
	 * using the action parameter.
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> createItems(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<ItemModel> itemModelList) {

		if ("UPDATE".equals(action)) {
			itemModelList = handleUpdateItemsRequest(createUserId, itemModelList);
		}
		else if ("DELETE".equals(action)) {
			itemModelList = handleDeleteItemsRequest(createUserId, itemModelList);
		}
		else {
			itemModelList = handleCreateItemsRequest(createUserId, itemModelList);
		}

		return itemModelList;
	}

	/**
	 * Get item by item ID.
	 */
	@RequestMapping(
			value = "/item/{itemId}",
			method = RequestMethod.GET,
			produces = "application/json")
	public ItemModel getItem(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "itemId") String itemId) {

		return handleGetItemRequest(readUserId, itemId);
	}

	/**
	 * Get items by list ID.
	 */
	@RequestMapping(
			value = "/user/{userIdOrUsername}/lists",
			method = RequestMethod.GET,
			produces = "application/json")
	public List<ItemModel> getItemsForList(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "listId") String listId) {

		return handleGetItemsForListRequest(readUserId, listId);
	}

	/**
	 * Update items by ID
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> updateItems(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<ItemModel> itemModelList) {

		return handleUpdateItemsRequest(updateUserId, itemModelList);
	}

	/**
	 * Delete items by ID
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> deleteItems(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<ItemModel> itemModelList) {

		return handleDeleteItemsRequest(deleteUserId, itemModelList);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private List<ItemModel> handleCreateItemsRequest(String createUserId, List<ItemModel> itemModelList) {
		log.info("A request has come in to create a list. Request User ID: " + createUserId);

		// Set the create and update fields.
		for (ItemModel itemModel : itemModelList) {
			itemModel.setCreateUser(UUID.fromString(createUserId));
			itemModel.setCreateDate(new Date());
			itemModel.setUpdateUser(UUID.fromString(createUserId));
			itemModel.setUpdateDate(new Date());
		}

		// Service call
		itemModelList = itemService.createItems(itemModelList);

		return itemModelList;
	}

	private ItemModel handleGetItemRequest(String readUserId, String itemId) {
		log.info("A request has come in to read an item. Request User ID: " + readUserId);

		return getItemModel(itemId);
	}

	private List<ItemModel> handleGetItemsForListRequest(String readUserId, String listId) {
		List<ItemModel> itemModelList = null;

		log.info("A request has come in to read items for a list. Request User ID: " + readUserId + ". For List ID: " + listId);

		itemModelList = itemService.getItemsByListId(UUID.fromString(listId));

		return itemModelList;
	}

	private List<ItemModel> handleUpdateItemsRequest(String updateUserId, List<ItemModel> itemModelList) {
		log.info("A request has come in to update a list. Request User ID: " + updateUserId);

		// Set the update fields.
		for (ItemModel itemModel : itemModelList) {
			itemModel.setUpdateUser(UUID.fromString(updateUserId));
			itemModel.setUpdateDate(new Date());
		}

		// Service call
		itemModelList = itemService.updateItems(itemModelList);

		return itemModelList;
	}

	private List<ItemModel> handleDeleteItemsRequest(String deleteUserId, List<ItemModel> itemModelList) {
		log.info("A request has come in to delete items. Request User ID: " + deleteUserId);

		// Service call
		itemModelList = itemService.deleteItems(itemModelList);

		return itemModelList;
	}
}
