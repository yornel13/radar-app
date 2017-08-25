package com.guardias.yornel.gpslocation.entity;

import io.realm.RealmObject;

/**
 * RouteMarker entity. @author MyEclipse Persistence Tools
 */

public class RouteMarker extends RealmObject {

	// Fields

	private Long id;
	private User user;
	private ControlPosition controlPosition;
	private Long createDate;

	// Constructors

	/** default constructor */
	public RouteMarker() {
	}

	/** full constructor */
	public RouteMarker(User user, ControlPosition controlPosition,
			Long createDate) {
		this.user = user;
		this.controlPosition = controlPosition;
		this.createDate = createDate;
	}

	// Property accessors

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ControlPosition getControlPosition() {
		return this.controlPosition;
	}

	public void setControlPosition(ControlPosition controlPosition) {
		this.controlPosition = controlPosition;
	}

	public Long getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

}