package com.guardias.yornel.gpslocation.entity;

import com.guardias.yornel.gpslocation.db.DataHelper;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Watch entity. @author MyEclipse Persistence Tools
 */

public class Watch extends RealmObject {

	// Fields

	private Long id;
	private User user;
	private Long startTime;
	private Long endTime;

	@Ignore
	private List<Position> positionsList;

	// Constructors

	/** default constructor */
	public Watch() {
	}

	/** minimal constructor */
	public Watch(User user, Long startTime, Long endTime) {
		this.user = user;
		this.startTime = startTime;
		this.endTime = endTime;
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

	public Long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public void save() {
		DataHelper.save(this);
	}

	public List<Position> getPositionsList() {
		return positionsList;
	}

	public void setPositionsList(List<Position> positionsList) {
		this.positionsList = positionsList;
	}
}