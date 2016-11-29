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

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;

	// Bound Statements
	private PreparedStatement PS_CREATE_LIST = null;
	private PreparedStatement PS_CREATE_USER_LIST = null;
	private PreparedStatement PS_GET_LIST_BY_LISTID = null;
	private PreparedStatement PS_GET_LISTS_BY_USERID = null;
	private PreparedStatement PS_GET_LISTS_BY_USERNAME = null;
	private PreparedStatement PS_UPDATE_LIST_BY_LISTID = null;
	private PreparedStatement PS_DELETE_LIST_BY_LISTID = null;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	public void createList(ListModel listModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_LIST == null) {
			PS_CREATE_LIST = session.prepare(
				"INSERT INTO lists (list_id, list_name, create_date, create_user, update_date, update_user) " +
				"VALUES (:listId, :listName, :createDate, :createUser, :updateDate, :updateUser)");
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
				"INSERT INTO user_lists (list_id, list_name, create_date, create_user, update_date, update_user) " +
				"VALUES (:listId, :listName, :createDate, :createUser, :updateDate, :updateUser)");
		}

		BoundStatement boundStatement2 = PS_CREATE_LIST.bind();
		updateBoundStatement(boundStatement2, listModel);

		// Create a batch statement to run both of the inserts.
		BatchStatement batchStatement = new BatchStatement();
		batchStatement.add(boundStatement1);
		batchStatement.add(boundStatement2);

		session.execute(batchStatement);
	}

	public ListModel getListById(UUID listId) {
		ListModel listModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_LIST_BY_LISTID == null) {
			PS_GET_LIST_BY_LISTID = session.prepare(
				"SELECT list_id, list_name, create_date, create_user, update_date, update_user " +
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
				"SELECT l.list_id, l.list_name, l.create_date, l.create_user, l.update_date, l.update_user " +
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
				"SELECT l.list_id, l.list_name, l.create_date, l.create_user, l.update_date, l.update_user " +
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

	public void updateListByListId(ListModel listModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_UPDATE_LIST_BY_LISTID == null) {
			PS_UPDATE_LIST_BY_LISTID = session.prepare(
				"UPDATE lists SET " +
				"list_id = :listId " +
				"list_name = :listName " +
				"create_date = :createDate " +
				"create_user = :createUser " +
				"update_date = :updateDate " +
				"update_user = :updateUser " +
				"WHERE list_id = :listId");
		}

		BoundStatement boundStatement = PS_UPDATE_LIST_BY_LISTID.bind();
		updateBoundStatement(boundStatement, listModel);

		session.execute(boundStatement);
	}

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
		boundStatement.setTimestamp("createDate", listModel.getCreateDate());
		boundStatement.setUUID("createUser", listModel.getCreateUser());
		boundStatement.setTimestamp("updateDate", listModel.getUpdateDate());
		boundStatement.setUUID("updateUser", listModel.getUpdateUser());
	}

	private ListModel transformRowToList(Row row) {
		if (row == null) {
			return null;
		}

		ListModel listModel = new ListModel();
		listModel.setListId(row.getUUID("user_id"));
		listModel.setListName(row.getString("username"));
		listModel.setCreateDate(row.getTimestamp("create_date"));
		listModel.setCreateUser(row.getUUID("create_user"));
		listModel.setUpdateDate(row.getTimestamp("update_date"));
		listModel.setUpdateUser(row.getUUID("update_user"));

		return listModel;
	}
}
