package com.betcha.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.betcha.R;
import com.betcha.api.model.RESTBet;

public class RESTClientBet extends RESTClient {

	public RESTClientBet(Context context) {
		super(context.getString(R.string.betcha_api) + "/bets");
	}

	public List<RESTBet> list() {
		// TODO Auto-generated method stub
		return null;
	}


	public RESTBet show(int id) {
		RESTBet res = restTemplate.getForObject(url + "/" + id + ".json" , RESTBet.class);
		return res;
	}
	
	public RESTBet showUUID(String uuid) {
		RESTBet res = restTemplate.getForObject(url + "/show_uuid.json?uuid={uuid}" , RESTBet.class, uuid);
		return res;
	}

	public RESTBet create(Map<String,String> arg) {
		RESTBet res = restTemplate.postForObject(url + ".json" , arg, RESTBet.class);		
		return res;
	}

	public void update(Map<String,String> arg, int id) {
		restTemplate.put(url + "/" + id + ".json", arg);
	}

	public void delete(int id) {
		restTemplate.delete(url  + "/" + id + ".json");
	}
}
