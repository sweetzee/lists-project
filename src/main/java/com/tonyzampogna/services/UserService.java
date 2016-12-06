package com.tonyzampogna.services;

import com.datastax.driver.core.*;
import com.tonyzampogna.domain.UserModel;
import com.tonyzampogna.factory.ListsDatabaseSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class contains the methods for operating on UserModels.
 */
@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	// Prepared Statements
	private static PreparedStatement PS_CREATE_USER = null;
	private static PreparedStatement PS_GET_USER_BY_USERID = null;
	private static PreparedStatement PS_GET_USER_BY_USERNAME = null;
	private static PreparedStatement PS_GET_USER_COUNT_BY_USERNAME = null;
	private static PreparedStatement PS_UPDATE_USER_BY_USERID = null;
	private static PreparedStatement PS_UPDATE_USER_CREDENTIALS_BY_USERID = null;
	private static PreparedStatement PS_DELETE_USER_BY_USERID = null;

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;


	/////////////////////////////////////////////////
	// Service Methods
	/////////////////////////////////////////////////

	/**
	 * Create
	 */
	public List<UserModel> createUsers(List<UserModel> userModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each UserModel...
		for (UserModel userModel : userModelList) {
			UUID userId = userModel.getUserId();
			String username = userModel.getUsername();
			String password = userModel.getPassword();

			// Create a new ID, if necessary.
			if (StringUtils.isEmpty(userId)) {
				userId = UUID.randomUUID();
				userModel.setUserId(userId);
			}

			// Generate a log buffer.
			log.info("Creating user in the database. User ID: " + userId);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(userModel.getCreateUser()) ||
				StringUtils.isEmpty(userModel.getCreateDate()) ||
				StringUtils.isEmpty(userModel.getUpdateUser()) ||
				StringUtils.isEmpty(userModel.getUpdateDate())) {
				throw new RuntimeException("The create and update user and timestamp cannot be blank. User ID: " + userId + ". Username: " + username);
			}

			// Make sure the username does not already exist.
			int count = getUserCountByUsername(username);
			if (count > 0) {
				throw new RuntimeException("Username already exists in the database. User ID: " + userId + ". Username: " + username);
			}

			// Username must not be blank.
			if (StringUtils.isEmpty(username)) {
				throw new RuntimeException("Username must not be blank during creation. User ID: " + userId + ". Username: " + username);
			}

			// Password must not be blank.
			if (StringUtils.isEmpty(password)) {
				throw new RuntimeException("Password must not be blank during creation. User ID: " + userId + ". Username: " + username);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getCreateUsersBoundStatements(userModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return userModelList;
	}

	/**
	 * Read (by userId)
	 */
	public UserModel getUserById(UUID userId) {
		UserModel userModel = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading user from the database (by userId). User ID: " + userId);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_USER_BY_USERID == null) {
			PS_GET_USER_BY_USERID = session.prepare(
				"SELECT user_id, username, password, first_name, last_name, email_address, create_user, create_date, update_user, update_date " +
				"FROM users WHERE user_id = :userId");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_USER_BY_USERID.bind();
		boundStatement.setUUID("userId", userId);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		Row row = resultSet.one();
		if (row != null) {
			userModel = transformRowToUser(row);
		}

		return userModel;
	}

	/**
	 * Read (by username)
	 */
	public UserModel getUserByUsername(String username) {
		UserModel userModel = null;
		Session session = listsDatabaseSessionFactory.getSession();

		log.info("Reading user from the database (by username). User ID: " + username);

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_USER_BY_USERNAME == null) {
			PS_GET_USER_BY_USERNAME = session.prepare(
				"SELECT user_id, username, password, first_name, last_name, email_address, create_user, create_date, update_user, update_date " +
				"FROM users WHERE username = :username");
		}

		// Execute Database Transaction
		BoundStatement boundStatement = PS_GET_USER_BY_USERNAME.bind();
		boundStatement.setString("username", username);
		ResultSet resultSet = session.execute(boundStatement);

		// Transform Results
		Row row = resultSet.one();
		if (row != null) {
			userModel = transformRowToUser(row);
		}

		return userModel;
	}

	/**
	 * Update
	 *
	 * This function updates everything on the User table
	 * except for the authentication information (username
	 * and password). Use the updateUserCredentials() function
	 * to do that.
	 */
	public List<UserModel> updateUsers(List<UserModel> userModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each UserModel...
		for (UserModel userModel : userModelList) {
			UUID userId = userModel.getUserId();
			String username = userModel.getUsername();

			// Generate a log buffer.
			log.info("Updating user in the database. User ID: " + userId + ". Username: " + username);

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(userModel.getUpdateUser()) ||
				StringUtils.isEmpty(userModel.getUpdateDate())) {
				throw new RuntimeException("The update user and timestamp cannot be blank. User ID: " + userId + ". Username: " + username);
			}
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getUpdateUsersBoundStatements(userModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return userModelList;
	}

	/**
	 * Update (User Credentials).
	 *
	 * This function updates only the user credentials. It makes sure
	 * to check to see if the username exists (only if it's changed).
	 */
	public List<UserModel> updateUserCredentials(List<UserModel> userModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each UserModel...
		for (UserModel userModel : userModelList) {
			UUID userId = userModel.getUserId();
			String username = userModel.getUsername();

			UserModel currentUserModel = getUserById(userModel.getUserId());

			// Generate a log buffer.
			log.info("Updating user in the database. User ID: " + userId + ". Username: " + username);

			// If no user exists, throw an error.
			if (currentUserModel == null) {
				throw new RuntimeException("User does not exist. User ID: " + userId + ". Username: " + username);
			}

			// Check if the username is changing.
			if (StringUtils.hasLength(username) && !username.equals(currentUserModel.getUsername())) {
				// The username is getting changed.
				// Check to see if it exists.
				int count = getUserCountByUsername(username);
				if (count > 0) {
					throw new RuntimeException("Username already exists in the database. User ID: " + userId + ". Username: " + username);
				}
			}
			else {
				// Set the username to the current username.
				userModel.setUsername(currentUserModel.getUsername());
			}

			// Check if the password is changing.
			if (StringUtils.isEmpty(userModel.getPassword())) {
				// Password cannot be empty. Set the password to the current password.
				userModel.setPassword(currentUserModel.getPassword());
			}

			// Make sure our logging fields are not empty.
			if (StringUtils.isEmpty(userModel.getUpdateUser()) ||
				StringUtils.isEmpty(userModel.getUpdateDate())) {
				throw new RuntimeException("The update user and timestamp cannot be blank. User ID: " + userId + ". Username: " + username);
			}
		}

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_USER_CREDENTIALS_BY_USERID == null) {
			PS_UPDATE_USER_CREDENTIALS_BY_USERID = session.prepare(
				"UPDATE users SET " +
				"username = :username, " +
				"password = :password, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE user_id = :userId");
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getUpdateUserCredentialsBoundStatements(userModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return userModelList;
	}

	/**
	 * Delete
	 */
	public List<UserModel> deleteUsers(List<UserModel> userModelList) {
		Session session = listsDatabaseSessionFactory.getSession();

		// For each UserModel...
		for (UserModel userModel : userModelList) {
			UUID userId = userModel.getUserId();

			// Generate a log buffer.
			log.info("Deleting list from the database. User ID: " + userId);
		}

		// Execute Database Transaction
		BatchStatement batchStatement = new BatchStatement();
		List<BoundStatement> boundStatements = getDeleteUsersBoundStatements(userModelList);
		if (boundStatements != null) {
			batchStatement.addAll(boundStatements);
			session.execute(batchStatement);
		}

		return userModelList;
	}


	/////////////////////////////////////////////////
	// Bound Statement Methods
	/////////////////////////////////////////////////

	/**
	 * Return the bound statements to create a list of users.
	 */
	public List<BoundStatement> getCreateUsersBoundStatements(List<UserModel> userModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_CREATE_USER == null) {
			PS_CREATE_USER = session.prepare(
				"INSERT INTO users (user_id, username, password, first_name, last_name, email_address, create_user, create_date, update_user, update_date) " +
				"VALUES (:userId, :username, :password, :firstName, :lastName, :emailAddress, :createUser, :createDate, :updateUser, :updateDate)");
		}

		if (userModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (UserModel userModel : userModelList) {
				BoundStatement boundStatement = PS_CREATE_USER.bind();
				updateBoundStatement(boundStatement, userModel);
				boundStatement.setString("username", userModel.getUsername());
				boundStatement.setString("password", userModel.getPassword());
				boundStatement.setUUID("createUser", userModel.getCreateUser());
				boundStatement.setTimestamp("createDate", userModel.getCreateDate());
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to update a list of items.
	 */
	public List<BoundStatement> getUpdateUsersBoundStatements(List<UserModel> userModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_USER_BY_USERID == null) {
			PS_UPDATE_USER_BY_USERID = session.prepare(
				"UPDATE users SET " +
				"first_name = :firstName, " +
				"last_name = :lastName, " +
				"email_address = :emailAddress, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE user_id = :userId");
		}

		if (userModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (UserModel userModel : userModelList) {
				BoundStatement boundStatement = PS_UPDATE_USER_BY_USERID.bind();
				updateBoundStatement(boundStatement, userModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to update a list of items.
	 */
	public List<BoundStatement> getUpdateUserCredentialsBoundStatements(List<UserModel> userModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_UPDATE_USER_CREDENTIALS_BY_USERID == null) {
			PS_UPDATE_USER_CREDENTIALS_BY_USERID = session.prepare(
				"UPDATE users SET " +
				"username = :username, " +
				"password = :password, " +
				"update_user = :updateUser, " +
				"update_date = :updateDate " +
				"WHERE user_id = :userId");
		}

		if (userModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (UserModel userModel : userModelList) {
				BoundStatement boundStatement = PS_UPDATE_USER_CREDENTIALS_BY_USERID.bind();
				boundStatement.setUUID("userId", userModel.getUserId());
				boundStatement.setString("username", userModel.getUsername());
				boundStatement.setString("password", userModel.getPassword());
				boundStatement.setUUID("updateUser", userModel.getUpdateUser());
				boundStatement.setTimestamp("updateDate", userModel.getUpdateDate());
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}

	/**
	 * Return the bound statements to delete a list of items.
	 */
	public List<BoundStatement> getDeleteUsersBoundStatements(List<UserModel> userModelList) {
		List<BoundStatement> boundStatements = null;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_DELETE_USER_BY_USERID == null) {
			PS_DELETE_USER_BY_USERID = session.prepare(
				"DELETE FROM users WHERE user_id = :userId");
		}

		if (userModelList != null) {
			boundStatements = new ArrayList<BoundStatement>();

			for (UserModel userModel : userModelList) {
				BoundStatement boundStatement = PS_DELETE_USER_BY_USERID.bind();
				updateBoundStatement(boundStatement, userModel);
				boundStatements.add(boundStatement);
			}
		}

		return boundStatements;
	}


	/////////////////////////////////////////////////
	// Helper Methods
	/////////////////////////////////////////////////

	/**
	 * This function is used to check whether a username
	 * already exists in the database. Used for changing the
	 * username.
	 */
	public int getUserCountByUsername(String username) {
		int count = 0;
		Session session = listsDatabaseSessionFactory.getSession();

		// Create the PreparedStatement if it does not exist.
		if (PS_GET_USER_COUNT_BY_USERNAME == null) {
			PS_GET_USER_COUNT_BY_USERNAME = session.prepare(
				"SELECT cast(count(*) as int) as count FROM users WHERE username = :username");
		}

		BoundStatement boundStatement = PS_GET_USER_COUNT_BY_USERNAME.bind();
		boundStatement.setString("username", username);

		ResultSet resultSet = session.execute(boundStatement);
		Row row = resultSet.one();
		if (row != null) {
			count = row.getInt("count");
		}

		return count;
	}

	private void updateBoundStatement(BoundStatement boundStatement, UserModel userModel) {
		boundStatement.setUUID("userId", userModel.getUserId());
		boundStatement.setString("firstName", userModel.getFirstName());
		boundStatement.setString("lastName", userModel.getLastName());
		boundStatement.setString("emailAddress", userModel.getEmailAddress());
		boundStatement.setUUID("updateUser", userModel.getUpdateUser());
		boundStatement.setTimestamp("updateDate", userModel.getUpdateDate());
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
		userModel.setCreateUser(row.getUUID("create_user"));
		userModel.setCreateDate(row.getTimestamp("create_date"));
		userModel.setUpdateUser(row.getUUID("update_user"));
		userModel.setUpdateDate(row.getTimestamp("update_date"));

		return userModel;
	}

}
