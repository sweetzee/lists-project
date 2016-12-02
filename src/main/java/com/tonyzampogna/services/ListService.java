package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.ListModel;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	// Create Methods
	/////////////////////////////////////////////////

	public void createList(ListModel listModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO lists (list_id, list_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:listId, :listName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		BoundStatement boundStatement = PS_CREATE_LIST.bind();
		updateBoundStatement(boundStatement, listModel);

		session.execute(boundStatement);
	}

	public void createListForUser(UserModel userModel, ListModel listModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_USER_LIST == null) {
			PS_CREATE_USER_LIST = session.prepare(
				"INSERT INTO lists (user_id, list_id) VALUES (:userId, :listId)");
		}

		BoundStatement boundStatement1 = PS_CREATE_USER_LIST.bind();
		boundStatement1.setUUID("userId", userModel.getUserId());
		boundStatement1.setUUID("listId", listModel.getListId());

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO user_lists (list_id, list_name, create_user, create_date, update_user, update_date) " +
				"VALUES (:listId, :listName, :createUser, :createDate, :updateUser, :updateDate)");
		}

		BoundStatement boundStatement2 = PS_CREATE_LIST.bind();
		updateBoundStatement(boundStatement2, listModel);

		// Create a batch statement to run both of the inserts.
		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(boundStatement1);
		batchStatement.add(boundStatement2);

		session.execute(batchStatement);
	}


	/////////////////////////////////////////////////
	// Read Methods
	/////////////////////////////////////////////////

	public ListModel getListById(UUID listId) {
		ListModel listModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_LIST_BY_LISTID == null) {
			PS_GET_LIST_BY_LISTID = session.prepare(
				"SELECT list_id, list_name, create_user, create_date, update_user, update_date " +
				"FROM lists WHERE list_id = :listId");
		}

		BoundStatement boundStatement = PS_GET_LIST_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			listModel = transformRowToList(row);
		}

		return listModel;
	}

	public ListModel getUserListsByUserId(UUID userId) {
		ListModel listModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_LISTS_BY_USERID == null) {
			PS_GET_LISTS_BY_USERID = session.prepare(
				"SELECT l.list_id, l.list_name, l.create_user, l.create_date, l.update_user, l.update_date " +
				"FROM lists l, user_lists ul " +
				"WHERE ul.user_id = :userId " +
				"AND l.list_id == ul.list_id"
			);
		}

		BoundStatement boundStatement = PS_GET_LISTS_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			listModel = transformRowToList(row);
		}

		return listModel;
	}

	public ListModel getUserListsByUsername(String username) {
		ListModel listModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_LISTS_BY_USERNAME == null) {
			PS_GET_LISTS_BY_USERNAME = session.prepare(
				"SELECT l.list_id, l.list_name, l.create_user, l.create_date, l.update_user, l.update_date " +
				"FROM users u, lists l, user_lists ul " +
				"WHERE u.username = :username " +
				"AND u.user_id == ul.user_id " +
				"AND l.list_id == ul.list_id"
			);
		}

		BoundStatement boundStatement = PS_GET_LISTS_BY_USERNAME.bind();
		boundStatement.setString("username", username);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			listModel = transformRowToList(row);
		}

		return listModel;
	}


	/////////////////////////////////////////////////
	// Update Methods
	/////////////////////////////////////////////////

	public void updateListByListId(ListModel listModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_UPDATE_LIST_BY_LISTID == null) {
			PS_UPDATE_LIST_BY_LISTID = session.prepare(
				"UPDATE lists SET " +
				"list_id = :listId, " +
				"list_name = :listName, " +
				"create_user = :createUser, " +
				"create_date = :createDate, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE list_id = :listId");
		}

		BoundStatement boundStatement = PS_UPDATE_LIST_BY_LISTID.bind();
		updateBoundStatement(boundStatement, listModel);

		session.execute(boundStatement);
	}


	/////////////////////////////////////////////////
	// Delete Methods
	/////////////////////////////////////////////////

	public void deleteListByListId(UUID listId) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_DELETE_LIST_BY_LISTID == null) {
			PS_DELETE_LIST_BY_LISTID = session.prepare(
				"DELETE FROM lists WHERE list_id = :listId");
		}

		BoundStatement boundStatement = PS_DELETE_LIST_BY_LISTID.bind();
		boundStatement.setUUID("listId", listId);

		session.execute(boundStatement);
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
		listModel.setListId(row.getUUID("user_id"));
		listModel.setListName(row.getString("username"));
		listModel.setCreateUser(row.getUUID("create_user"));
		listModel.setCreateDate(row.getTimestamp("create_date"));
		listModel.setUpdateUser(row.getUUID("update_user"));
		listModel.setUpdateDate(row.getTimestamp("update_date"));

		return listModel;
	}
}
