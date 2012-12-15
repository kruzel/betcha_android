package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import android.content.Context;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.TopicCategoryRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "topic_categories")
public class TopicCategory extends ModelCache<TopicCategory, String> {
	@DatabaseField
	private String name;
	@DatabaseField
	private String image_url;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Topic> topics;
	
	public static List<TopicCategory> getCategories(Context context, String categoryGroup) {
		List<TopicCategory> tmpCategoriesList = null;
		try {
			tmpCategoriesList = TopicCategory.getModelDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return tmpCategoriesList;
	}
		
	public static TopicCategory get(String id) {
		TopicCategory category = null;
		try {
			category = TopicCategory.getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return category;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String category) {
		this.name = category;
	}
	public String getImageUrl() {
		return image_url;
	}
	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}
	public Collection<Topic> getTopics() {
		return topics;
	}
	
	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<TopicCategory, String> dao;
	private TopicCategoryRestClient restClient;
	
	public static Dao<TopicCategory, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(TopicCategory.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<TopicCategory, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(TopicCategory.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public TopicCategoryRestClient getRestClient() {
		if (restClient == null)
			restClient = new TopicCategoryRestClient();

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
	
	@Override
	public int onRestGetAllForCurUser() {
		return TopicCategory.getAllUpdatesForCurUser(null);
	}

	public static int getAllUpdatesForCurUser(DateTime lastUpdate) {
		
		List<TopicCategory> categories = new ArrayList<TopicCategory>();

		TopicCategoryRestClient restClient = new TopicCategoryRestClient();
		JSONObject jsonCategories = null;
		try {
			if(lastUpdate==null)
				jsonCategories = restClient.show_for_user();
			else
				jsonCategories = restClient.show_updates_for_user(lastUpdate); 

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonCategories == null)
			return 0;
		
		JSONArray jsonCategoriesArray = null;
		try {
			jsonCategoriesArray = jsonCategories.getJSONArray("topic_categories");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonCategoriesArray == null)
			return 0;

		for (int i = 0; i < jsonCategoriesArray.length(); i++) {
			JSONObject jsonCategory;

			try {
				jsonCategory = jsonCategoriesArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			TopicCategory tmpCategory = null;
			try {
				List<TopicCategory> tmpCategories = TopicCategory.getModelDao().queryForEq("id",jsonCategory.getString("id"));
				if (tmpCategories != null && tmpCategories.size() > 0) {
					tmpCategory = tmpCategories.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpCategory == null) {
				tmpCategory = new TopicCategory();
			}

			if (!tmpCategory.setJson(jsonCategory))
				continue;

			tmpCategory.setServerCreated(true);
			tmpCategory.setServerUpdated(true);
			
			try {
				tmpCategory.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			categories.add(tmpCategory);

		}

		return categories.size();
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
		
		//topics
		JSONArray jsonTopics = null;
		try {
			jsonTopics = json.getJSONArray("topics");
		} catch (JSONException e) {
			e.printStackTrace();
			return false; // must have at list one prediction
		}

		if (jsonTopics != null) {
			for (int j = 0; j < jsonTopics.length(); j++) {
				JSONObject jsonTopic;

				try {
					jsonTopic = jsonTopics.getJSONObject(j);
				} catch (JSONException e3) {
					e3.printStackTrace();
					continue;
				}
				
				Topic tmpTopic = null;
				if(topics!=null && topics.size()>0) {
					for (Topic topic : topics) {
						String topicId = jsonTopic.optString("id");
						if(topicId!=null && topic.getId().equals(topicId)) {
							tmpTopic = topic;
							continue;
						}
					}
				}

				if (tmpTopic == null) {
					tmpTopic = new Topic();
				}

				if (!tmpTopic.setJson(jsonTopic))
					continue;

				tmpTopic.setCategory(this);
				
				tmpTopic.setServerCreated(true);
				tmpTopic.setServerUpdated(true);

				try {
					tmpTopic.createOrUpdateLocal();
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			try {
				if(topics!=null)
					topics.refreshCollection();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		return true;
	}
}
