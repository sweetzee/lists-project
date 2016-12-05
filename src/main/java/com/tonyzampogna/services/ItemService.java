package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class contains the methods for operating on ItemModel.
 */
public class ItemService {
	private static final Logger log = LoggerFactory.getLogger(ItemService.class);

	// Prepared Statements
	private static PreparedStatement PS_CREATE_ITEM = null;
	private static PreparedStatement PS_GET_ITEM_BY_ITEMID = null;
	private static PreparedStatement PS_GET_ITEMS_BY_LISTID = null;
	private static PreparedStatement PS_UPDATE_ITEM_BY_ITEMID = null;
	private static PreparedStatement PS_DELETE_ITEM_BY_ITEMID = null;

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	/**
	 * Create
	 */
	public List<ItemModel> createItemsByItemId(List<ItemModel> itemModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ItemModel...
		for (ItemModel itemModel : itemModelList) {
			UUID itemId = itemModel.getItemId();

			// Create a new user ID, if necessary.
			if (StringUtils.isEmpty(itemId)) {
				itemId = UUID.randomUUID();
				itemModel.setListId(itemId);
			}

			// Generate a log buffer.
			log.info("Creating item in the database. Item ID: " + itemId);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(itemModel.getCreateUser()) ||
				StringUtils.isEmpty(itemModel.getCreateDate()) ||
				StringUtils.isEmpty(itemModel.getUpdateUser()) ||
				StringUtils.isEmpty(itemModel.getUpdateDate())) {
				throw new RuntimeException("The create and update user and timestamp cannot be blank. Item: " + itemId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = null;
		List<BoundStatement> boundStatements = getCreateItemsBoundStatements(itemModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return itemModelList;
	}

	/**
	 * Read (by itemId)
	 */
	public ItemModel getItemById(UUID itemId) {
		ItemModel itemModel = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading item from the database (by itemId). Item ID: " + itemId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_ITEM_BY_ITEMID == null) {
			PS_GET_ITEM_BY_ITEMID = session.prepare(
				"SELECT item_id, list_id, item_name, create_user, create_date, update_user, update_date " +
				"FROM items WHERE item_id = :itemId");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_ITEM_BY_ITEMID.bind();
		boundStatement.setUUID("itemId", itemId);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		Row row = resultSet.one();
		if (row != null) {
			itemModel = transformRowToItem(row);
		}

		return itemModel;
	}

	/**
	 * Read (by listId)
	 */
	public List<ItemModel> getItemsByListId(UUID listId) {
		List<ItemModel> itemModelList = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading items from the database for list. List ID: " + listId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_ITEMS_BY_LISTID == null) {
			PS_GET_ITEMS_BY_LISTID = session.prepare(
				"SELECT item_id, list_id, item_name, create_user, create_date, update_user, update_date " +
				"FROM items WHERE list_id = :listId"
			);
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_ITEMS_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);
		boundStatement.setFetchSize(1000);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		for (Row row : resultSet) {
			if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched()) {
				resultSet.fetchMoreResults();
			}

			if (row != null) {
				ItemModel itemModel = transformRowToItem(row);
				itemModelList.add(itemModel);
			}
		}

		return itemModelList;
	}

	/**
	 * Update
	 */
	public List<ItemModel> updateItemsByItemId(List<ItemModel> itemModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ItemModel...
		for (ItemModel itemModel : itemModelList) {
			UUID itemId = itemModel.getItemId();

			// Create a new user ID, if necessary.
			if (StringUtils.isEmpty(itemId)) {
				itemId = UUID.randomUUID();
				itemModel.setListId(itemId);
			}

			// Generate a log buffer.
			log.info("Updating item in the database. Item ID: " + itemId);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(itemModel.getCreateUser()) ||
				StringUtils.isEmpty(itemModel.getCreateDate()) ||
				StringUtils.isEmpty(itemModel.getUpdateUser()) ||
				StringUtils.isEmpty(itemModel.getUpdateDate())) {
				throw new RuntimeException("The create and update user and timestamp cannot be blank. Item: " + itemId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = null;
		List<BoundStatement> boundStatements = getUpdateItemsBoundStatements(itemModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return itemModelList;
	}

	/**
	 * Delete
	 */
	public List<ItemModel> deleteItemsByItemId(List<ItemModel> itemModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ItemModel...
		for (ItemModel itemModel : itemModelList) {
			UUID itemId = itemModel.getItemId();

			// Create a new user ID, if necessary.
			if (StringUtils.isEmpty(itemId)) {
				itemId = UUID.randomUUID();
				itemModel.setListId(itemId);
			}

			// Generate a log buffer.
			log.info("Deleting item in the database. Item ID: " + itemId);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(itemModel.getCreateUser()) ||
				StringUtils.isEmpty(itemModel.getCreateDate()) ||
				StringUtils.isEmpty(itemModel.getUpdateUser()) ||
				StringUtils.isEmpty(itemModel.getUpdateDate())) {
				throw new RuntimeException("The create and update user and timestamp cannot be blank. Item: " + itemId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = null;
		List<BoundStatement> boundStatements = getDeleteItemsBoundStatements(itemModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return itemModelList;
	}


	/////////////////////////////////////////////////
	// Bound Statement Methods
	/////////////////////////////////////////////////

	/**
	 * Return the bound statements to create a list of items.
	 */
	public List<BoundStatement> getCreateItemsBoundStatements(List<ItemModel> itemModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_ITEM == null) {
			PS_CREATE_ITEM = session.prepare(
				"INSERT INTO items (item_id, list_id, item_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:itemId, :listId, :itemName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		if (itemModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (ItemModel itemModel : itemModelList) {
				BoundStatement boundStatement = PS_CREATE_ITEM.bind();
				updateBoundStatement(boundStatement, itemModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to read a list of items.
	 */
	public List<BoundStatement> getReadItemsBoundStatements(List<ItemModel> itemModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_ITEM == null) {
			PS_CREATE_ITEM = session.prepare(
				"INSERT INTO items (item_id, list_id, item_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:itemId, :listId, :itemName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		if (itemModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (ItemModel itemModel : itemModelList) {
				BoundStatement boundStatement = PS_CREATE_ITEM.bind();
				updateBoundStatement(boundStatement, itemModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to update a list of items.
	 */
	public List<BoundStatement> getUpdateItemsBoundStatements(List<ItemModel> itemModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_ITEM_BY_ITEMID == null) {
			PS_UPDATE_ITEM_BY_ITEMID = session.prepare(
				"UPDATE items SET " +
				"item_id = :itemId, " +
				"list_id = :listId, " +
				"item_name = :listName, " +
				"create_user = :createUser, " +
				"create_date = :createDate, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE item_id = :itemId");
		}

		if (itemModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (ItemModel itemModel : itemModelList) {
				BoundStatement boundStatement = PS_UPDATE_ITEM_BY_ITEMID.bind();
				updateBoundStatement(boundStatement, itemModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to delete a list of items.
	 */
	public List<BoundStatement> getDeleteItemsBoundStatements(List<ItemModel> itemModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_DELETE_ITEM_BY_ITEMID == null) {
			PS_DELETE_ITEM_BY_ITEMID = session.prepare(
				"DELETE FROM items WHERE item_id = :itemId");
		}

		if (itemModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (ItemModel itemModel : itemModelList) {
				BoundStatement boundStatement = PS_DELETE_ITEM_BY_ITEMID.bind();
				updateBoundStatement(boundStatement, itemModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private void updateBoundStatement(BoundStatement boundStatement, ItemModel itemModel) {
		boundStatement.setUUID("itemId", itemModel.getItemId());
		boundStatement.setUUID("listId", itemModel.getListId());
		boundStatement.setString("itemName", itemModel.getItemName());
		boundStatement.setUUID("createUser", itemModel.getCreateUser());
		boundStatement.setTimestamp("createDate", itemModel.getCreateDate());
		boundStatement.setUUID("updateUser", itemModel.getUpdateUser());
		boundStatement.setTimestamp("updateDate", itemModel.getUpdateDate());
	}

	private ItemModel transformRowToItem(Row row) {
		if (row == null) {
			return null;
		}

		ItemModel itemModel = new ItemModel();
		itemModel.setItemId(row.getUUID("item_id"));
		itemModel.setListId(row.getUUID("user_id"));
		itemModel.setItemName(row.getString("item_name"));
		itemModel.setCreateUser(row.getUUID("create_user"));
		itemModel.setCreateDate(row.getTimestamp("create_date"));
		itemModel.setUpdateUser(row.getUUID("update_user"));
		itemModel.setUpdateDate(row.getTimestamp("update_date"));

		return itemModel;
	}
}
