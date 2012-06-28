package com.betcha.model.tasks;

import java.util.List;

import com.betcha.model.UserBet;

public interface IGetUserBetsCB {
	
	abstract void OnGetUserBetsCompleted(Boolean success, List<UserBet> usersbets);
}
