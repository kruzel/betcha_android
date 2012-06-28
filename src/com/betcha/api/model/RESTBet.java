package com.betcha.api.model;

import com.betcha.model.Bet;


public class RESTBet {
	
	private int id;
	private String uuid;
	private int user_id; //owner
	private String subject;
	private String reward; //benefit
	private String date;
	private String due_date;
	private String state; // open/due/closed
	
	public RESTBet(Bet bet) {
		this.setId(bet.getServer_id());
		this.setUser_id(bet.getOwner().getId());
		this.setSubject(bet.getSubject());
		this.setReward(bet.getReward());
		this.setDueDate(bet.getDueDate().toString());
		this.setDate(bet.getDate().toString());
		this.setUuid(bet.getUuid());
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int i) {
		this.user_id = i;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
		this.reward = reward;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDueDate() {
		return due_date;
	}
	public void setDueDate(String dueDate) {
		this.due_date = dueDate;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
