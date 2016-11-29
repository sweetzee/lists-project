package com.tonyzampogna.domain;

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
	private Date createDate = null;
	private UUID createUser = null;
	private Date updateDate = null;
	private UUID updateUser = null;

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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public UUID getCreateUser() {
		return createUser;
	}

	public void setCreateUser(UUID createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public UUID getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(UUID updateUser) {
		this.updateUser = updateUser;
	}

	public List<ItemModel> getItemModels() {
		return itemModels;
	}

	public void setItemModels(List<ItemModel> itemModels) {
		this.itemModels = itemModels;
	}
}
