package com.betcha.model.server.api;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import com.betcha.model.Bet;
import com.betcha.model.Prediction;

public class BetRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		BetRestClient.url = url;
	}
	
	public JSONArray list()  {
		// TODO Auto-generated method stub
		return null;
	}


	public JSONObject show(String id) {
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/" + id  + ".json?"+ GetURLTokenParam() , String.class);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray show_for_user() {
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_user.json?"+ GetURLTokenParam() , String.class);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray show_updates_for_user(DateTime lastUpdate) {
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_updates_for_user.json?"+ GetURLTokenParam() + "&updated_at=" + lastUpdate.toString() , String.class);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public JSONObject create(Bet bet) {
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , request, String.class);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}		
		JSONObject jsonRes = null;
		try {
			jsonRes = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonRes;
	}

	public void update(Bet bet) {
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		try {
			restTemplate.put(url + "/" + bet.getId() + ".json", request);
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateOrCreate(Bet bet) {
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		try {
			restTemplate.put(url + "/" + bet.getId() + "/update_or_create.json", request);
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void delete(String id)  {
		try {
			restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
	
}
