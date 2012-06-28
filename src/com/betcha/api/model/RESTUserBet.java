package com.betcha.api.model;

import com.betcha.model.UserBet;

public class RESTUserBet {
	private int id;
	private int user_id;
	private int bet_id;
	private String user_result_bet;
	private String date;
	private Boolean result;
	private String user_ack;

	public RESTUserBet (UserBet userBet) {
		this.id = userBet.getServer_id();
		this.user_id = userBet.getUser().getServer_id();
		this.bet_id = userBet.getBet().getServer_id();
		this.user_result_bet = userBet.getMyBet();
		this.date = userBet.getDate().toString();
		this.result = userBet.getResult();
		this.user_ack = userBet.getMyAck();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getUser_result_bet() {
		return user_result_bet;
	}
	public void setUser_result_bet(String user_result_bet) {
		this.user_result_bet = user_result_bet;
	}
	public Boolean getResult() {
		return result;
	}
	public void setResult(Boolean result) {
		this.result = result;
	}
	public String getUser_ack() {
		return user_ack;
	}
	public void setUser_ack(String user_ack) {
		this.user_ack = user_ack;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getBet_id() {
		return bet_id;
	}

	public void setBet_id(int bet_id) {
		this.bet_id = bet_id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
}
