package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	// Create Methods
	/////////////////////////////////////////////////

	public void createItem(ItemModel itemModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_ITEM == null) {
			PS_CREATE_ITEM = session.prepare(
				"INSERT INTO items (item_id, list_id, item_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:itemId, :listId, :itemName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		BoundStatement boundStatement = PS_CREATE_ITEM.bind();
		updateBoundStatement(boundStatement, itemModel);

		session.execute(boundStatement);
	}


	/////////////////////////////////////////////////
	// Read Methods
	/////////////////////////////////////////////////

	public ItemModel getItemById(UUID itemId) {
		ItemModel itemModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_ITEM_BY_ITEMID == null) {
			PS_GET_ITEM_BY_ITEMID = session.prepare(
				"SELECT item_id, list_id, item_name, create_user, create_date, update_user, update_date " +
				"FROM items WHERE item_id = :itemId");
		}

		BoundStatement boundStatement = PS_GET_ITEM_BY_ITEMID.bind();
		boundStatement.setUUID("itemId", itemId);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			itemModel = transformRowToItem(row);
		}

		return itemModel;
	}

	public List<ItemModel> getItemsByListId(UUID listId) {
		List<ItemModel> itemModelList = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_ITEMS_BY_LISTID == null) {
			PS_GET_ITEMS_BY_LISTID = session.prepare(
				"SELECT item_id, list_id, item_name, create_user, create_date, update_user, update_date " +
				"FROM items WHERE list_id = :listId"
			);
		}

		BoundStatement boundStatement = PS_GET_ITEMS_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);
		boundStatement.setFetchSize(1000);

		ResultSet resultSet = session.execute(boundStatement);
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


	/////////////////////////////////////////////////
	// Update Methods
	/////////////////////////////////////////////////

	public void updateItemByItemId(ItemModel itemModel) {
		List<ItemModel> itemModelList = new ArrayList<ItemModel>();
		itemModelList.add(itemModel);
		updateItemsByItemId(itemModelList);
	}

	public void updateItemsByItemId(List<ItemModel> itemModelList) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
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
				"WHERE list_id = :listId");
		}

		BatchStatement batchStatement = null;
		BoundStatement boundStatement = null;
		for (ItemModel itemModel : itemModelList) {
			boundStatement = PS_UPDATE_ITEM_BY_ITEMID.bind();
			updateBoundStatement(boundStatement, itemModel);
			batchStatement.add(boundStatement);
		}

		session.execute(batchStatement);
	}


	/////////////////////////////////////////////////
	// Delete Methods
	/////////////////////////////////////////////////

	public void deleteItemByItemId(UUID itemId) {
		List<ItemModel> itemModelList = new ArrayList<ItemModel>();
		ItemModel itemModel = new ItemModel();
		itemModel.setItemId(itemId);
		itemModelList.add(itemModel);
		deleteItemsByItemId(itemModelList);
	}

	public void deleteItemsByItemId(List<ItemModel> itemModelList) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_DELETE_ITEM_BY_ITEMID == null) {
			PS_DELETE_ITEM_BY_ITEMID = session.prepare(
				"DELETE FROM items WHERE item_id = :itemId");
		}

		BatchStatement batchStatement = null;
		BoundStatement boundStatement = null;
		for (ItemModel itemModel : itemModelList) {
			boundStatement = PS_DELETE_ITEM_BY_ITEMID.bind();
			updateBoundStatement(boundStatement, itemModel);
			batchStatement.add(boundStatement);
		}

		session.execute(batchStatement);
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
