package com.betcha.model;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.TopicResultRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class TopicResults extends ModelCache<TopicResults, String> {
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Topic topic;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private PredictionOption winningOption;
	@DatabaseField
	private Integer score1;
	@DatabaseField
	private Integer score2;
		
	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public PredictionOption getWinningOption() {
		return winningOption;
	}

	public void setWinningOption(PredictionOption winningOption) {
		this.winningOption = winningOption;
	}

	public Integer getScore1() {
		return score1;
	}

	public void setScore1(Integer score1) {
		this.score1 = score1;
	}

	public Integer getScore2() {
		return score2;
	}

	public void setScore2(Integer score2) {
		this.score2 = score2;
	}

	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<TopicResults, String> dao;
	private TopicResultRestClient restClient;
	
	public static Dao<TopicResults, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(TopicResults.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<TopicResults, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(TopicResults.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public TopicResultRestClient getRestClient() {
		if (restClient == null)
			restClient = new TopicResultRestClient();

		return restClient;
	}

	@Override
	public HttpStatus getLastRestErrorCode() {
		return getRestClient().getLastRestErrorCode();
	}

	@Override
	protected int onRestCreate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestGet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestDelete() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public Boolean setJson(JSONObject json) {
		
		if (json == null)
			return null;

		super.setJson(json);
		
		try {
			setScore1(json.getInt("result_score_1 "));
		} catch (JSONException e1) {
		}
		
		try {
			setScore2(json.getInt("result_score_2 "));
		} catch (JSONException e1) {
		}
		
		String optionId = null;
		try {
			optionId = json.getString("result_id");
		} catch (JSONException e1) {
		}
		
		if(optionId!=null) {
			setWinningOption(PredictionOption.get(optionId));
		}
		
		return true;
	}
}
