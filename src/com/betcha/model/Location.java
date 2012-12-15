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
import com.betcha.model.server.api.LocationRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "locations")
public class Location extends ModelCache<Location, String> {
	
	@DatabaseField
	private String country;
	@DatabaseField
	private String city;
		
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	public static Location get(String id) {
		Location location = null;
		try {
			location = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return location;
	}
	
	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<Location, String> dao;
	private LocationRestClient locationRestClient;
	
	public static Dao<Location, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Location.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Location, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Location.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public LocationRestClient getRestClient() {
		if (locationRestClient == null)
			locationRestClient = new LocationRestClient();

		return locationRestClient;
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
		return Location.getAllUpdatesForCurUser(null);
	}

	public static int getAllUpdatesForCurUser(DateTime lastUpdate) {
		
		List<Location> locations = new ArrayList<Location>();

		LocationRestClient restClient = new LocationRestClient();
		JSONObject jsonLocations = null;
		try {
			if(lastUpdate==null)
				jsonLocations = restClient.show_for_user();
			else
				jsonLocations = restClient.show_updates_for_user(lastUpdate); 

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonLocations == null)
			return 0;
		
		JSONArray jsonLocationsArray = null;
		try {
			jsonLocationsArray = jsonLocations.getJSONArray("locations");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonLocationsArray == null)
			return 0;

		for (int i = 0; i < jsonLocationsArray.length(); i++) {
			JSONObject jsonLocation;

			try {
				jsonLocation = jsonLocationsArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			Location tmpLocation = null;
			try {
				List<Location> tmpLocations = Location.getModelDao().queryForEq("id",jsonLocation.getString("id"));
				if (tmpLocations != null && tmpLocations.size() > 0) {
					tmpLocation = tmpLocations.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpLocation == null) {
				tmpLocation = new Location();
			}

			if (!tmpLocation.setJson(jsonLocation))
				continue;

			tmpLocation.setServerCreated(true);
			tmpLocation.setServerUpdated(true);
			
			try {
				tmpLocation.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			locations.add(tmpLocation);

		}

		return locations.size();
	}
	
	public Boolean setJson(JSONObject json) {
		if (json == null)
			return null;

		super.setJson(json);

		try {
			setCountry(json.getString("country"));
		} catch (JSONException e1) {
		}

		try {
			setCity(json.getString("city"));
		} catch (JSONException e1) {
		}
		
		return true;
	}

}
