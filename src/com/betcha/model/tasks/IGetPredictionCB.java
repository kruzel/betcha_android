package com.betcha.model.tasks;

import com.betcha.model.Prediction;

public interface IGetPredictionCB {
	
	abstract void OnGetPredictionCompleted(Boolean success, Prediction prediction);
}
