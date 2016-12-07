package com.tonyzampogna.domain;

import java.util.UUID;

/**
 * UserList Model
 */
public class UserListModel {
	public enum AuthorizationLevel {
		OWNER("OWNER"),
		WRITE_ACCESS("WRITE_ACCESS"),
		READ_ACCESS("READ_ACCESS");

		private final String value;

		AuthorizationLevel(String value) {
			this.value = value;
		}

		public String toString() {
			return name();
		}
	};

	private UUID userId = null;
	private UUID listId = null;
	private AuthorizationLevel authorizationLevel = null;


	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getListId() {
		return listId;
	}

	public void setListId(UUID listId) {
		this.listId = listId;
	}

	public AuthorizationLevel getAuthorizationLevel() {
		return authorizationLevel;
	}

	public void setAuthorizationLevel(AuthorizationLevel authorizationLevel) {
		this.authorizationLevel = authorizationLevel;
	}
}
