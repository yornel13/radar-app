package com.guardias.yornel.gpslocation.entity;

import com.guardias.yornel.gpslocation.db.DataHelper;

import io.realm.RealmObject;

/**
 * Admin entity. @author MyEclipse Persistence Tools
 */

public class Admin extends RealmObject {

	// Fields

	private Long id;
	private String dni;
	private String username;
	private String password;
	private String name;
	private String lastname;
	private Long create;
	private Long update;
	private Boolean active;

	// Constructors

	/** default constructor */
	public Admin() {
	}

	/** full constructor */
	public Admin(String username, String password, String name,
			String lastname, Long create, Long update, Boolean active) {
		this.username = username;
		this.password = password;
		this.name = name;
		this.lastname = lastname;
		this.create = create;
		this.update = update;
		this.active = active;
	}

	// Property accessors

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return this.name;
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

	public Long getCreate() {
		return this.create;
	}

	public void setCreate(Long create) {
		this.create = create;
	}

	public Long getUpdate() {
		return this.update;
	}

	public void setUpdate(Long update) {
		this.update = update;
	}

	public Boolean getActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void save() {
		DataHelper.save(this);
	}

}