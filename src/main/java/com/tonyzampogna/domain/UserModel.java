package com.tonyzampogna.domain;

import java.util.Date;
import java.util.UUID;

/**
 * User Model
 */
public class UserModel {
	public enum AuthorizationLevel {
		ADMIN("ADMIN"),
		USER("USER");

		private final String value;

		AuthorizationLevel(String value) {
			this.value = value;
		}

		public String toString() {
			return name();
		}
	};

	private UUID userId = null;
	private String username = null;
	private String password = null;
	private AuthorizationLevel authorizationLevel = null;
	private String firstName = null;
	private String lastName = null;
	private String emailAddress = null;
	private UUID createUser = null;
	private Date createDate = null;
	private UUID updateUser = null;
	private Date updateDate = null;


	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthorizationLevel getAuthorizationLevel() {
		return authorizationLevel;
	}

	public void setAuthorizationLevel(AuthorizationLevel authorizationLevel) {
		this.authorizationLevel = authorizationLevel;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public UUID getCreateUser() {
		return createUser;
	}

	public void setCreateUser(UUID createUser) {
		this.createUser = createUser;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public UUID getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(UUID updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
