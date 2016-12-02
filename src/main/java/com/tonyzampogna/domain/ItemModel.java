package com.tonyzampogna.domain;

import java.util.Date;
import java.util.UUID;

/**
 * Item Model
 */
public class ItemModel {
	private UUID itemId = null;
	private UUID listId = null;
	private String itemName = null;
	private UUID createUser = null;
	private Date createDate = null;
	private UUID updateUser = null;
	private Date updateDate = null;


	public UUID getItemId() {
		return itemId;
	}

	public void setItemId(UUID itemId) {
		this.itemId = itemId;
	}

	public UUID getListId() {
		return listId;
	}

	public void setListId(UUID listId) {
		this.listId = listId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
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
