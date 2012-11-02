package com.betcha.model;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

import com.betcha.BetchaApp;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.TokenRestClient;
import com.betcha.model.server.api.UserRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@DatabaseTable(tableName = "users")
public class User extends ModelCache<User,String> {
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
	private String push_notifications_device_id; 
	@DatabaseField
	private String profile_pic_url;
	@DatabaseField
	private Long contact_id;
	@DatabaseField
	private Long contact_photo_id;
	@ForeignCollectionField(eager = false)
	private ForeignCollection<Bet>  bets;
	@ForeignCollectionField(eager = false)
	private ForeignCollection<Friend>  friends;
	
	//non persistent
	private static UserRestClient userClient;
	private static Dao<User,String> dao;
	private static Bitmap default_pic;
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	private Boolean isInvitedToBet = false;
	
	public void setUser(User newUser) {
		this.id = newUser.getId();
		this.name = newUser.getName();
		this.email = newUser.getEmail();
		this.password = newUser.getPassword();
		this.provider = newUser.getProvider();
		this.uid = newUser.getUid();
		this.access_token = newUser.getAccess_token();
		this.profile_pic_url = newUser.getProfile_pic_url();
		this.push_notifications_device_id = newUser.getPush_notifications_device_id();
		this.contact_id = newUser.getContact_id();
		this.contact_photo_id = newUser.getContact_photo_id();
		setCreated_at(newUser.getCreated_at());
		setUpdated_at(newUser.getUpdated_at());
	}

	public UserRestClient getUserClient() {
		if(userClient==null)
			userClient = new UserRestClient();
		
		return userClient;
	}
	
