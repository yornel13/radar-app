package com.guardias.yornel.gpslocation.entity;

import com.guardias.yornel.gpslocation.db.DataHelper;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * User entity. @author MyEclipse Persistence Tools
 */

public class User extends RealmObject {

	// Fields

	private Long id;
	private Group group;
	private String dni;
	private String name;
	private String lastname;
	private String password;
	private Long createDate;
	private Long lastUpdate;
	private Boolean active;

	@Ignore
	List<Watch> watches;

	// Constructors

	/** default constructor */
	public User() {
	}

	/** minimal constructor */
	public User(String name, String lastname) {
		this.name = name;
		this.lastname = lastname;
	}

	// Property accessors

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public String getFullName() {
		return this.lastname+" "+this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public List<Watch> getWatches() {
		return watches;
	}

	public void setWatches(List<Watch> watches) {
		this.watches = watches;
	}

	public void save() {
		DataHelper.save(this);
	}
}