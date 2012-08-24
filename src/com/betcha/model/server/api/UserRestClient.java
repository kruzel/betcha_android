package com.betcha.model.server.api;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

public class UserRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		UserRestClient.url = url;
	}
	
	public JSONArray list() throws RestClientException {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject show(int id) throws RestClientException {
		String res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class);
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject create(String full_name, String email, String password) throws RestClientException {		
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("provider", "email");
			jsonContent.put("email", email);
			jsonContent.put("full_name", full_name);
			jsonContent.put("password", password);
			jsonParent.put("user", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		
		String res = restTemplate.postForObject(url + ".json" , request, String.class);	
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject createOAuth(String provider, String uid, String access_token ) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("provider", provider);
			jsonContent.put("uid", uid);
			jsonContent.put("access_token", access_token);
			jsonParent.put("user", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res = restTemplate.postForObject(url + ".json" , request, String.class);	
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public void update(Map<String,String> arg, int id) throws RestClientException {
		arg.put("auth_token", GetToken());
		
		restTemplate.put(url + "/" + id + ".json", arg);
	}

	public void delete(int id) throws RestClientException {
		restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
	}
}
