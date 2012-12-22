package com.betcha.model;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import android.content.Context;

import com.betcha.adapter.CategoryAdapter;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.TopicRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "topics")
public class Topic  extends ModelCache<Topic, String> {
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private TopicCategory category;
	@DatabaseField
	private String name;
	@DatabaseField
	private DateTime startTime;
	@DatabaseField
	private DateTime endtTime;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Location location;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private TopicResults results;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<PredictionOption> options;
	
	public static List<Topic> getForCategory(Context context, String catpgoryId) {
				
		List<Topic> newTopicsList = null;
				
		try {
			newTopicsList = Topic.getModelDao().queryForEq("category_id", catpgoryId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		return newTopicsList;
	}
	
	public static Topic get(String id) {
		Topic topic = null;
		try {
			topic = Topic.getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topic;
	}

	public TopicCategory getCategory() {
		return category;
	}

	public void setCategory(TopicCategory category) {
		this.category = category;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getEndtTime() {
		return endtTime;
	}

	public void setEndtTime(DateTime endtTime) {
		this.endtTime = endtTime;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Collection<PredictionOption> getOptions() {
		return options;
	}

	public void setOptions(ForeignCollection<PredictionOption> options) {
		this.options = options;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<Topic, String> dao;
	private TopicRestClient restClient;
	
	public static Dao<Topic, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Topic.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Topic, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Topic.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public TopicRestClient getRestClient() {
		if (restClient == null)
			restClient = new TopicRestClient();

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
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		if (json == null)
			return null;

		super.setJson(json);

		try {
			setName(json.getString("name"));
		} catch (JSONException e1) {
		}

		try {
			setStartTime(formatter.parseDateTime(json.getString("start_time")));
		} catch (JSONException e1) {
		}
		
		try {
			setEndtTime(formatter.parseDateTime(json.getString("end_time")));
		} catch (JSONException e1) {
		}

		String loctionId = null;
		try {
			loctionId = json.getString("location_id");
		} catch (JSONException e3) {
			//e3.printStackTrace();
		}
		
		if(loctionId!=null) {
			location = Location.get(loctionId);	//all locations are loaded earlier
			if(location!=null)
				setLocation(location);
		}
		
		//options
		JSONArray jsonOptions = null;
		try {
			jsonOptions = json.getJSONArray("prediction_options");
		} catch (JSONException e) {
			e.printStackTrace();
			return false; // must have at list one prediction
		}

		if (jsonOptions != null) {
			for (int j = 0; j < jsonOptions.length(); j++) {
				JSONObject jsonOption;

				try {
					jsonOption = jsonOptions.getJSONObject(j);
				} catch (JSONException e3) {
					e3.printStackTrace();
					continue;
				}
				
				PredictionOption tmpOption = null;
				if(options!=null && options.size()>0) {
					for (PredictionOption option : options) {
						String optionId = jsonOption.optString("id");
						if(optionId!=null && option.getId().equals(optionId)) {
							tmpOption = option;
							continue;
						}
					}
				}

				if (tmpOption == null) {
					tmpOption = new PredictionOption();
				}

				if (!tmpOption.setJson(jsonOption))
					continue;

				tmpOption.setTopic(this);
				
				tmpOption.setServerCreated(true);
				tmpOption.setServerUpdated(true);

				try {
					tmpOption.createOrUpdateLocal();
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
				
				//options.add(tmpOption);
			}
						
			//results - must be done after setting the options
			JSONObject jsonResults = null;

			try {
				jsonResults = json.getJSONObject("topic_results");
			} catch (JSONException e3) {
				//e3.printStackTrace();
			}
			
			if(jsonResults!=null) {
				results = new TopicResults();
	
				if (results.setJson(jsonResults)) {
	
					results.setTopic(this);
					
					results.setServerCreated(true);
					results.setServerUpdated(true);
			
					try {
						results.createOrUpdateLocal();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		try {
			if(options!=null)
				options.refreshCollection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
				
		return true;
	}
}
