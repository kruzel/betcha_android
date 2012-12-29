package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import com.betcha.BetchaApp;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.BadgeRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "badges")
public class Badge extends ModelCache<Badge, String> {
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user; // owner
	@DatabaseField
	private String name;
	@DatabaseField
	private Integer value;
	@DatabaseField
	private String image_url;

	public String getImageUrl() {
		return image_url;
	}

	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}

	public String getName() {
		return name;
	}

	public void setName(String type) {
		this.name = type;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static Badge get(String id) {
		Badge stake = null;
		try {
			stake = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return stake;
	}
	
	private static Dao<Badge, String> dao;
	private BadgeRestClient restClient;

	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Badge, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Badge.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Badge, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Badge.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public BadgeRestClient getRestClient() {
		if (restClient == null)
			restClient = new BadgeRestClient();

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
	
	public static int getAllUpdatesForCurUser(DateTime lastUpdate) {
		
		List<Badge> badges = new ArrayList<Badge>();

		BadgeRestClient restClient = new BadgeRestClient();
		restClient.setUser_id(BetchaApp.getInstance().getCurUser().getId());
		JSONObject jsonBadges = null;
		try {
			if(lastUpdate==null)
				jsonBadges = restClient.show_for_user();
			else
				jsonBadges = restClient.show_updates_for_user(lastUpdate); 

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonBadges == null)
			return 0;
		
		JSONArray jsonArray = null;
		try {
			jsonArray = jsonBadges.getJSONArray("badges");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonArray == null)
			return 0;

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json;

			try {
				json = jsonArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			Badge tmpBadge = null;
			try {
				List<Badge> tmpBadges = Badge.getModelDao().queryForEq("id",json.getString("id"));
				if (tmpBadges != null && tmpBadges.size() > 0) {
					tmpBadge = tmpBadges.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpBadge == null) {
				tmpBadge = new Badge();
			}

			if (!tmpBadge.setJson(json))
				continue;

			tmpBadge.setServerCreated(true);
			tmpBadge.setServerUpdated(true);
			
			try {
				tmpBadge.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			badges.add(tmpBadge);

		}

		return badges.size();
	}
	
	public Boolean setJson(JSONObject json) {
		if (json == null)
			return null;

		super.setJson(json);
		
		if(user==null) {
			try {
				user = User.getUserLocalOrRemoteInner(json.getString("user_id"));
				setUser(user);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		}
		
		if(user==null)
			return false;
	
		try {
			setName(json.getString("name"));
		} catch (JSONException e1) {
		}
		
		try {
			setValue(json.getInt("value"));
		} catch (JSONException e1) {
		}

		try {
			setImageUrl(json.getString("image_url"));
		} catch (JSONException e1) {
		}
		
		return true;
	}
	
}
