package com.betcha.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.betcha.R;
import com.betcha.api.model.RESTUserBet;


public class RESTClientUserBet extends RESTClient {

	public RESTClientUserBet(Context context) {
		super(context.getString(R.string.betcha_api) + "/user_bets");
	}

	public List<RESTUserBet> list() {
		// TODO Auto-generated method stub
		return null;
	}

	public RESTUserBet show(int id) {
		RESTUserBet res = restTemplate.getForObject(url + "/" + id + ".json" , RESTUserBet.class);
		return res;
	}
	
	public List<RESTUserBet> showBetId(int id) {
		
		RESTUserBet[] res = restTemplate.getForObject(url + "/show_bet_id.json?bet_id={id}" , RESTUserBet[].class, id);
		return new ArrayList<RESTUserBet>(Arrays.asList(res));
	}

	public RESTUserBet create(Map<String,String> arg) {
		RESTUserBet res = restTemplate.postForObject(url + ".json" , arg, RESTUserBet.class);		
		return res;
	}

	public void update(Map<String,String> arg, int id) {
		restTemplate.put(url + "/" + id + ".json", arg);
	}
	
	public void update(List<RESTUserBetUpdate> arg) {
		restTemplate.put(url + "/update_list.json", arg);
	}

	public void delete(int id) {
		restTemplate.delete(url  + "/" + id + ".json");
	}


}
