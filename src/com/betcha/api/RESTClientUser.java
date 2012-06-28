package com.betcha.api;

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.betcha.R;
import com.betcha.api.model.RESTUser;
import com.betcha.model.User;

public class RESTClientUser extends RESTClient {

	public RESTClientUser(Context context) {
		super(context.getString(R.string.betcha_api) + "/users");
	}

	public List<User> list() {
		// TODO Auto-generated method stub
		return null;
	}

	public RESTUser show(int id) {
		RESTUser res = restTemplate.getForObject(url + "/" + id + ".json" , RESTUser.class);
		return res;
	}
	
	public RESTUser create(Map<String,String> arg) {
		RESTUser res = restTemplate.postForObject(url + ".json" , arg, RESTUser.class);		
		return res;
	}

	public void update(Map<String,String> arg, int id) {
		restTemplate.put(url + "/" + id + ".json", arg);
	}

	public void delete(int id) {
		restTemplate.delete(url  + "/" + id + ".json");
	}
}
