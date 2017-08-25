package com.guardias.yornel.gpslocation.entity;

import java.util.HashSet;
import java.util.Set;

import io.realm.RealmObject;

/**
 * Route entity. @author MyEclipse Persistence Tools
 */

public class Route extends RealmObject {

	// Fields

	private Long id;
	private String name;
	private Long createDate;
	private Long lastUpdate;
	private Boolean active;

	// Uso local
	private boolean selected;

	// Constructors

	/** default constructor */
	public Route() {
	}

	/** minimal constructor */
	public Route(String name, Long createDate, Long lastUpdate, Boolean active) {
		this.name = name;
		this.createDate = createDate;
		this.lastUpdate = lastUpdate;
		this.active = active;
	}

	/** full constructor */
	public Route(String name, Long createDate, Long lastUpdate, Boolean active,
			Set groups, Set routePositions) {
		this.name = name;
		this.createDate = createDate;
		this.lastUpdate = lastUpdate;
		this.active = active;
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

	public void setName(String name) {
		this.name = name;
	}

	public Long getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Long getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Boolean getActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}