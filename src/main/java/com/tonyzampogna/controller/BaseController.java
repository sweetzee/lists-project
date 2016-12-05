package com.tonyzampogna.controller;

import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.services.ItemService;
import com.tonyzampogna.services.ListService;
import com.tonyzampogna.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class BaseController {
	private static final Logger log = LoggerFactory.getLogger(BaseController.class);

	@Autowired
	private UserService userService;
	private ListService listService;
	private ItemService itemService;


	/////////////////////////////////////////////////
	// Controller Methods
	/////////////////////////////////////////////////

	/**
	 * This function returns the UserModel from the database.
	 *
	 * The function uses a regular expression to determine
	 * if the argument passed in is the userId or the username.
	 *
	 * @Param userIdOrUsername: Either a user ID (UUID) or a username
	 */
	protected UserModel getUserModel(String userIdOrUsername) {
		UserModel userModel = null;

		// Check to see if it is a UUID or a username.
		if (isUUID(userIdOrUsername)) {
			userModel = userService.getUserById(UUID.fromString(userIdOrUsername));
		}
		else {
			userModel = userService.getUserByUsername(userIdOrUsername);
		}

		return userModel;
	}

	/**
	 * This function returns the ListModel from the database.
	 *
	 * @Param listId: List ID for the list.
	 */
	protected ListModel getListModel(String listId) {
		return listService.getListById(UUID.fromString(listId));
	}

	/**
	 * This function returns the ItemModel from the database.
	 *
	 * @Param itemId: Item ID for the list.
	 */
	protected ItemModel getItemModel(String itemId) {
		return itemService.getItemById(UUID.fromString(itemId));
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	protected boolean isUUID(String value) {
		boolean isUUID = false;

		if (!StringUtils.isEmpty(value)) {
			Pattern pattern = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
			Matcher matcher = pattern.matcher(value);
			if (matcher.matches()) {
				isUUID = true;
			}
		}

		return isUUID;
	}

}