	public HttpStatus getLastRestErrorCode() {
		return getUserClient().getLastRestErrorCode();
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

	public Boolean getIsInvitedToBet() {
		return isInvitedToBet;
	}

	public void setIsInvitedToBet(Boolean isInvitedToBet) {
		this.isInvitedToBet = isInvitedToBet;
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

	public String getPush_notifications_device_id() {
		return push_notifications_device_id;
	}

	public void setPush_notifications_device_id(String push_notifications_device_id) {
		this.push_notifications_device_id = push_notifications_device_id;
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<User,String> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(User.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	@Override
	protected Dao<User, String> getDao() throws SQLException {
		if(dao==null){
			dao = getDbHelper().getDao(User.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	@Override
	protected Boolean authenticateCreate() {
		return false;
	}

	/** inherited ModelCache methods */
	public int onRestCreate() {
		if(isServerCreated())
			return 1;
		
		Friend friend = new Friend(this);
		//report completion only after friend get is done
		//friend.setListener(listener);
		//setListener(null);
		
		if(restCreateUserAccount()==0)
			return 0;
		
		if(restCreateToken()==0)
			return 0;
			
		//get friends
		if(getProvider().equals("facebook"))
			friend.onRestGetAllForCurUser();
		
		return 1;
	}
	
	//TODO try to unify with OnRestGet()
	public int restCreateUserAccount() {
		int res = 0;
		JSONObject jsonUser = null;
		
		if (provider.equals("email")) {
			jsonUser = getUserClient().create(getId(), name, email, password, getProfilePhotoBitmap());
		} else if (provider.equals("facebook")) {
			jsonUser = getUserClient().createOAuth(getId(), provider, uid, access_token);
		}
		
		if(jsonUser==null)
			return 0;
		
		JSONArray jsonArray = null;
		try {
			jsonArray = jsonUser.getJSONArray("users");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(jsonArray==null)
			return 0;

		JSONObject jsonContent = null;
		try {
			jsonContent = jsonArray.getJSONObject(0);
		} catch (JSONException e1) {
		}
		
		if(jsonContent==null)
			return 0;
		
		setServerCreated(true);
		
		String resId = jsonContent.optString("id");
		if(resId!=null && resId!=getId()) {
			//oops the user already exist on the server, need to update id locally,
			recreateUser(jsonContent);
		}
					
		return 1;
	}
	
	private void recreateUser(JSONObject jsonUser) {
	
		Boolean isLocalUser = BetchaApp.getInstance().getCurUser()==this;
	
		String oldId = getId();
		onLocalDelete();
		
		setJson(jsonUser);
		onLocalCreate();
		
		if(isLocalUser)
			BetchaApp.getInstance().setMe(this);
		
		List<Friend> friends = null;
		try {
			friends = Friend.getModelDao().queryForEq("friend_id", oldId);
		} catch (SQLException e) {
		}
		
		if(friends!=null && friends.size()>0) {
			for (Friend friend : friends) {
				friend.setUser(this);
				friend.onLocalUpdate();
			}
		}
		
		// then update all foreign keys as well
		// for now the  relevant foreign keys are prediction and friends
		List<Prediction> predictions = null;
		try {
			predictions = Prediction.getModelDao().queryForEq("user_id", oldId);
		} catch (SQLException e) {
		}
		
		if(predictions!=null && predictions.size()>0) {
			for (Prediction prediction : predictions) {
				prediction.setUser(this);
				prediction.onLocalUpdate();
			}
		}
		
		List<ChatMessage> chatMessages = null;
		try {
			chatMessages = ChatMessage.getModelDao().queryForEq("user_id", oldId);
		} catch (SQLException e) {
		}
		
		if(chatMessages!=null && chatMessages.size()>0) {
			for (ChatMessage chatMessage : chatMessages) {
				chatMessage.setUser(this);
				chatMessage.onLocalUpdate();
			}
		}
	}
	
	public int restCreateToken() {
			
		TokenRestClient tokenClient = new TokenRestClient();
		String jsonToken = null;
		if (provider.equals("email")) {
			jsonToken = tokenClient.create(email, password);
		} else if (provider.equals("facebook")) {
			jsonToken = tokenClient.createOAuth(provider, uid, access_token);
		}
					
		BetchaApp.setToken(jsonToken);
		
		if(jsonToken==null) {
			return 0;
		} else {
			return 1;
		}
	}

	public int onRestUpdate() {
		Log.i("User.onRestUpdate()", "updating server");
		getUserClient().update(this);
		
		return 1;
	}

	public int onRestDelete() {
		getUserClient().delete(getId());
		return 1;
	}

	public int onRestSync() {
		int res = 0;
		res = onRestGet();
		
		if(!isServerUpdated()) {
			if(getId()==null) {
				res =+ onRestCreate();
			} else {
				res =+ onRestUpdate(); 
			}
		} 
		
		return res;
	}
	
	@Override
	public int onRestGet() {
		UserRestClient userClient = new UserRestClient();
		JSONObject jsonUser = null;
		
		if(isServerCreated()) {
			try {
				jsonUser = userClient.show(getId());
			} catch (RestClientException e) {
				e.printStackTrace();
				return 0;
			}
		} else { 
			try {
				if(getProvider()!=null) {
					if(getProvider().equals("email"))
						jsonUser = userClient.showViaEmail(getEmail());
					else if(getProvider().equals("facebook"))
						jsonUser = userClient.showViaUid(getUid());
					
					if(jsonUser==null)
						return 0;
									
				} else {
					//may happen when we recover user account from the server so need to reload predictions users
					jsonUser = userClient.show(getId());
				}
			} catch (RestClientException e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		if(jsonUser==null)
			return 0;
		
		JSONArray jsonArray = null;
		try {
			jsonArray = jsonUser.getJSONArray("users");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(jsonArray==null)
			return 0;

		JSONObject jsonContent = null;
		try {
			jsonContent = jsonArray.getJSONObject(0);
		} catch (JSONException e1) {
		}
		
		if(jsonContent==null)
			return 0;
				
		String resId = jsonContent.optString("id");
		if(resId!=null && resId!=getId()) {
			//oops the user already exist on the server, need to replace with new id
			recreateUser(jsonContent);
			return 1;
		} else {
			//update existing user
			setJson(jsonContent);
			
			try {
				createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		return 1;
	}
	
	//should be executed in UI thread
	public static User getUserLocalOrRemoteInner(String user_id) {
		User tmpOwner = null;
		try {
			List<User> listUser = null;
			listUser = User.getModelDao().queryForEq("id", user_id);
			if(listUser!=null && listUser.size()>0) {
				tmpOwner = listUser.get(0);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		if(tmpOwner != null) { //then create it
			return tmpOwner;
		} 
		
		tmpOwner = new User();
		tmpOwner.setId(user_id);
		if(tmpOwner.onRestGet()==0)
			return null;
		
		return tmpOwner;
	}
	
	public Boolean setJson(JSONObject json) {
		super.setJson(json);
		
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
			setName(json.getString("full_name"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			setUid(json.getString("uid"));
		} catch (JSONException e) {
		}
		
		try {
			String pic = json.getString("profile_pic_url");
			if(pic!=null && !pic.equals("null"))
				setProfile_pic_url(pic);
		} catch (JSONException e) {
		}
		
		try {
			setPush_notifications_device_id(json.getString("push_notifications_device_id"));
		} catch (JSONException e) {
		}
		
		return true;
	}
	
	public void cancelProfilePhotoUpdate(ImageView image) 	{
		if(imageLoader!=null) 
			imageLoader.cancelDisplayTask(image);
	}
	
	public void setProfilePhoto(ImageView image) 	{
		Bitmap profile_pic_bitmap = null;
		if(default_pic==null)
			default_pic = BitmapFactory.decodeResource(context.getResources(), com.betcha.R.drawable.ic_launcher);
				
		//default image
		image.setImageBitmap(Bitmap.createScaledBitmap(default_pic, 100, 100, false));
		
		profile_pic_bitmap = getProfilePhotoBitmap();
		if(profile_pic_bitmap!=null) {
			image.setImageBitmap(Bitmap.createScaledBitmap(profile_pic_bitmap, 100, 100, false));
	    	return;
		}
				
		if(imageLoader==null) {
			imageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
		
		if(getProvider()!=null && getProvider().equals("facebook") && getUid()!=null) {	
			String url = "http://graph.facebook.com/" + getUid() + "/picture?type=square";
			imageLoader.displayImage(url, image,defaultOptions);
			return;
    	}
		
		String url = "http://robohash.org/" + getEmail() + ".png?set=set3&size=100x100";
		imageLoader.displayImage(url, image,defaultOptions);
		
	}
	
	private Bitmap getProfilePhotoBitmap() {
		Bitmap profile_pic_bitmap = null;
		
		ContentResolver cr = context.getContentResolver();
		
		if(getContact_id()!=null) {
		    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, getContact_id());
		    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		    if (input != null) 
		    {
		    	profile_pic_bitmap = BitmapFactory.decodeStream(input);
		    }
		}

		if(getContact_photo_id()!=null) {
		    byte[] photoBytes = null;
	
		    Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, getContact_photo_id());
		    Cursor c = cr.query(photoUri, new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
	
		    try 
		    {
		        if (c.moveToFirst()) 
		            photoBytes = c.getBlob(0);
	
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        c.close();
		    }           
	
		    if (photoBytes != null) {
		    	profile_pic_bitmap = BitmapFactory.decodeByteArray(photoBytes,0,photoBytes.length);
		    }    
		}
		
		return profile_pic_bitmap;
	}
	
	public int resetPassword() {
		getUserClient().resetPassword(getEmail());
		return 1;
	}

}
