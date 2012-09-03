package com.betcha.model.server.api;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

public class TokenRestClient extends RestClient {
	protected static String url;
	
	public static void setUrl(String url) {
		TokenRestClient.url = url;
	}
	
	public String create(String email, String password) {
		Map<String,String> arg = new HashMap<String,String>();
		arg.put("provider", "email");
		arg.put("email", email);
		arg.put("password", password);
		
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , arg, String.class);
		} catch (RestClientException e1) {
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
		
		String token = null;
		try {
			token = json.getString("token");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return token;
	}
	
	
	public String createOAuth(String provider, String uid, String access_token) {
		Map<String,String> arg = new HashMap<String,String>();
		arg.put("provider", provider);
		arg.put("uid", uid);
		arg.put("access_token", access_token);
		
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , arg, String.class);
		} catch (RestClientException e1) {
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
		
		String token = null;
		try {
			token = json.getString("token");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}

	//sign_out
	public void delete() {
		try {
			restTemplate.delete(url  + "/" + token + ".json");
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}
