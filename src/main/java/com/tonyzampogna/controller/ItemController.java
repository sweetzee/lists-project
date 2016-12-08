package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.domain.UserListModel;
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
public class ItemController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(ItemController.class);


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * Create Items (Update, Delete as well)
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> createItems(
			@RequestParam(name = "userId") String createUserId,
			@RequestParam(name = "action", required = false) String action,
			@RequestBody List<ItemModel> itemModels) {

		if ("UPDATE".equals(action)) {
			itemModels = handleUpdateItemsRequest(createUserId, itemModels);
		}
		else if ("DELETE".equals(action)) {
			itemModels = handleDeleteItemsRequest(createUserId, itemModels);
		}
		else {
			itemModels = handleCreateItemsRequest(createUserId, itemModels);
		}

		return itemModels;
	}

	/**
	 * Get Item (by item ID)
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
	 * Get Items (by item ID)
	 */
	@RequestMapping(
			value = "/list/{listId}/items",
			method = RequestMethod.GET,
			produces = "application/json")
	public List<ItemModel> getItemsForList(
			@RequestParam(name = "userId") String readUserId,
			@PathVariable(name = "listId") String listId) {

		return handleGetItemsForListRequest(readUserId, listId);
	}

	/**
	 * Update Items
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> updateItems(
			@RequestParam(name = "userId") String updateUserId,
			@RequestBody List<ItemModel> itemModels) {

		return handleUpdateItemsRequest(updateUserId, itemModels);
	}

	/**
	 * Delete Items
	 */
	@RequestMapping(
			value = "/items",
			method = RequestMethod.DELETE,
			consumes = "application/json",
			produces = "application/json")
	public List<ItemModel> deleteItems(
			@RequestParam(name = "userId") String deleteUserId,
			@RequestBody List<ItemModel> itemModels) {

		return handleDeleteItemsRequest(deleteUserId, itemModels);
	}


	/////////////////////////////////////////////////
	// Handler Methods
	/////////////////////////////////////////////////

	private List<ItemModel> handleCreateItemsRequest(String createUserId, List<ItemModel> itemModels) {
		log.info("A request has come in to create a list. Request User ID: " + createUserId);

		// Set the create and update fields.
		for (ItemModel itemModel : itemModels) {
			itemModel.setCreateUser(UUID.fromString(createUserId));
			itemModel.setCreateDate(new Date());
			itemModel.setUpdateUser(UUID.fromString(createUserId));
			itemModel.setUpdateDate(new Date());
		}

		// Service call
		itemModels = itemService.createItems(itemModels);

		return itemModels;
	}

	private ItemModel handleGetItemRequest(String readUserId, String itemId) {
		log.info("A request has come in to read an item. Request User ID: " + readUserId);

		return getItemModel(itemId);
	}

	private List<ItemModel> handleGetItemsForListRequest(String readUserId, String listId) {
		List<ItemModel> itemModels = null;

		log.info("A request has come in to read items for a list. Request User ID: " + readUserId + ". For List ID: " + listId);

		itemModels = itemService.getItemsByListId(UUID.fromString(listId));

		return itemModels;
	}

	private List<ItemModel> handleUpdateItemsRequest(String updateUserId, List<ItemModel> itemModels) {
		log.info("A request has come in to update a list. Request User ID: " + updateUserId);

		// Set the update fields.
		for (ItemModel itemModel : itemModels) {
			itemModel.setUpdateUser(UUID.fromString(updateUserId));
			itemModel.setUpdateDate(new Date());
		}

		// Service call
		itemModels = itemService.updateItems(itemModels);

		return itemModels;
	}

	private List<ItemModel> handleDeleteItemsRequest(String deleteUserId, List<ItemModel> itemModels) {
		log.info("A request has come in to delete items. Request User ID: " + deleteUserId);

		// Service call
		itemModels = itemService.deleteItems(itemModels);

		return itemModels;
	}


	/////////////////////////////////////////////////
	// Permission Validation Methods
	/////////////////////////////////////////////////

	private void validateCreateItemPermissions(String requestUserId, List<ItemModel> itemModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (ItemModel itemModel : itemModels) {
			boolean canCreateItem = false;
			UUID userId = requestUserModel.getUserId();
			UUID listId = itemModel.getListId();

			// Admins can create items.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canCreateItem = true;
			}

			// Check the permission on the list.
			UserListModel userListModel = getUserListModel(userId, listId);
			boolean hasWritePermission = listService.hasWritePermission(userId, userListModel);
			if (hasWritePermission) {
				canCreateItem = true;
			}

			if (!canCreateItem) {
				throw new RuntimeException("The requesting user cannot create the item. Request User ID: " + requestUserId + ". Item ID: " + itemModel.getItemId());
			}
		}
	}

	private void validateReadItemPermissions(String requestUserId, List<ItemModel> itemModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (ItemModel itemModel : itemModels) {
			boolean canReadItem = false;
			UUID userId = requestUserModel.getUserId();
			UUID listId = itemModel.getListId();

			// Admins can create lists.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canReadItem = true;
			}

			// Check the permission on the list.
			UserListModel userListModel = getUserListModel(userId, listId);
			boolean hasReadPermission = listService.hasReadPermission(userId, userListModel);
			if (hasReadPermission) {
				canReadItem = true;
			}

			if (!canReadItem) {
				throw new RuntimeException("The requesting user cannot read the item. Request User ID: " + requestUserId + ". Item ID: " + itemModel.getItemId());
			}
		}
	}

	private void validateUpdateItemPermissions(String requestUserId, List<ItemModel> itemModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (ItemModel itemModel : itemModels) {
			boolean canUpdateItem = false;
			UUID userId = requestUserModel.getUserId();
			UUID listId = itemModel.getListId();

			// Admins can create lists.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canUpdateItem = true;
			}

			// Check the permission on the list.
			UserListModel userListModel = getUserListModel(userId, listId);
			boolean hasWritePermission = listService.hasWritePermission(userId, userListModel);
			if (hasWritePermission) {
				canUpdateItem = true;
			}

			if (!canUpdateItem) {
				throw new RuntimeException("The requesting user cannot read the item. Request User ID: " + requestUserId + ". Item ID: " + itemModel.getItemId());
			}
		}
	}

	private void validateDeleteItemPermissions(String requestUserId, List<ItemModel> itemModels) {
		UserModel requestUserModel = getUserModel(requestUserId);
		if (requestUserModel == null) {
			throw new RuntimeException("The user requesting the operation does not exist. Request User ID: " + requestUserId);
		}

		for (ItemModel itemModel : itemModels) {
			boolean canDeleteItem = false;
			UUID userId = requestUserModel.getUserId();
			UUID listId = itemModel.getListId();

			// Admins can create lists.
			if (UserModel.AuthorizationLevel.ADMIN.equals(requestUserModel.getAuthorizationLevel())) {
				canDeleteItem = true;
			}

			// Check the permission on the list.
			UserListModel userListModel = getUserListModel(userId, listId);
			boolean hasWritePermission = listService.hasWritePermission(userId, userListModel);
			if (hasWritePermission) {
				canDeleteItem = true;
			}

			if (!canDeleteItem) {
				throw new RuntimeException("The requesting user cannot read the item. Request User ID: " + requestUserId + ". Item ID: " + itemModel.getItemId());
			}
		}
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private UserListModel getUserListModel(UUID userId, UUID listId) {
		List<UserListModel> userListModels = listService.getUserListsByUserId(userId);
		for (UserListModel userListModel : userListModels) {
			if (userListModel.getUserId().equals(userId) &&
				userListModel.getListId().equals(listId)) {
				return userListModel;
			}
		}
		return null;
	}

}
