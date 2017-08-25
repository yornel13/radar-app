package com.guardias.yornel.gpslocation.entity;

import io.realm.RealmObject;

/**
 * RoutePosition entity. @author MyEclipse Persistence Tools
 */

public class RoutePosition extends RealmObject {

	// Fields

	private Long id;
	private ControlPosition controlPosition;
	private Route route;
	private Long createDate;

	// Constructors

	/** default constructor */
	public RoutePosition() {
	}

	/** full constructor */
	public RoutePosition(ControlPosition controlPosition, Route route,
			Long createDate) {
		this.controlPosition = controlPosition;
		this.route = route;
		this.createDate = createDate;
	}

	// Property accessors

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ControlPosition getControlPosition() {
		return this.controlPosition;
	}

	public void setControlPosition(ControlPosition controlPosition) {
		this.controlPosition = controlPosition;
	}

	public Route getRoute() {
		return this.route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public Long getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

}