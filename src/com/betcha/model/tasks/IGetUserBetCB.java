package com.betcha.model.tasks;

import com.betcha.model.UserBet;

public interface IGetUserBetCB {
	
	abstract void OnGetUserBetCompleted(Boolean success, UserBet userbet);
}
