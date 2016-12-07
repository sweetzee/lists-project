package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.ItemModel;
import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserListModel;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * This class contains the methods for operating on ListModels.
 *
 * The service handles safety checks that pertain to the database.
 * For example, making sure that constraints are met (like NOT NULL),
 * or checking that duplicate data is not getting generated.
 *
 * Whether a record can be created, read, updated, or deleted is
 * handled at the controller level.
 */
@Service
public class ListService {
	private static final Logger log = LoggerFactory.getLogger(ListService.class);

	// Prepared Statements
	private static PreparedStatement PS_CREATE_LIST = null;
	private static PreparedStatement PS_CREATE_USER_LIST = null;
	private static PreparedStatement PS_GET_LIST_BY_LISTID = null;
	private static PreparedStatement PS_GET_LISTS_BY_LISTIDS = null;
	private static PreparedStatement PS_GET_USER_LISTS_BY_USERID = null;
	private static PreparedStatement PS_UPDATE_LIST = null;
	private static PreparedStatement PS_UPDATE_USER_LIST = null;
	private static PreparedStatement PS_DELETE_LIST = null;
	private static PreparedStatement PS_DELETE_USER_LIST = null;

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;

	@Autowired
	private ItemService itemService;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	/**
	 * Create Lists
	 */
	public List<ListModel> createLists(UserModel userModel, List<ListModel> listModels) {
		UUID userId = userModel.getUserId();
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ListModel...
		for (ListModel listModel : listModels) {
			UUID listId = listModel.getListId();

			// Create a new ID, if necessary.
			if (StringUtils.isEmpty(listId)) {
				listId = UUID.randomUUID();
				listModel.setListId(listId);
			}

			// Make sure the authorization level is not null.
			if (StringUtils.isEmpty(listModel.getAuthorizationLevel())) {
				listModel.setAuthorizationLevel(UserListModel.AuthorizationLevel.OWNER);
			}

			// Generate a log buffer.
			log.info("Creating list in the database for user. User ID: " + userId + ". List ID: " + listId);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(listModel.getCreateUser()) ||
				StringUtils.isEmpty(listModel.getCreateDate()) ||
				StringUtils.isEmpty(listModel.getUpdateUser()) ||
				StringUtils.isEmpty(listModel.getUpdateDate())) {
				throw new RuntimeException("The create and update user and timestamp cannot be blank. List ID: " + listId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getCreateListsBoundStatements(userId, listModels);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
		}
		session.execute(batchStatement);

		return listModels;
	}

	/**
	 * Read List (by listId)
	 */
	public ListModel getList(UUID listId) {
		ListModel listModel = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading list from the database (by listId). List ID: " + listId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_LIST_BY_LISTID == null) {
			PS_GET_LIST_BY_LISTID = session.prepare(
				"SELECT list_id, list_name, item_sort_order, create_user, create_date, update_user, update_date " +
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
	 * Read Lists (by userId)
	 */
	public List<ListModel> getListsByUserId(UUID userId) {
		log.info("Reading lists from the database for user. User ID: " + userId);

		// Get a list of all of the lists
		// the user has permission to.
		List<UserListModel> userListModels = getUserListsByUserId(userId);

		// Get the lists using an array of
		// list IDs from the previous query.
		List<ListModel> listModels = getListsForUserLists(userListModels);

		return listModels;
	}

	/**
	 * Read User Lists (by userId)
	 */
	public List<UserListModel> getUserListsByUserId(UUID userId) {
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_USER_LISTS_BY_USERID == null) {
			PS_GET_USER_LISTS_BY_USERID = session.prepare(
				"SELECT user_id, list_id, authorization_level " +
				"FROM user_lists WHERE user_id = :userId"
			);
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_USER_LISTS_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);
		boundStatement.setFetchSize(1000);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		List<UserListModel> userListModels = null;
		for (Row row : resultSet) {
			if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched()) {
				resultSet.fetchMoreResults();
			}

			if (row != null) {
				UserListModel userListModel = new UserListModel();
				userListModel.setUserId(userId);
				userListModel.setListId(row.getUUID("list_id"));

				UserListModel.AuthorizationLevel authorizationLevel = null;
				String authorizationLevelString = row.getString("authorization_level");
				try {
					authorizationLevel = UserListModel.AuthorizationLevel.valueOf(authorizationLevelString);
				}
				catch (Exception e) {
					log.error("Could not create AuthorizationLevel enum from value. Value: " + authorizationLevelString);
					authorizationLevel = UserListModel.AuthorizationLevel.READ_ACCESS;
				}
				userListModel.setAuthorizationLevel(authorizationLevel);

				userListModels.add(userListModel);
			}
		}

		return userListModels;
	}

	/**
	 * Update Lists
	 */
	public List<ListModel> updateLists(UserModel userModel, List<ListModel> listModels) {
		UUID userId = userModel.getUserId();
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ListModel...
		for (ListModel listModel : listModels) {
			UUID listId = listModel.getListId();
			UserListModel userListModel = getUserListByListId(userId, listId);

			// Generate a log buffer.
			log.info("Updating list in the database for user. User ID: " + userId + ". List ID: " + listId);

			// Check the permissions on the list.
			if (!hasWritePermission(userId, userListModel)) {
				throw new RuntimeException("The user does not have write access on the list. User ID: " + userId + ". List ID: " + listId);
			}

			// Make sure the authorization level is not null.
			if (StringUtils.isEmpty(listModel.getAuthorizationLevel())) {
				listModel.setAuthorizationLevel(UserListModel.AuthorizationLevel.READ_ACCESS);
			}

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(listModel.getUpdateUser()) ||
				StringUtils.isEmpty(listModel.getUpdateDate())) {
				throw new RuntimeException("The update user and timestamp cannot be blank. List ID: " + listId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getUpdateListsBoundStatements(userId, listModels);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
		}
		session.execute(batchStatement);

		return listModels;
	}

	/**
	 * Delete Lists
	 */
	public List<ListModel> deleteLists(UserModel userModel, List<ListModel> listModels) {
		UUID userId = userModel.getUserId();
		Session session = listsDatabaseSessionFactory.getSession();

		// For each ListModel...
		for (ListModel listModel : listModels) {
			UUID listId = listModel.getListId();
			UserListModel userListModel = getUserListByListId(userId, listId);

			// Generate a log buffer.
			log.info("Deleting list from the database. List ID: " + listId);

			// Check the permissions on the list.
			if (!hasWritePermission(userId, userListModel)) {
				throw new RuntimeException("The user does not have write access on the list. User ID: " + userId + ". List ID: " + listId);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getDeleteListsBoundStatements(userId, listModels);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
		}
		session.execute(batchStatement);

		return listModels;
	}


	/////////////////////////////////////////////////
	// Permissions
	/////////////////////////////////////////////////

	public boolean isOwner(UUID userId, UserListModel userListModel) {
		boolean isOwner = false;
		if (userListModel.getUserId().equals(userId) &&
			UserListModel.AuthorizationLevel.OWNER.equals(
				userListModel.getAuthorizationLevel())) {
			isOwner = true;
		}
		return isOwner;
	}

	public boolean hasReadPermission(UUID userId, UserListModel userListModel) {
		boolean canRead = false;
		if (userListModel.getUserId().equals(userId) &&
				(UserListModel.AuthorizationLevel.OWNER.equals(
					userListModel.getAuthorizationLevel()) ||
				 UserListModel.AuthorizationLevel.WRITE_ACCESS.equals(
					userListModel.getAuthorizationLevel()) ||
				 UserListModel.AuthorizationLevel.READ_ACCESS.equals(
					userListModel.getAuthorizationLevel()))) {
			canRead = true;
		}
		return canRead;
	}

	public boolean hasWritePermission(UUID userId, UserListModel userListModel) {
		boolean canWrite = false;
		if (userListModel.getUserId().equals(userId) &&
				(UserListModel.AuthorizationLevel.OWNER.equals(
					userListModel.getAuthorizationLevel()) ||
				 UserListModel.AuthorizationLevel.WRITE_ACCESS.equals(
					userListModel.getAuthorizationLevel()))) {
			canWrite = true;
		}
		return canWrite;
	}


	/////////////////////////////////////////////////
	// Bound Statement Methods
	/////////////////////////////////////////////////

	/**
	 * Return the bound statements to create a list of lists.
	 */
	public List<BoundStatement> getCreateListsBoundStatements(UUID userId, List<ListModel> listModels) {
		List<BoundStatement> boundStatements = new ArrayList<BoundStatement>();
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_USER_LIST == null) {
			PS_CREATE_USER_LIST = session.prepare(
				"INSERT INTO user_lists (user_id, list_id, authorization_level) " +
				"VALUES (:userId, :listId, :authorizationLevel)");
		}

		// Create records in the user_lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_CREATE_USER_LIST.bind();
			boundStatement.setUUID("userId", userId);
			boundStatement.setUUID("listId", listModel.getListId());
			boundStatement.setString("authorizationLevel", listModel.getAuthorizationLevel().toString());
			boundStatements.add(boundStatement);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO lists (list_id, list_name, item_sort_order, create_user, create_date, update_user, update_date) " +
				"VALUES (:listId, :listName, :item_sort_order, :createUser, :createDate, :updateUser, :updateDate)");
		}

		// Create records in the lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_CREATE_LIST.bind();
			updateBoundStatement(boundStatement, listModel);
			boundStatements.add(boundStatement);
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to update a list of lists.
	 */
	public List<BoundStatement> getUpdateListsBoundStatements(UUID userId, List<ListModel> listModels) {
		List<BoundStatement> boundStatements = new ArrayList<BoundStatement>();
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_USER_LIST == null) {
			PS_UPDATE_USER_LIST = session.prepare(
				"UPDATE user_lists SET " +
				"user_id = :userId, " +
				"list_id = :listId, " +
				"authorization_level = :authorizationLevel " +
				"WHERE user_id = :userId " +
				"AND list_id = :listId");
		}

		// Update the user_lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_UPDATE_USER_LIST.bind();
			boundStatement.setUUID("userId", userId);
			boundStatement.setUUID("listId", listModel.getListId());
			boundStatement.setString("authorizationLevel", listModel.getAuthorizationLevel().toString());
			boundStatements.add(boundStatement);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_LIST == null) {
			PS_UPDATE_LIST = session.prepare(
				"UPDATE lists SET " +
				"list_name = :listName, " +
				"item_sort_order = :itemSortOrder, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE list_id = :listId");
		}

