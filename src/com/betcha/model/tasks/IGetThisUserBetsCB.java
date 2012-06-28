package com.betcha.model.tasks;

import java.util.List;

import com.betcha.model.Bet;

public interface IGetThisUserBetsCB {
	
	abstract void OnGetUserBetsCompleted(Boolean success, List<Bet> bets);
}
