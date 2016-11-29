package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * This class contains the methods for operating on UserModels.
 */
@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;

	// Bound Statements
	private PreparedStatement PS_CREATE_USER = null;
	private PreparedStatement PS_GET_USER_BY_USERID = null;
	private PreparedStatement PS_GET_USER_BY_USERNAME = null;
	private PreparedStatement PS_UPDATE_USER_BY_USERID = null;
	private PreparedStatement PS_UPDATE_USER_BY_USERNAME = null;
	private PreparedStatement PS_DELETE_USER_BY_USERID = null;
	private PreparedStatement PS_DELETE_USER_BY_USERNAME = null;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	public void createUser(UserModel userModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_CREATE_USER == null) {
			PS_CREATE_USER = session.prepare(
				"INSERT INTO users (user_id, username, password, first_name, last_name, email_address, create_date, create_user, update_date, update_user) " +
				"VALUES (:userId, :username, :password, :firstName, :lastName, :emailAddress, :createDate, :createUser, :updateDate, :updateUser)");
		}

		BoundStatement boundStatement = PS_CREATE_USER.bind();
		updateBoundStatement(boundStatement, userModel);

		session.execute(boundStatement);
	}

	public UserModel getUserById(UUID userId) {
		UserModel userModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_USER_BY_USERID == null) {
			PS_GET_USER_BY_USERID = session.prepare(
				"SELECT user_id, username, password, first_name, last_name, email_address, create_date, create_user, update_date, update_user " +
				"FROM users WHERE userId = :userId");
		}

		BoundStatement boundStatement = PS_GET_USER_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			userModel = transformRowToUser(row);
		}

		return userModel;
	}

	public UserModel getUserByUsername(String username) {
		UserModel userModel = null;

		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_GET_USER_BY_USERNAME == null) {
			PS_GET_USER_BY_USERNAME = session.prepare(
				"SELECT user_id, username, password, first_name, last_name, email_address, create_date, create_user, update_date, update_user " +
				"FROM users WHERE username = :username");
		}

		BoundStatement boundStatement = PS_GET_USER_BY_USERNAME.bind();
		boundStatement.setString("username", username);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			userModel = transformRowToUser(row);
		}

		return userModel;
	}

	public void updateUserByUserId(UserModel userModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_UPDATE_USER_BY_USERID == null) {
			PS_UPDATE_USER_BY_USERID = session.prepare(
				"UPDATE users SET " +
				"user_id = :userId " +
				"username = :username " +
				"password = :password " +
				"first_name = :firstName " +
				"last_name = :lastName " +
				"email_address = :emailAddress " +
				"create_date = :createDate " +
				"create_user = :createUser " +
				"update_date = :updateDate " +
				"update_user = :updateUser " +
				"WHERE user_id = :userId");
		}

		BoundStatement boundStatement = PS_UPDATE_USER_BY_USERID.bind();
		updateBoundStatement(boundStatement, userModel);

		session.execute(boundStatement);
	}

	public void updateUserByUsername(UserModel userModel) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_UPDATE_USER_BY_USERNAME == null) {
			PS_UPDATE_USER_BY_USERNAME = session.prepare(
				"UPDATE users SET " +
				"user_id = :userId " +
				"username = :username " +
				"password = :password " +
				"first_name = :firstName " +
				"last_name = :lastName " +
				"email_address = :emailAddress " +
				"create_date = :createDate " +
				"create_user = :createUser " +
				"update_date = :updateDate " +
				"update_user = :updateUser " +
				"WHERE username = :username");
		}

		BoundStatement boundStatement = PS_UPDATE_USER_BY_USERNAME.bind();
		updateBoundStatement(boundStatement, userModel);

		session.execute(boundStatement);
	}

	public void deleteUserByUserId(UUID userId) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_DELETE_USER_BY_USERID == null) {
			PS_DELETE_USER_BY_USERID = session.prepare(
				"DELETE FROM users WHERE user_id = :userId");
		}

		BoundStatement boundStatement = PS_DELETE_USER_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);

		session.execute(boundStatement);
	}

	public void deleteUserByUsername(String username) {
		// Get the Session.
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it doesn't exist.
		if (PS_DELETE_USER_BY_USERNAME == null) {
			PS_DELETE_USER_BY_USERNAME = session.prepare(
				"DELETE FROM users WHERE username = :username");
		}

		BoundStatement boundStatement = PS_DELETE_USER_BY_USERNAME.bind();
		boundStatement.setString("username", username);

		session.execute(boundStatement);
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	private void updateBoundStatement(BoundStatement boundStatement, UserModel userModel) {
		boundStatement.setUUID("userId", userModel.getUserId());
		boundStatement.setString("username", userModel.getUsername());
		boundStatement.setString("password", userModel.getPassword());
		boundStatement.setString("firstName", userModel.getFirstName());
		boundStatement.setString("lastName", userModel.getLastName());
		boundStatement.setString("emailAddress", userModel.getEmailAddress());
		boundStatement.setTimestamp("createDate", userModel.getCreateDate());
		boundStatement.setUUID("createUser", userModel.getCreateUser());
		boundStatement.setTimestamp("updateDate", userModel.getUpdateDate());
		boundStatement.setUUID("updateUser", userModel.getUpdateUser());
	}

	private UserModel transformRowToUser(Row row) {
		if (row == null) {
			return null;
		}

		UserModel userModel = new UserModel();
		userModel.setUserId(row.getUUID("user_id"));
		userModel.setUsername(row.getString("username"));
		userModel.setPassword(row.getString("password"));
		userModel.setFirstName(row.getString("first_name"));
		userModel.setLastName(row.getString("last_name"));
		userModel.setEmailAddress(row.getString("email_address"));
		userModel.setCreateDate(row.getTimestamp("create_date"));
		userModel.setCreateUser(row.getUUID("create_user"));
		userModel.setUpdateDate(row.getTimestamp("update_date"));
		userModel.setUpdateUser(row.getUUID("update_user"));

		return userModel;
	}

}
