package com.betcha.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;

import com.betcha.R;
import com.betcha.api.model.RESTBet;

public class RESTClientBet extends RESTClient {

	public RESTClientBet(Context context) {
		super(context.getString(R.string.betcha_api) + "/bets");
	}

	public List<RESTBet> list() throws RestClientException {
		// TODO Auto-generated method stub
		return null;
	}


	public RESTBet show(int id) throws RestClientException {
		RESTBet res = restTemplate.getForObject(url + "/" + id  + ".json?"+ GetURLTokenParam() , RESTBet.class);
		return res;
	}
	
	public RESTBet showUUID(String uuid) throws RestClientException {
		RESTBet res = restTemplate.getForObject(url + "/show_uuid.json?uuid={uuid}&" + GetURLTokenParam()  , RESTBet.class, uuid);
		return res;
	}
	
	public List<RESTBet> show_for_user_id(int id) throws RestClientException {
		RESTBet[] res = restTemplate.getForObject(url + "/show_for_user_id/{id}.json?"+ GetURLTokenParam() , RESTBet[].class, id);
		return new ArrayList<RESTBet>(Arrays.asList(res));
	}

	public RESTBet create(Map<String,String> arg) throws RestClientException {
		arg.put("auth_token", GetToken());
		RESTBet res = restTemplate.postForObject(url + ".json" , arg, RESTBet.class);		
		return res;
	}

	public void update(Map<String,String> arg, int id) throws RestClientException {
		arg.put("auth_token", GetToken());
		restTemplate.put(url + "/" + id + ".json", arg);
	}

	public void delete(int id) throws RestClientException {
		restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
	}
}
