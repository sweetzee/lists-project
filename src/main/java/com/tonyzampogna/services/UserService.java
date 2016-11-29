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
 * This class contains the methods for operating on Users.
 */
@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private ListsDatabaseSessionFactory listsDatabaseSessionFactory;

	// Bound Statements
	private PreparedStatement PS_GET_USER_BY_USERNAME = null;


	public UserModel getUserById(UUID userId) {
		return null;
	}

	public UserModel getUserByUsername(String username) {
		UserModel userModel = null;

//		// Get the Session.
//		Session session = listsDatabaseSessionFactory.getSession();
//
//		// Create the PreparedStatement if it doesn't exist.
//		if (PS_GET_USER_BY_USERNAME == null) {
//			PS_GET_USER_BY_USERNAME = session.prepare(
//					"SELECT user_id, username, first_name, last_name, email_address, create_date, create_user, update_date, update_user " +
//					"FROM users WHERE username = :username");
//		}
//
//		BoundStatement boundStatement = PS_GET_USER_BY_USERNAME.bind();
//		boundStatement.setString("username", username);
//
//		ResultSet resultSet = session.execute(boundStatement);
//		Row row = resultSet.one();
//		if (row != null) {
//			userModel = new UserModel();
//			userModel.setUserId(row.getUUID("user_id"));
//		}

		userModel = new UserModel();
		userModel.setUserId(UUID.randomUUID());
		userModel.setUsername("tony");

		return userModel;
	}

}
