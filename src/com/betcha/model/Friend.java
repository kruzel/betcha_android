/**
 * 
 */
package com.betcha.model;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.FriendRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author ofer
 *
 */
@DatabaseTable(tableName = "friends")
public class Friend extends ModelCache<Friend, Integer> {
	@DatabaseField(generatedId  = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user; 
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User friend; 
	
	private int retries = 0;
	
	public Friend() {
		super();
	}
	
	public Friend(User user) {
		super();
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getFriend() {
		return friend;
	}

	public void setFriend(User friend) {
		this.friend = friend;
	}

	private static Dao<Friend,Integer> dao;
	
	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Friend, Integer> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(Friend.class);
		}
		return dao;
	}
	
	@Override
	public void initDao() {
		try {
			setDao(getModelDao());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean setJson(JSONObject json) {
		super.setJson(json);
		
		try {
			user = User.getAndCreateUser(json.getInt("user_id"));
		} catch (JSONException e2) {
			e2.printStackTrace();
			return false;
		}
		
		try {
			friend = User.getAndCreateUser(json.getInt("friend_id"));
		} catch (JSONException e2) {
			e2.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static Boolean getCurrentUsetBets(User curUser) {
		
		return false; 
	}

	@Override
	public int onRestCreate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int onRestUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int onRestGet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int onRestGetWithDependents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int onRestGetAllForCurUser() {
		// this may take some time till we fetch all contacts from FB, so keep trying till it succeed
		int res = 0;
		
		FriendRestClient restClient = new FriendRestClient(user.getId());
		JSONArray friends = restClient.show_for_user();
		
		if(friends==null || friends.length()==0) {
			if(retries<3) {
			
				//try again in 10 sec
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						retries++;
						try {
							synchronized (this) {
								  this.wait(10000);
								}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						Log.i("Friend.onRestGetAllForCurUser()", "retrying to get friends");
						Friend.this.onRestGetAllForCurUser();
					}
				});
				thread.run();
			} 
				
			return 0;
		}
		
		JSONObject jsonFriend = null;
		for (int i = 0; i < friends.length(); i++) {
			try {
				jsonFriend = friends.getJSONObject(i);
			} catch (JSONException e1) {
				e1.printStackTrace();
				continue;
			}
			
			User tmpNewUser = null;
			try {
				List<User> tmpNewUsers = User.getModelDao().queryForEq("server_id", jsonFriend.getInt("id"));
				if(tmpNewUsers!=null && tmpNewUsers.size()>0) { 
					tmpNewUser = tmpNewUsers.get(0);
				} 
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if(tmpNewUser!=null) 
				continue; //known user
			
			tmpNewUser = new User();
							
			if(!tmpNewUser.setJson(jsonFriend))
				continue;
			
			try {
				tmpNewUser.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			
			if(user.getServer_id() == tmpNewUser.getServer_id())
				continue;
			
			Friend friend = new Friend(user);
			friend.setFriend(tmpNewUser);
			try {
				friend.createLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			
			res++;
		}
		
		return res;
	}

	@Override
	public int onRestDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int onRestSyncToServer() {
		int res = 0;
		if(!isServerUpdated()) {
			if(getServer_id()==-1) {
				res = onRestCreate();
			} else {
				res = onRestUpdate(); 
			}
		}
		
		return res;
	}

}
