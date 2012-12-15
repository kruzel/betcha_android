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

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.StakeRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stakes")
public class Stake extends ModelCache<Stake, String> {
	@DatabaseField
	private String Name;
	@DatabaseField
	private String image_url;
	@DatabaseField
	private String affilifate_access_token;
	@DatabaseField
	private String affiliate_url;
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getAffilifate_access_token() {
		return affilifate_access_token;
	}

	public void setAffilifate_access_token(String affilifate_access_token) {
		this.affilifate_access_token = affilifate_access_token;
	}

	public String getAffiliate_url() {
		return affiliate_url;
	}

	public void setAffiliate_url(String affiliate_url) {
		this.affiliate_url = affiliate_url;
	}

	public static Stake get(String id) {
		Stake stake = null;
		try {
			stake = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return stake;
	}
	
	private static Dao<Stake, String> dao;
	private StakeRestClient stakeRestClient;

	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Stake, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Stake.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Stake, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Stake.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public StakeRestClient getRestClient() {
		if (stakeRestClient == null)
			stakeRestClient = new StakeRestClient();

		return stakeRestClient;
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
		
		List<Stake> stakes = new ArrayList<Stake>();

		StakeRestClient restClient = new StakeRestClient();
		JSONObject jsonstakes = null;
		try {
			if(lastUpdate==null)
				jsonstakes = restClient.show_for_user();
			else
				jsonstakes = restClient.show_updates_for_user(lastUpdate); 

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonstakes == null)
			return 0;
		
		JSONArray jsonStakesArray = null;
		try {
			jsonStakesArray = jsonstakes.getJSONArray("stakes");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonStakesArray == null)
			return 0;

		for (int i = 0; i < jsonStakesArray.length(); i++) {
			JSONObject jsonStake;

			try {
				jsonStake = jsonStakesArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			Stake tmpStake = null;
			try {
				List<Stake> tmpStakes = Stake.getModelDao().queryForEq("id",jsonStake.getString("id"));
				if (tmpStakes != null && tmpStakes.size() > 0) {
					tmpStake = tmpStakes.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpStake == null) {
				tmpStake = new Stake();
			}

			if (!tmpStake.setJson(jsonStake))
				continue;

			tmpStake.setServerCreated(true);
			tmpStake.setServerUpdated(true);
			
			try {
				tmpStake.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			stakes.add(tmpStake);

		}

		return stakes.size();
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
			setImage_url(json.getString("image_url"));
		} catch (JSONException e1) {
		}
		
		try {
			setAffilifate_access_token(json.getString("access_token"));
		} catch (JSONException e1) {
		}
		
		try {
			setAffiliate_url(json.getString("url"));
		} catch (JSONException e1) {
		}
		
		return true;
	}

	public static String[] getIds() {
		List<Stake> stakes = null;
		try {
			stakes = getModelDao().queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(stakes==null)
			return null;
		
		String[] strArray = new String[stakes.size()];
		int i = 0;
		for (Stake reward : stakes) {
			strArray[i] = reward.getId();
			i++;
		}
		return strArray;
	}
	
}
