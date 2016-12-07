package com.tonyzampogna.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * List Model
 */
public class ListModel {
	private UUID listId = null;
	private String listName = null;
	private UserListModel.AuthorizationLevel authorizationLevel = null;
	private UUID createUser = null;
	private Date createDate = null;
	private UUID updateUser = null;
	private Date updateDate = null;
	private List<UUID> itemSortOrder = null;

	@JsonIgnore()
	private List<ItemModel> itemModels = new ArrayList<ItemModel>();


	public UUID getListId() {
		return listId;
	}

	public void setListId(UUID listId) {
		this.listId = listId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public UserListModel.AuthorizationLevel getAuthorizationLevel() {
		return authorizationLevel;
	}

	public void setAuthorizationLevel(UserListModel.AuthorizationLevel authorizationLevel) {
		this.authorizationLevel = authorizationLevel;
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

	public List<UUID> getItemSortOrder() {
		return itemSortOrder;
	}

	public void setItemSortOrder(List<UUID> itemSortOrder) {
		this.itemSortOrder = itemSortOrder;
	}

	public List<ItemModel> getItemModels() {
		return itemModels;
	}

	public void setItemModels(List<ItemModel> itemModels) {
		this.itemModels = itemModels;
	}
}
