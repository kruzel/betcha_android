package com.betcha.model.tasks;

import com.betcha.model.Bet;

public interface IGetBetAndOwnerCB {
	
	abstract void OnGetBetCompleted(Boolean success, Bet bet);
}