		// Update the lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_UPDATE_LIST.bind();
			updateBoundStatement(boundStatement, listModel);
			boundStatements.add(boundStatement);
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to delete a list of lists.
	 */
	public List<BoundStatement> getDeleteListsBoundStatements(UUID userId, List<ListModel> listModels) {
		List<BoundStatement> boundStatements = new ArrayList<BoundStatement>();
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_DELETE_USER_LIST == null) {
			PS_DELETE_USER_LIST = session.prepare(
				"DELETE FROM user_lists WHERE user_id = :userId AND list_id = :listId");
		}

		// Delete from the user_lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_DELETE_USER_LIST.bind();
			boundStatement.setUUID("userId", userId);
			boundStatement.setUUID("listId", listModel.getListId());
			boundStatements.add(boundStatement);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_DELETE_LIST == null) {
			PS_DELETE_LIST = session.prepare(
				"DELETE FROM lists WHERE list_id = :listId");
		}

		// Delete from the lists table.
		for (ListModel listModel : listModels) {
			BoundStatement boundStatement = PS_DELETE_LIST.bind();
			updateBoundStatement(boundStatement, listModel);
			boundStatements.add(boundStatement);
		}

		return boundStatements;
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	/**
	 * Takes an array of UserLists and returns the Lists.
	 */
	private List<ListModel> getListsForUserLists(List<UserListModel> userListModels) {
		List<ListModel> listModels = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Get the list IDs from the UserLists.
		List<UUID> listIds = new ArrayList<UUID>();
		Map<UUID, UserListModel> userListModelMap = new HashMap<UUID, UserListModel>();
		for (UserListModel userListModel : userListModels) {
			listIds.add(userListModel.getListId());
			userListModelMap.put(userListModel.getListId(), userListModel);
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_LISTS_BY_LISTIDS == null) {
			PS_GET_LISTS_BY_LISTIDS = session.prepare(
				"SELECT list_id, list_name, item_sort_order, create_user, create_date, update_user, update_date " +
				"FROM lists WHERE list_id IN :listIds"
			);
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_LISTS_BY_LISTIDS.bind();
		boundStatement.setList("listIds", listIds, UUID.class);
		boundStatement.setFetchSize(1000);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		for (Row row : resultSet) {
			if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched()) {
				resultSet.fetchMoreResults();
			}

			if (row != null) {
				ListModel listModel = transformRowToList(row);

				// UserListModel
				UserListModel userListModel = userListModelMap.get(listModel.getListId());
				listModel.setAuthorizationLevel(userListModel.getAuthorizationLevel());

				listModels.add(listModel);
			}
		}

		return listModels;
	}

	private UserListModel getUserListByListId(UUID userId, UUID listId) {
		UserListModel userListModel = null;
		List<UserListModel> userListModels = getUserListsByUserId(userId);
		for (UserListModel model : userListModels) {
			if (listId != null && listId.equals(model.getListId())) {
				userListModel = model;
			}
		}
		return userListModel;
	}

	private void updateBoundStatement(BoundStatement boundStatement, ListModel listModel) {
		boundStatement.setUUID("listId", listModel.getListId());
		boundStatement.setString("listName", listModel.getListName());
		boundStatement.setUUID("createUser", listModel.getCreateUser());
		boundStatement.setTimestamp("createDate", listModel.getCreateDate());
		boundStatement.setUUID("updateUser", listModel.getUpdateUser());
		boundStatement.setTimestamp("updateDate", listModel.getUpdateDate());

		// Update the sort order based on the list of items.
		List<UUID> itemSortOrder = null;
		if (listModel.getItemModels() != null) {
			itemSortOrder = new ArrayList<UUID>();
			for (ItemModel itemModel : listModel.getItemModels()) {
				itemSortOrder.add(itemModel.getItemId());
			}
		}
		listModel.setItemSortOrder(itemSortOrder);
		boundStatement.setList("itemSortOrder", itemSortOrder, UUID.class);
	}

	private ListModel transformRowToList(Row row) {
		if (row == null) {
			return null;
		}

		ListModel listModel = new ListModel();
		listModel.setListId(row.getUUID("list_id"));
		listModel.setListName(row.getString("list_name"));
		listModel.setItemSortOrder(row.getList("item_sort_order", UUID.class));
		listModel.setCreateUser(row.getUUID("create_user"));
		listModel.setCreateDate(row.getTimestamp("create_date"));
		listModel.setUpdateUser(row.getUUID("update_user"));
		listModel.setUpdateDate(row.getTimestamp("update_date"));

		// Get the ItemModels from the database.
		// Sort the item model list after fetching, then
		// set it on the ListModel.
		List<ItemModel> itemModelList = itemService.getItemsByListId(listModel.getListId());
		List<ItemModel> sortedItemModelList = getSortedItemModels(
				listModel.getItemSortOrder(), itemModelList);
		listModel.setItemModels(sortedItemModelList);

		return listModel;
	}

	/**
	 * Whenever the items on the list change, call this
	 * to keep the item sort order up to date.
	 */
	private void updateItemSortOrder(ListModel listModel) {
		List<UUID> itemSortOrder = null;

		if (listModel.getItemModels() != null) {
			itemSortOrder = new ArrayList<UUID>();

			for (ItemModel itemModel : listModel.getItemModels()) {
				itemSortOrder.add(itemModel.getItemId());
			}
		}

		listModel.setItemSortOrder(itemSortOrder);
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
