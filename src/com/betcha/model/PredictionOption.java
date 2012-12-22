package com.betcha.model;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import android.content.Context;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.PredictionOptionRestClinet;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "prediction_options")
public class PredictionOption extends ModelCache<PredictionOption, String> {
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Topic topic;
	@DatabaseField
	private String name;
	@DatabaseField
	private String image_url;
	
	public static PredictionOption get(String id) {
		PredictionOption option = null;
		try {
			option = PredictionOption.getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return option;
	}
	
	public static List<PredictionOption> getForTopic(Context context, String topicId) {
		
		List<PredictionOption> newSuggestionsList = null;
		
		try {
			newSuggestionsList = PredictionOption.getModelDao().queryForEq("topic_id", topicId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return newSuggestionsList;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Topic getTopic() {
		return topic;
	}
	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageUrl() {
		return image_url;
	}
	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}
	
	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<PredictionOption, String> dao;
	private PredictionOptionRestClinet restClient;
	
	public static Dao<PredictionOption, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(PredictionOption.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<PredictionOption, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(PredictionOption.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public PredictionOptionRestClinet getRestClient() {
		if (restClient == null)
			restClient = new PredictionOptionRestClinet();

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
			setName(json.getString("name"));
		} catch (JSONException e1) {
		}
		
		try {
			setImageUrl(json.getString("image_url"));
		} catch (JSONException e1) {
		}
		
		return true;
	}
}
