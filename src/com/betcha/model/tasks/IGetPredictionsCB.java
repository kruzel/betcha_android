package com.betcha.model.tasks;

import java.util.List;

import com.betcha.model.Prediction;

public interface IGetPredictionsCB {
	
	abstract void OnGetPredictionsCompleted(Boolean success, List<Prediction> predictions);
}
