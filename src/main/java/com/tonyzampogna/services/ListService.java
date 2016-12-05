package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class contains the methods for operating on ListModels.
 */
public class ListService {
	private static final Logger log = LoggerFactory.getLogger(ListService.class);

	// Prepared Statements
	private static PreparedStatement PS_CREATE_LIST = null;
	private static PreparedStatement PS_CREATE_USER_LIST = null;
	private static PreparedStatement PS_GET_LIST_BY_LISTID = null;
	private static PreparedStatement PS_GET_LISTS_BY_USERID = null;
	private static PreparedStatement PS_GET_LISTS_BY_USERNAME = null;
	private static PreparedStatement PS_UPDATE_LIST_BY_LISTID = null;
	private static PreparedStatement PS_DELETE_LIST_BY_LISTID = null;

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	/**
	 * Create
	 */
	public ListModel createList(ListModel listModel) {
		UUID listId = listModel.getListId();
		Session session = listsDatabaseSessionFactory.getSession();

		// Create a new user ID, if necessary.
		if (StringUtils.isEmpty(listId)) {
			listId = UUID.randomUUID();
			listModel.setListId(listId);
		}

		log.info("Creating list in the database. List ID: " + listId);

		// Make sure our logging fields are not empty.
		if (StringUtils.isEmpty(listModel.getCreateUser()) ||
			StringUtils.isEmpty(listModel.getCreateDate()) ||
			StringUtils.isEmpty(listModel.getUpdateUser()) ||
			StringUtils.isEmpty(listModel.getUpdateDate())) {
			throw new RuntimeException("The create and update user and timestamp cannot be blank. List: " + listId);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO lists (list_id, list_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:listId, :listName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_CREATE_LIST.bind();
		updateBoundStatement(boundStatement, listModel);
		session.execute(boundStatement);

		return getListById(listId);
	}

	/**
	 * Create (for User)
	 */
	public ListModel createListForUser(UserModel userModel, ListModel listModel) {
		UUID userId = userModel.getUserId();
		UUID listId = listModel.getListId();
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Creating list for user in the database. List ID: " + listId + ". User: " + userId);

		// Make sure our logging fields are not empty.
		if (StringUtils.isEmpty(listModel.getCreateUser()) ||
			StringUtils.isEmpty(listModel.getCreateDate()) ||
			StringUtils.isEmpty(listModel.getUpdateUser()) ||
			StringUtils.isEmpty(listModel.getUpdateDate())) {
			throw new RuntimeException("The create and update user and timestamp cannot be blank. List: " + listId);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_USER_LIST == null) {
			PS_CREATE_USER_LIST = session.prepare(
				"INSERT INTO lists (user_id, list_id) VALUES (:userId, :listId)");
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO user_lists (list_id, list_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:listId, :listName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		// Create User List
		BoundStatement boundStatement1 = PS_CREATE_USER_LIST.bind();
		boundStatement1.setUUID("userId", userId);
		boundStatement1.setUUID("listId", listId);
		batchStatement.add(boundStatement1);
		// Create List
		BoundStatement boundStatement2 = PS_CREATE_LIST.bind();
		updateBoundStatement(boundStatement2, listModel);
		batchStatement.add(boundStatement2);
		session.execute(batchStatement);

		return listModel;
	}

	/**
	 * Read (by listId)
	 */
	public ListModel getListById(UUID listId) {
		ListModel listModel = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading list from the database (by listId). List ID: " + listId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_LIST_BY_LISTID == null) {
			PS_GET_LIST_BY_LISTID = session.prepare(
				"SELECT list_id, list_name, create_user, create_date, update_user, update_date " +
				"FROM lists WHERE list_id = :listId");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_LIST_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		Row row = resultSet.one();
		if (row != null) {
			listModel = transformRowToList(row);
		}

		return listModel;
	}

	/**
	 * Read (by userId)
	 */
	public List<ListModel> getListsByUserId(UUID userId) {
		List<ListModel> listModelList = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading lists from the database for user. User ID: " + userId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_LISTS_BY_USERID == null) {
			PS_GET_LISTS_BY_USERID = session.prepare(
				"SELECT l.list_id, l.list_name, l.create_user, l.create_date, l.update_user, l.update_date " +
				"FROM lists l, user_lists ul " +
				"WHERE ul.user_id = :userId " +
				"AND l.list_id == ul.list_id"
			);
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_LISTS_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);
		boundStatement.setFetchSize(1000);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		for (Row row : resultSet) {
			if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched()) {
				resultSet.fetchMoreResults();
			}

			if (row != null) {
				ListModel listModel = transformRowToList(row);
				listModelList.add(listModel);
			}
		}

		return listModelList;
	}

	/**
	 * Read (by username)
	 */
	public List<ListModel> getListsByUsername(String username) {
		List<ListModel> listModelList = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading lists from the database for user. Username: " + username);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_LISTS_BY_USERNAME == null) {
			PS_GET_LISTS_BY_USERNAME = session.prepare(
				"SELECT l.list_id, l.list_name, l.create_user, l.create_date, l.update_user, l.update_date " +
				"FROM users u, lists l, user_lists ul " +
				"WHERE u.username = :username " +
				"AND u.user_id == ul.user_id " +
				"AND l.list_id == ul.list_id"
			);
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_LISTS_BY_USERNAME.bind();
		boundStatement.setString("username", username);
		boundStatement.setFetchSize(1000);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		for (Row row : resultSet) {
			if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched()) {
				resultSet.fetchMoreResults();
			}

			if (row != null) {
				ListModel listModel = transformRowToList(row);
				listModelList.add(listModel);
			}
		}

		return listModelList;
	}

	/**
	 * Update
	 */
	public ListModel updateList(ListModel listModel) {
		UUID listId = listModel.getListId();
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Updating list in the database. List ID: " + listId);

		// Make sure our logging fields are not empty.
		if (StringUtils.isEmpty(listModel.getUpdateUser()) ||
			StringUtils.isEmpty(listModel.getUpdateDate())) {
			throw new RuntimeException("The update user and timestamp cannot be blank. List ID: " + listId);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_LIST_BY_LISTID == null) {
			PS_UPDATE_LIST_BY_LISTID = session.prepare(
				"UPDATE lists SET " +
				"list_id = :listId, " +
				"list_name = :listName, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE list_id = :listId");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_UPDATE_LIST_BY_LISTID.bind();
		updateBoundStatement(boundStatement, listModel);
		session.execute(boundStatement);

		return listModel;
	}

	/**
	 * Delete
	 */
	public ListModel deleteList(ListModel listModel) {
		UUID listId = listModel.getListId();
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Deleting list credentials in the database. List ID: " + listId);

		// Create the PreparedStatement if it does not exist.
		if (PS_DELETE_LIST_BY_LISTID == null) {
			PS_DELETE_LIST_BY_LISTID = session.prepare(
				"DELETE FROM lists WHERE list_id = :listId");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_DELETE_LIST_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);
		session.execute(boundStatement);

		return null;
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private void updateBoundStatement(BoundStatement boundStatement, ListModel listModel) {
		boundStatement.setUUID("listId", listModel.getListId());
		boundStatement.setString("listName", listModel.getListName());
		boundStatement.setUUID("createUser", listModel.getCreateUser());
		boundStatement.setTimestamp("createDate", listModel.getCreateDate());
		boundStatement.setUUID("updateUser", listModel.getUpdateUser());
		boundStatement.setTimestamp("updateDate", listModel.getUpdateDate());
	}

	private ListModel transformRowToList(Row row) {
		if (row == null) {
			return null;
		}

		ListModel listModel = new ListModel();
		listModel.setListId(row.getUUID("list_id"));
		listModel.setListName(row.getString("list_name"));
		listModel.setCreateUser(row.getUUID("create_user"));
		listModel.setCreateDate(row.getTimestamp("create_date"));
		listModel.setUpdateUser(row.getUUID("update_user"));
		listModel.setUpdateDate(row.getTimestamp("update_date"));

		return listModel;
	}

	/**
	 * We store the UUIDs of the ItemModels on the ListModel.
	 * This function reads the items.
	 */
	private List<UUID> getItemSortOrder(ListModel listModel) {
		List<UUID> itemSortOrder = null;

		if (listModel.getItemModels() != null) {
			itemSortOrder = new ArrayList<UUID>();

			for (ItemModel itemModel : listModel.getItemModels()) {
				itemSortOrder.add(itemModel.getItemId());
			}
		}

		return itemSortOrder;
	}

	/**
	 * Used when reading the items from the database.
	 * We need to sort the unordered ItemModel list we read
	 * from the database with the list of UUIDs we stored
	 * on the ListModel.
	 *
	 * @return The sorted list of ItemModels.
	 */
	private List<ItemModel> getSortedItemModels(List<UUID> itemSortOrder, List<ItemModel> itemModels) {
		List<ItemModel> sortedItemModels = null;

		if (itemModels != null) {
			sortedItemModels = new ArrayList<ItemModel>();

			// Copy the item models, since we are going to modify it.
			List<ItemModel> clonedItemModels = new ArrayList<ItemModel>();
			for (ItemModel itemModel : itemModels) {
				clonedItemModels.add(itemModel);
			}

			for (UUID itemId : itemSortOrder) {
				for (int count = 0; count < clonedItemModels.size(); count++) {
					ItemModel itemModel = clonedItemModels.remove(count);
					if (itemId.equals(itemModel.getItemId())) {
						// We've matched the ID, so add it to the
						// list. Break out of the for loop and
						// move to the next Item UUID.
						sortedItemModels.add(itemModel);
						break;
					}
					else {
						// Add it back to the list so we can
						// check it again on the next iteration.
						clonedItemModels.add(itemModel);
					}
				}
			}
		}

		return sortedItemModels;
	}
}
