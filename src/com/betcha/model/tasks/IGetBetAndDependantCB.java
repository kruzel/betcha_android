package com.betcha.model.tasks;

import com.betcha.model.Bet;

public interface IGetBetAndDependantCB {
	
	abstract void OnGetBetCompleted(Boolean success, Bet bet);
}
