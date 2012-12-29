package com.betcha.model;

import java.sql.SQLException;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.betcha.model.cache.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "contacts")
public class Contact {
	@DatabaseField(generatedId = true , canBeNull = false)
	protected Integer id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String email;
	@DatabaseField
	private String password;
	@DatabaseField
	private String provider;
	@DatabaseField
	private String uid;
	@DatabaseField
	private String access_token;
	@DatabaseField
	private String profile_pic_url;
	@DatabaseField
	private Long contact_id;
	@DatabaseField
	private Long contact_photo_id;
	
	//non persistent
	private static Dao<Contact,String> dao;
	private static DatabaseHelper databaseHelper = null;
		
	public void setContact(Contact newUser) {
		this.id = newUser.getId();
		this.name = newUser.getName();
		this.email = newUser.getEmail();
		this.password = newUser.getPassword();
		this.provider = newUser.getProvider();
		this.uid = newUser.getUid();
		this.access_token = newUser.getAccess_token();
		this.profile_pic_url = newUser.getProfile_pic_url();
		this.contact_id = newUser.getContact_id();
		this.contact_photo_id = newUser.getContact_photo_id();
	}
	
	public static Contact get(String id) {
		Contact user = null;
		try {
			user = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return user;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String pass) {
		this.password = pass;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	
	public String getProfile_pic_url() {
		return profile_pic_url;
	}

	public void setProfile_pic_url(String profile_pic_url) {
		this.profile_pic_url = profile_pic_url;
	}

	public Long getContact_id() {
		return contact_id;
	}

	public void setContact_id(Long contact_id) {
		this.contact_id = contact_id;
	}

	public Long getContact_photo_id() {
		return contact_photo_id;
	}

	public void setContact_photo_id(Long contact_photo_id) {
		this.contact_photo_id = contact_photo_id;
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static DatabaseHelper getDbHelper() {
		return databaseHelper;
	}
	
	public static void setDbHelper(DatabaseHelper dbHelper) {
		databaseHelper = dbHelper;
	}
	
	public static Dao<Contact,String> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(Contact.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	protected Dao<Contact, String> getDao() throws SQLException {
		if(dao==null){
			dao = getDbHelper().getDao(Contact.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
		
	public Boolean setJson(JSONObject json) {		
		try {
			setProvider(json.getString("provider"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			setEmail(json.getString("email"));
		} catch (JSONException e1) {
		}
		try {
			setName(json.getString("name"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			setUid(json.getString("id"));
		} catch (JSONException e) {
		}
		
		try {
			String pic = json.getString("profile_pic_url");
			if(pic!=null && !pic.equals("null"))
				setProfile_pic_url(pic);
		} catch (JSONException e) {
		}
				
		return true;
	}

}
